"""
services/affiliate.py
----------------------
Converte URLs de produto em links de afiliado rastreáveis, padronizando
SEMPRE a saída na chave `affiliate_link` (contrato exigido pelo schema
unificado `product_payload`), independentemente da plataforma de origem.

Nenhuma função aqui lança exceção para o chamador: erros de rede,
autenticação ou payload são capturados e retornam `None`, permitindo que o
worker aplique fallback (ex.: usar a URL original do produto) sem derrubar
o pipeline.

CORREÇÃO (auditoria v2.1): este módulo é agora a ÚNICA implementação da
geração de link de afiliado no projeto. Anteriormente a mesma lógica também
existia duplicada em nós Code do workflow n8n "Sub-Agente 1", mas de forma
desatualizada e insegura (credenciais reais hardcoded como fallback,
parâmetro `sign_method` ausente na assinatura da AliExpress). Manter duas
implementações do mesmo algoritmo de assinatura é, por si só, um risco:
elas divergem com o tempo. O workflow n8n foi simplificado para apenas
minerar e enfileirar o job — quem gera o link agora é sempre este módulo.
"""
import hashlib
import hmac
import logging
import time
from typing import Optional

import requests

from worker import config

logger = logging.getLogger("affiliate")

REQUEST_TIMEOUT = 10  # segundos


# ---------------------------------------------------------------------------
# SHOPEE AFFILIATE API (GraphQL)
# ---------------------------------------------------------------------------
def _shopee_signature(payload: str, timestamp: int) -> str:
    """Assinatura SHA256 exigida pela Shopee Affiliate Open API."""
    base_string = f"{config.SHOPEE_APP_ID}{timestamp}{payload}{config.SHOPEE_APP_KEY}"
    return hashlib.sha256(base_string.encode("utf-8")).hexdigest()


def get_shopee_affiliate_link(product_url: str) -> Optional[str]:
    """
    Gera um shortLink de afiliado via Shopee Affiliate Open API (GraphQL).
    Retorna None em qualquer falha (rede, auth, resposta inesperada).
    """
    if not config.SHOPEE_APP_ID or not config.SHOPEE_APP_KEY:
        logger.warning("[shopee] Credenciais ausentes — pulando geração de link.")
        return None

    endpoint = "https://open-api.affiliate.shopee.com.br/graphql"
    timestamp = int(time.time())

    query = """
    mutation generateShortLink($input: ShortLinkInput!) {
      generateShortLink(input: $input) {
        shortLink
      }
    }
    """
    variables = {"input": {"originUrl": product_url, "subIds": ["dropship-automation"]}}
    payload_str = f'{{"query":"{query}","variables":{variables}}}'
    signature = _shopee_signature(payload_str, timestamp)

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"SHA256 Credential={config.SHOPEE_APP_ID}, "
        f"Timestamp={timestamp}, Signature={signature}",
    }

    try:
        resp = requests.post(
            endpoint,
            json={"query": query, "variables": variables},
            headers=headers,
            timeout=REQUEST_TIMEOUT,
        )
        resp.raise_for_status()
        data = resp.json()
        # Optional chaining "manual" em Python — nunca deixa o acesso quebrar
        link = (
            (data.get("data") or {})
            .get("generateShortLink", {})
            .get("shortLink")
        )
        if not link:
            logger.error("[shopee] Resposta sem shortLink: %s", data)
            return None
        return link
    except (requests.RequestException, ValueError) as exc:
        logger.error("[shopee] Falha ao gerar affiliate link: %s", exc)
        return None


# ---------------------------------------------------------------------------
# ALIEXPRESS PORTALS API (REST + assinatura HMAC)
# ---------------------------------------------------------------------------
def _aliexpress_signature(params: dict) -> str:
    """Assinatura HMAC-SHA256 exigida pela AliExpress Open Platform API."""
    sorted_params = "".join(f"{k}{v}" for k, v in sorted(params.items()))
    return (
        hmac.new(
            config.ALIEXPRESS_APP_SECRET.encode("utf-8"),
            sorted_params.encode("utf-8"),
            hashlib.sha256,
        )
        .hexdigest()
        .upper()
    )


def get_aliexpress_affiliate_link(product_url: str) -> Optional[str]:
    """
    Gera um `promotion_link` via AliExpress Affiliate API
    (aliexpress.affiliate.link.generate). Inclui `sign_method`, parâmetro
    de sistema obrigatório da TOP API que estava ausente na versão antiga
    (n8n) deste algoritmo — sem ele a API rejeita a chamada.
    """
    if not config.ALIEXPRESS_APP_KEY or not config.ALIEXPRESS_APP_SECRET:
        logger.warning("[aliexpress] Credenciais ausentes — pulando geração de link.")
        return None

    endpoint = "https://api-sg.aliexpress.com/sync"
    params = {
        "app_key": config.ALIEXPRESS_APP_KEY,
        "method": "aliexpress.affiliate.link.generate",
        "sign_method": "sha256",
        "timestamp": str(int(time.time() * 1000)),
        "source_values": product_url,
        "promotion_link_type": "0",
        "tracking_id": config.ALIEXPRESS_TRACKING_ID or "dropship_automation",
        "format": "json",
        "v": "2.0",
    }
    params["sign"] = _aliexpress_signature(params)

    try:
        resp = requests.get(endpoint, params=params, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
        data = resp.json()
        links = (
            (data.get("aliexpress_affiliate_link_generate_response") or {})
            .get("resp_result", {})
            .get("result", {})
            .get("promotion_links", {})
            .get("promotion_link", [])
        )
        if not links:
            logger.error("[aliexpress] Resposta sem promotion_link: %s", data)
            return None
        # A API retorna lista — pegamos o primeiro item com fallback seguro
        first = links[0] if isinstance(links, list) else links
        return first.get("promotion_link") if isinstance(first, dict) else None
    except (requests.RequestException, ValueError) as exc:
        logger.error("[aliexpress] Falha ao gerar affiliate link: %s", exc)
        return None


# ---------------------------------------------------------------------------
# MERCADO LIVRE — fallback (API oficial de products; sem programa de
# afiliados público simplificado, então tratamos como fallback informativo)
# ---------------------------------------------------------------------------
def get_mercadolivre_link(product_url: str) -> Optional[str]:
    """
    Fallback: Mercado Livre não possui um endpoint self-service simples de
    shortlink de afiliado equivalente ao Shopee/AliExpress. Nesta primeira
    versão, apenas validamos que a URL é acessível e a retornamos como está
    (o rastreamento pode ser feito via parâmetro UTM próprio, se aplicável).
    """
    try:
        resp = requests.head(product_url, timeout=REQUEST_TIMEOUT, allow_redirects=True)
        if resp.status_code >= 400:
            logger.warning("[mercadolivre] URL retornou status %s", resp.status_code)
            return None
        # Adiciona UTM próprio como pseudo-rastreamento até integrarmos o
        # programa de afiliados oficial (quando disponível na conta).
        separador = "&" if "?" in product_url else "?"
        return f"{product_url}{separador}utm_source=dropship_automation"
    except requests.RequestException as exc:
        logger.error("[mercadolivre] Falha ao validar URL: %s", exc)
        return None


# ---------------------------------------------------------------------------
# FUNÇÃO ÚNICA DE ENTRADA — usada exclusivamente pelo worker
# ---------------------------------------------------------------------------
def gerar_affiliate_link(origem: str, product_url: str) -> str:
    """
    Ponto único de entrada. Sempre retorna uma string (nunca None) —
    em caso de falha em todas as tentativas, faz fallback para a URL
    original do produto, garantindo que o pipeline nunca quebre por causa
    de um link de afiliado ausente.
    """
    origem = (origem or "").lower().strip()

    estrategia = {
        "shopee": get_shopee_affiliate_link,
        "aliexpress": get_aliexpress_affiliate_link,
        "mercado_livre": get_mercadolivre_link,
        "mercadolivre": get_mercadolivre_link,
    }.get(origem)

    link = estrategia(product_url) if estrategia else None
    return link or product_url  # fallback final: nunca retorna vazio/None
