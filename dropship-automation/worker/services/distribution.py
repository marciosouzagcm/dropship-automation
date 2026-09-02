"""
services/distribution.py
--------------------------
Publicação do conteúdo aprovado nos canais de distribuição. Cada função
retorna um dicionário padronizado {"sucesso": bool, "detalhe": str,
"post_id": str|None} para que o worker/n8n consiga registrar o resultado
no campo `approval`/log do product_payload sem precisar tratar formatos
de erro diferentes por plataforma.

CORREÇÃO (auditoria v2.1): Instagram Graph API e TikTok Content Posting
API exigem uma URL HTTPS publicamente acessível para o vídeo — não aceitam
o caminho local de disco (`/data/assets/processed/...`) que o worker usa
internamente. A versão anterior enviava esse caminho local diretamente,
o que teria falhado sempre que essas duas integrações fossem de fato
usadas. Agora `_resolver_url_publica` constrói a URL a partir de
`config.PUBLIC_ASSETS_BASE_URL`; se essa variável não estiver configurada,
a publicação é pulada com um aviso claro em vez de uma tentativa silenciosa
e fadada ao erro. Ver docs/GUIA-CONFIGURACAO-APIS.md para como expor
`assets/processed` publicamente (Cloudflare Tunnel, S3/R2 + CDN, etc.).
"""
import logging
import os
import time
from typing import Optional

import requests

from worker import config

logger = logging.getLogger("distribution")

REQUEST_TIMEOUT = 30
GRAPH_API_VERSION = "v20.0"


def _resposta(sucesso: bool, detalhe: str, post_id: Optional[str] = None) -> dict:
    return {"sucesso": sucesso, "detalhe": detalhe, "post_id": post_id}


def _resolver_url_publica(caminho_local: str) -> Optional[str]:
    """
    Converte um caminho local de arquivo (ex.: /data/assets/processed/x.mp4)
    na URL pública equivalente, usando PUBLIC_ASSETS_BASE_URL como raiz.
    Retorna None se a variável não estiver configurada ou o caminho for vazio.
    """
    if not caminho_local:
        return None
    if caminho_local.startswith("http://") or caminho_local.startswith("https://"):
        return caminho_local  # já é uma URL (ex.: vídeo de referência remoto)
    if not config.PUBLIC_ASSETS_BASE_URL:
        logger.warning(
            "[distribution] PUBLIC_ASSETS_BASE_URL não configurado — "
            "não é possível expor '%s' publicamente para Instagram/TikTok.",
            caminho_local,
        )
        return None
    nome_arquivo = os.path.basename(caminho_local)
    return f"{config.PUBLIC_ASSETS_BASE_URL}/{nome_arquivo}"


# ---------------------------------------------------------------------------
# TELEGRAM — canal público de vendas (pós-aprovação, distinto do chat de
# aprovação usado pelo Sub-Agente 2)
# ---------------------------------------------------------------------------
def publicar_telegram_canal(video_url: str, legenda: str, link_compra: str) -> dict:
    if not config.TELEGRAM_BOT_TOKEN or not config.TELEGRAM_CHANNEL_ID:
        return _resposta(False, "Credenciais do canal Telegram ausentes.")

    url = f"https://api.telegram.org/bot{config.TELEGRAM_BOT_TOKEN}/sendVideo"
    body = {
        "chat_id": config.TELEGRAM_CHANNEL_ID,
        "video": video_url,
        "caption": legenda or "",
        "parse_mode": "HTML",
        "reply_markup": {
            "inline_keyboard": [[{"text": "🛒 Comprar agora", "url": link_compra or "#"}]]
        },
    }
    try:
        resp = requests.post(url, json=body, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
        data = resp.json()
        message_id = (data.get("result") or {}).get("message_id")
        return _resposta(True, "Publicado no canal do Telegram.", str(message_id))
    except requests.RequestException as exc:
        logger.error("[telegram] Falha ao publicar no canal: %s", exc)
        return _resposta(False, f"Erro Telegram: {exc}")


# ---------------------------------------------------------------------------
# INSTAGRAM GRAPH API — publicação de Reels (fluxo em 2 etapas: cria
# container -> publica o container)
# ---------------------------------------------------------------------------
def publicar_instagram_reel(video_url: str, legenda: str) -> dict:
    """
    `video_url` precisa ser uma URL publicamente acessível (a Graph API não
    aceita upload direto de binário para Reels) — normalmente resolvida por
    `_resolver_url_publica` a partir de PUBLIC_ASSETS_BASE_URL.
    """
    if not config.INSTAGRAM_GRAPH_API_TOKEN or not config.INSTAGRAM_BUSINESS_ACCOUNT_ID:
        return _resposta(False, "Credenciais do Instagram Graph API ausentes.")

    if not video_url:
        return _resposta(False, "URL pública do vídeo indisponível (ver PUBLIC_ASSETS_BASE_URL).")

    base = f"https://graph.facebook.com/{GRAPH_API_VERSION}/{config.INSTAGRAM_BUSINESS_ACCOUNT_ID}"

    try:
        # 1) Cria o container de mídia
        criar_resp = requests.post(
            f"{base}/media",
            data={
                "media_type": "REELS",
                "video_url": video_url,
                "caption": legenda or "",
                "access_token": config.INSTAGRAM_GRAPH_API_TOKEN,
            },
            timeout=REQUEST_TIMEOUT,
        )
        criar_resp.raise_for_status()
        container_id = criar_resp.json().get("id")
        if not container_id:
            return _resposta(False, "Falha ao criar container de mídia (sem id retornado).")

        # 2) Aguarda o processamento do container (polling simples)
        status = "IN_PROGRESS"
        tentativas = 0
        while status == "IN_PROGRESS" and tentativas < 10:
            time.sleep(3)
            status_resp = requests.get(
                f"https://graph.facebook.com/{GRAPH_API_VERSION}/{container_id}",
                params={"fields": "status_code", "access_token": config.INSTAGRAM_GRAPH_API_TOKEN},
                timeout=REQUEST_TIMEOUT,
            )
            status = status_resp.json().get("status_code", "ERROR")
            tentativas += 1

        if status != "FINISHED":
            return _resposta(False, f"Container não finalizou a tempo (status={status}).")

        # 3) Publica o container
        publicar_resp = requests.post(
            f"{base}/media_publish",
            data={"creation_id": container_id, "access_token": config.INSTAGRAM_GRAPH_API_TOKEN},
            timeout=REQUEST_TIMEOUT,
        )
        publicar_resp.raise_for_status()
        post_id = publicar_resp.json().get("id")
        return _resposta(True, "Reel publicado com sucesso.", post_id)

    except requests.RequestException as exc:
        logger.error("[instagram] Falha na publicação: %s", exc)
        return _resposta(False, f"Erro Instagram Graph API: {exc}")


# ---------------------------------------------------------------------------
# TIKTOK CONTENT POSTING API
# ---------------------------------------------------------------------------
def publicar_tiktok_video(video_path_publico: str, legenda: str, modo_rascunho: bool = True) -> dict:
    """
    Publica via TikTok Content Posting API. Por padrão usa `modo_rascunho=True`
    (privacy_level=SELF_ONLY), recomendado para contas em fase de homologação —
    o vídeo fica pendente de publicação manual final dentro do app do TikTok.
    Trocar para publicação direta exige aprovação de "Direct Post" pela TikTok.
    """
    if not config.TIKTOK_CONTENT_POSTING_TOKEN:
        return _resposta(False, "Token da TikTok Content Posting API ausente.")

    if not video_path_publico:
        return _resposta(False, "URL pública do vídeo indisponível (ver PUBLIC_ASSETS_BASE_URL).")

    endpoint = "https://open.tiktokapis.com/v2/post/publish/video/init/"
    headers = {
        "Authorization": f"Bearer {config.TIKTOK_CONTENT_POSTING_TOKEN}",
        "Content-Type": "application/json",
    }
    body = {
        "post_info": {
            "title": legenda or "",
            "privacy_level": "SELF_ONLY" if modo_rascunho else "PUBLIC_TO_EVERYONE",
        },
        "source_info": {
            "source": "PULL_FROM_URL",
            "video_url": video_path_publico,
        },
    }
    try:
        resp = requests.post(endpoint, json=body, headers=headers, timeout=REQUEST_TIMEOUT)
        resp.raise_for_status()
        data = resp.json()
        publish_id = (data.get("data") or {}).get("publish_id")
        if not publish_id:
            return _resposta(False, f"Resposta sem publish_id: {data}")
        return _resposta(True, "Vídeo enviado ao TikTok (rascunho/publicação iniciada).", publish_id)
    except requests.RequestException as exc:
        logger.error("[tiktok] Falha na publicação: %s", exc)
        return _resposta(False, f"Erro TikTok Content Posting API: {exc}")


# ---------------------------------------------------------------------------
# ORQUESTRAÇÃO — publica em todos os canais configurados no product_payload
# ---------------------------------------------------------------------------
def distribuir(product_payload: dict) -> dict:
    """
    Recebe o product_payload completo (já aprovado) e publica nos canais
    disponíveis. Nunca lança exceção — cada canal reporta seu próprio
    resultado, permitindo publicação parcial (ex.: Instagram falha mas
    TikTok e Telegram funcionam).
    """
    media = product_payload.get("media") or {}
    copy = product_payload.get("copywriting") or {}

    caminho_video = media.get("rendered_video_path") or ""
    # Telegram aceita tanto arquivo local (via upload) quanto URL — mas as
    # APIs de Instagram/TikTok exigem obrigatoriamente uma URL pública.
    url_publica = _resolver_url_publica(caminho_video)

    resultados = {
        "telegram": publicar_telegram_canal(
            url_publica or caminho_video,
            copy.get("telegram_post") or "",
            product_payload.get("affiliate_link") or "",
        ),
        "instagram": publicar_instagram_reel(url_publica, copy.get("instagram_caption") or ""),
        "tiktok": publicar_tiktok_video(url_publica, copy.get("tiktok_caption") or ""),
    }
    return resultados
