"""
import_n8n_workflow.py
-----------------------
Importa um workflow (arquivo .json exportado do n8n) para uma instância n8n
via API REST local, autenticando com usuário/senha.

CORREÇÃO DE SEGURANÇA (auditoria v2.1): este script substitui 5 arquivos
de debug que existiam soltos na raiz do projeto (import_workflow.py,
import_workflow_v2.py, import_workflow_v3.py, check_workflow_api.py,
tmp_login.py, tmp_n8n_login.py) — todos com variações do mesmo problema:
usuário e SENHA DO ADMIN DO N8N HARDCODED EM TEXTO PURO no código-fonte,
além de um caminho de arquivo específico do Windows de um único
desenvolvedor (C:\\Users\\DELL\\...). Como o repositório é público, essa
senha deve ser tratada como comprometida — troque-a em N8N_PASSWORD (.env)
e reinicie o container do n8n antes de usar este script.

Uso:
    export N8N_BASE_URL=http://localhost:5678       # opcional, padrão abaixo
    export N8N_ADMIN_EMAIL=admin@example.com
    export N8N_ADMIN_PASSWORD=sua_senha_atual
    python3 scripts/python/import_n8n_workflow.py workflows/sub-agente-1-mineracao/sub-agente-1-afiliados.json
"""
import argparse
import json
import os
import sys
import urllib.error
import urllib.request

DEFAULT_BASE_URL = "http://localhost:5678"


def _obter_sessao(base_url: str, email: str, senha: str) -> str:
    """Autentica na API REST do n8n e retorna o id de sessão."""
    login_req = urllib.request.Request(
        f"{base_url}/rest/login",
        data=json.dumps({"emailOrLdapLoginId": email, "password": senha}).encode(),
        headers={"Content-Type": "application/json"},
    )
    with urllib.request.urlopen(login_req, timeout=10) as resp:
        dados = json.load(resp)
    # Diferentes versões do n8n usam chaves distintas para o id de sessão;
    # tentamos as duas conhecidas em vez de manter scripts separados por versão.
    return dados["data"].get("id") or dados["data"].get("sessionId")


def importar_workflow(caminho_arquivo: str, base_url: str, email: str, senha: str) -> None:
    with open(caminho_arquivo, "r", encoding="utf-8") as f:
        workflow = json.load(f)

    sessao = _obter_sessao(base_url, email, senha)
    print(f"[+] Sessão obtida com sucesso em {base_url}")

    payload = {
        "name": workflow.get("name", "Imported Workflow"),
        "nodes": workflow.get("nodes", []),
        "connections": workflow.get("connections", {}),
        "active": workflow.get("active", False),
        "settings": workflow.get("settings", {}),
    }

    req = urllib.request.Request(
        f"{base_url}/rest/workflows",
        data=json.dumps(payload).encode(),
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {sessao}",
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            print(f"[+] STATUS {resp.status}")
            print(resp.read().decode())
    except urllib.error.HTTPError as exc:
        print(f"[-] HTTP {exc.code}")
        print(exc.read().decode())
        sys.exit(1)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Importa um workflow JSON para o n8n via API REST.")
    parser.add_argument("arquivo", help="Caminho do arquivo .json do workflow exportado do n8n")
    parser.add_argument("--base-url", default=os.getenv("N8N_BASE_URL", DEFAULT_BASE_URL))
    args = parser.parse_args()

    email_admin = os.getenv("N8N_ADMIN_EMAIL")
    senha_admin = os.getenv("N8N_ADMIN_PASSWORD")

    if not email_admin or not senha_admin:
        print(
            "[-] Defina N8N_ADMIN_EMAIL e N8N_ADMIN_PASSWORD como variáveis de "
            "ambiente antes de rodar este script (nunca hardcode credenciais)."
        )
        sys.exit(1)

    importar_workflow(args.arquivo, args.base_url, email_admin, senha_admin)
