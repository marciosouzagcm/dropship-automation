"""
minerador_main.py
------------------
Script de mineração (scraping) de páginas de produto em Mercado Livre,
Shopee e AliExpress. Extrai título, preço e mídia bruta (imagens/vídeos)
diretamente do HTML público da página e envia o resultado para o webhook
`novo-produto-minerado` do Sub-Agente 1 (n8n), que apenas normaliza e
enfileira o job — quem gera o link de afiliado e processa o vídeo é o
worker (ver worker/services/affiliate.py e worker/worker.py).

Este script é a única parte do projeto que faz scraping de HTML: as APIs
oficiais (Shopee Affiliate, AliExpress Affiliate, Mercado Livre) não
retornam metadados completos de produto de forma simples e gratuita, então
esses dados vêm daqui e o restante do pipeline apenas os aproveita.

Uso:
    python3 minerador_main.py "https://produto.mercadolivre.com.br/..."
"""
import json
import os
import re
import sys
import time

import requests
from bs4 import BeautifulSoup

# Configuração do Webhook do Sub-Agente 1
WEBHOOK_URL = os.getenv("N8N_WEBHOOK_URL", "http://n8n:5678") + os.getenv(
    "N8N_WEBHOOK_MINERACAO_PATH", "/webhook/novo-produto-minerado"
)

# CORREÇÃO (auditoria v2.1): o webhook agora exige este header (ver
# worker/config.py e o node "Normalizar Entrada + Gerar product_id" do
# Sub-Agente 1). Sem ele, o n8n rejeita a requisição.
WEBHOOK_SHARED_SECRET = os.getenv("WEBHOOK_SHARED_SECRET", "")

# Atraso mínimo entre requisições HTTP para reduzir o risco de bloqueio por
# anti-bot dos marketplaces. Ajuste via env se precisar minerar em lote.
DELAY_ENTRE_REQUISICOES_SEGUNDOS = float(os.getenv("MINERADOR_DELAY_SEGUNDOS", "1.5"))

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
    "Accept-Language": "pt-BR,pt;q=0.9,en-US;q=0.8,en;q=0.7"
}


def _extrair_preco_json_ld(soup: BeautifulSoup) -> float:
    """
    Extrai o preço a partir do bloco estruturado JSON-LD (<script
    type="application/ld+json">), quando presente. É mais confiável que a
    regex genérica de "price" no HTML bruto, que pode acidentalmente
    capturar o preço de um produto relacionado/recomendado na mesma página
    em vez do produto principal.
    """
    for script_tag in soup.find_all("script", type="application/ld+json"):
        try:
            dados = json.loads(script_tag.string or "{}")
        except (json.JSONDecodeError, TypeError):
            continue

        candidatos = dados if isinstance(dados, list) else [dados]
        for item in candidatos:
            oferta = item.get("offers") if isinstance(item, dict) else None
            if isinstance(oferta, list):
                oferta = oferta[0] if oferta else None
            if isinstance(oferta, dict) and oferta.get("price"):
                try:
                    return float(oferta["price"])
                except (TypeError, ValueError):
                    continue
    return 0.0


def minerar_mercado_livre(url):
    print(f"[+] Minerando Mercado Livre: {url}")
    session = requests.Session()
    session.headers.update(HEADERS)

    try:
        # Resolver redirects de URLs encurtadas (ex: mercadolivre.com/sec/...)
        res = session.get(url, timeout=12, allow_redirects=True)
        if res.status_code != 200:
            print(f"[-] Erro HTTP Mercado Livre: status {res.status_code}")
            return None

        html = res.text
        soup = BeautifulSoup(html, 'html.parser')

        # 1. Captura de Título
        title_tag = soup.find('meta', property='og:title') or soup.find('h1', class_=re.compile(r'ui-pdp-title'))
        title = title_tag['content'] if title_tag and title_tag.has_attr('content') else (title_tag.text.strip() if title_tag else "Produto Mercado Livre")

        # 2. Captura de Imagens (Alta Resolução - D_NQ_NP_)
        images = []
        og_image = soup.find('meta', property='og:image')
        if og_image and og_image.get('content'):
            images.append(og_image['content'])

        # Busca por galeria de imagens adicionais no HTML
        gallery_matches = re.findall(r'https://http2\.mlstatic\.com/D_NQ_NP_[0-9]+-[A-Z_]+-[0-9]+_[0-9]+-[FA]\.webp', html)
        for img in gallery_matches:
            # Converter webp de baixa para JPG de alta resolução se aplicável
            img_clean = img.replace('-F.webp', '-O.jpg').replace('-A.webp', '-O.jpg')
            if img_clean not in images:
                images.append(img_clean)

        # 3. Captura de Preço — prioriza JSON-LD (mais confiável), com
        # fallback para meta tags OpenGraph e, por último, regex no HTML.
        # CORREÇÃO (auditoria v2.1): a regex genérica '"price":\s*(...)'
        # sozinha podia capturar o preço de um produto relacionado exibido
        # na mesma página, não o produto principal.
        price_val = _extrair_preco_json_ld(soup)

        if not price_val:
            price_meta = soup.find('meta', property='product:price:amount') or soup.find('meta', itemprop='price')
            if price_meta and price_meta.get('content'):
                try:
                    price_val = float(price_meta['content'])
                except ValueError:
                    pass

        if not price_val:
            price_match = re.search(r'"price":\s*([0-9]+\.?[0-9]*)', html)
            if price_match:
                price_val = float(price_match.group(1))

        # 4. Captura de Vídeos (se houver)
        videos = list(set(re.findall(r'https://[a-zA-Z0-9\.-]+\.mlstatic\.com/video/[a-zA-Z0-9_-]+\.mp4', html)))

        return {
            "origem": "mercado_livre",
            "raw_product_url": res.url,  # URL final expandida
            "title": title.replace(" | MercadoLivre", "").strip(),
            "category": "Geral",
            "price": {
                "original": price_val,
                "promotional": price_val,
                "currency": "BRL"
            },
            "media": {
                "raw_images": images[:5],
                "raw_videos": videos[:2]
            }
        }
    except requests.RequestException as e:
        print(f"[-] Erro ao minerar Mercado Livre: {e}")
        return None


def minerar_shopee(url):
    print(f"[+] Minerando Shopee: {url}")
    session = requests.Session()

    # 1. Extração do shopid e itemid
    match = re.search(r'i\.(\d+)\.(\d+)', url) or re.search(r'product/(\d+)/(\d+)', url)

    if match:
        shopid, itemid = match.group(1), match.group(2)
        api_url = f"https://shopee.com.br/api/v4/item/get?itemid={itemid}&shopid={shopid}"

        shopee_headers = {
            **HEADERS,
            "Referer": url,
            "X-Shopee-Language": "pt",
            "X-Requested-With": "XMLHttpRequest"
        }

        try:
            res = session.get(api_url, headers=shopee_headers, timeout=10)
            if res.status_code == 200:
                data = res.json().get('data', {})
                if data:
                    title = data.get('name', 'Produto Shopee')
                    price_raw = data.get('price', 0) / 100000.0

                    image_hashes = data.get('images', [])
                    images = [f"https://down-br.img.susercontent.com/file/{img_hash}" for img_hash in image_hashes]

                    videos = []
                    for vid in data.get('video_info_list', []):
                        v_url = vid.get('default_format', {}).get('url')
                        if v_url:
                            videos.append(v_url)

                    if images or videos:
                        return {
                            "origem": "shopee",
                            "raw_product_url": url,
                            "title": title,
                            "category": "Geral",
                            "price": {"original": price_raw, "promotional": price_raw, "currency": "BRL"},
                            "media": {
                                "raw_images": images[:5],
                                "raw_videos": videos[:2]
                            }
                        }
        except requests.RequestException as e:
            # Este endpoint não é a API oficial de afiliados, e sim um
            # endpoint interno da Shopee sujeito a bloqueio por anti-bot a
            # qualquer momento — por isso o fallback HTML abaixo existe.
            print(f"[-] Falha na requisição API Shopee ({e}). Alternando para Scraper HTML...")

    # Fallback HTML
    try:
        res = session.get(url, headers=HEADERS, timeout=10)
        if res.status_code != 200:
            return None

        html = res.text
        soup = BeautifulSoup(html, 'html.parser')

        images = list(set(re.findall(r'https://down-br\.img\.susercontent\.com/file/[a-zA-Z0-9_-]+', html)))
        videos = list(set(re.findall(r'https://[a-zA-Z0-9\.-]+\.(?:mp4|mov)', html)))

        title_tag = soup.find('meta', property='og:title')
        title = title_tag['content'] if title_tag else "Produto Shopee"
        price_val = _extrair_preco_json_ld(soup)

        return {
            "origem": "shopee",
            "raw_product_url": url,
            "title": title,
            "category": "Geral",
            "price": {"original": price_val, "promotional": price_val, "currency": "BRL"},
            "media": {
                "raw_images": images[:5],
                "raw_videos": videos[:2]
            }
        }
    except requests.RequestException as e:
        print(f"[-] Erro ao minerar HTML da Shopee: {e}")
        return None


def minerar_aliexpress(url):
    print(f"[+] Minerando AliExpress: {url}")
    try:
        res = requests.get(url, headers=HEADERS, timeout=10)
        if res.status_code != 200:
            return None

        html = res.text
        soup = BeautifulSoup(html, 'html.parser')

        images = list(set(re.findall(r'https://ae01\.alicdn\.com/kf/[a-zA-Z0-9_-]+\.jpg', html)))
        videos = list(set(re.findall(r'https://video\.aliexpress-media\.com/[a-zA-Z0-9_-]+\.mp4', html)))

        title_tag = soup.find('meta', property='og:title')
        title = title_tag['content'] if title_tag else "Produto AliExpress"
        price_val = _extrair_preco_json_ld(soup)

        return {
            "origem": "aliexpress",
            "raw_product_url": url,
            "title": title,
            "category": "Geral",
            "price": {"original": price_val, "promotional": price_val, "currency": "BRL"},
            "media": {
                "raw_images": images[:5],
                "raw_videos": videos[:2]
            }
        }
    except requests.RequestException as e:
        print(f"[-] Erro ao minerar AliExpress: {e}")
        return None


def executar_mineracao(product_url):
    payload = None
    url_lower = product_url.lower()

    if "shopee" in url_lower:
        payload = minerar_shopee(product_url)
    elif "aliexpress" in url_lower:
        payload = minerar_aliexpress(product_url)
    elif "mercadolivre" in url_lower or "mercadolibre" in url_lower or "ml.com" in url_lower:
        payload = minerar_mercado_livre(product_url)
    else:
        print("[-] URL de origem não suportada.")
        return

    # Pequeno atraso pós-mineração: reduz a chance de bloqueio por
    # anti-bot quando este script é chamado repetidamente em lote/cron.
    time.sleep(DELAY_ENTRE_REQUISICOES_SEGUNDOS)

    if payload:
        print("[+] Enviando dados para o Sub-Agente 1 (n8n)...")
        headers = {"x-webhook-secret": WEBHOOK_SHARED_SECRET} if WEBHOOK_SHARED_SECRET else {}
        try:
            res = requests.post(WEBHOOK_URL, json=payload, headers=headers, timeout=10)
            print(f"[+] Status do Webhook: {res.status_code}")
        except requests.RequestException as e:
            print(f"[-] Erro ao disparar para o Webhook: {e}")


if __name__ == "__main__":
    if len(sys.argv) > 1:
        target_url = sys.argv[1]
    else:
        target_url = "https://produto.mercadolivre.com.br/MLB-3567891234-escova-de-limpeza-eletrica.html"

    executar_mineracao(target_url)
