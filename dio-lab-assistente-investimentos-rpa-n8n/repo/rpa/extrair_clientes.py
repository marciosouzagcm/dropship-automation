"""RPA: extrai clientes da página HTML e envia para o Webhook do N8N.

Uso:
    # Só extrair e mostrar (não envia nada)
    python rpa/extrair_clientes.py --dry-run

    # Extrair da página publicada e enviar ao N8N
    export N8N_WEBHOOK_URL="https://SEU_N8N/webhook/clientes"
    python rpa/extrair_clientes.py

    # Extrair de um HTML local (útil para testes offline)
    python rpa/extrair_clientes.py --arquivo docs/index.html --dry-run
"""
import argparse
import json
import os
import sys

import requests
from bs4 import BeautifulSoup

URL_PAGINA = os.getenv(
    "URL_PAGINA",
    "http://localhost:8080/",
)
N8N_WEBHOOK_URL = os.getenv("N8N_WEBHOOK_URL", "")


def baixar_html(url: str) -> str:
    resposta = requests.get(url, timeout=30)
    resposta.raise_for_status()
    resposta.encoding = "utf-8"
    return resposta.text


def extrair_clientes(html: str) -> list[dict]:
    soup = BeautifulSoup(html, "html.parser")
    clientes = []
    for linha in soup.select("#clientes tbody tr"):
        colunas = linha.find_all("td")
        if len(colunas) < 4:
            continue  # ignora linhas malformadas
        clientes.append(
            {
                "nome": colunas[0].get_text(strip=True),
                "email": colunas[1].get_text(strip=True),
                "saldo": colunas[2].get_text(strip=True),
                "perfil": colunas[3].get_text(strip=True),
            }
        )
    return clientes


def enviar_para_n8n(clientes: list[dict], webhook_url: str):
    resposta = requests.post(webhook_url, json={"clientes": clientes}, timeout=120)
    resposta.raise_for_status()
    return resposta.json()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--arquivo", help="HTML local em vez de baixar da URL")
    parser.add_argument("--url", default=URL_PAGINA, help="URL da página de clientes")
    parser.add_argument("--webhook", default=N8N_WEBHOOK_URL, help="URL do Webhook do N8N")
    parser.add_argument("--dry-run", action="store_true", help="só extrai e imprime, sem enviar")
    args = parser.parse_args()

    if args.arquivo:
        with open(args.arquivo, encoding="utf-8") as f:
            html = f.read()
    else:
        html = baixar_html(args.url)

    clientes = extrair_clientes(html)
    print(f"{len(clientes)} clientes encontrados:")
    for c in clientes:
        print(" ", c)

    if args.dry_run:
        return 0
    if not args.webhook:
        print("\nDefina N8N_WEBHOOK_URL (ou --webhook) para enviar, ou use --dry-run.", file=sys.stderr)
        return 1

    resultado = enviar_para_n8n(clientes, args.webhook)
    print("\nResposta do N8N:")
    print(json.dumps(resultado, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    sys.exit(main())
