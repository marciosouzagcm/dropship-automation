"""
config.py
---------
Ponto único de leitura de configuração do worker. Todos os módulos em
/worker/services devem importar as constantes daqui em vez de ler
os.getenv diretamente — facilita testes e evita chaves espalhadas pelo código.
"""
import os

from dotenv import load_dotenv

load_dotenv()  # carrega o .env quando executado fora do container (dev local)


def _env(key: str, default: str = "") -> str:
    """Leitura segura de variável de ambiente, nunca retorna None (evita
    TypeError mais adiante em .startswith()/.format() etc.)."""
    return os.getenv(key, default) or default


# --- Redis / filas ---
REDIS_HOST = _env("REDIS_HOST", "redis")
REDIS_PORT = int(_env("REDIS_PORT", "6379"))
# CORREÇÃO (auditoria de segurança): o Redis não tinha senha configurada e
# ficava exposto na porta do host sem autenticação. Se REDIS_PASSWORD não
# for definido, a conexão continua sem senha (compatível com ambientes de
# dev), mas em produção esta variável deve SEMPRE estar preenchida — ver
# docker-compose.yml (comando `--requirepass`) e docs/GUIA-CONFIGURACAO-APIS.md.
REDIS_PASSWORD = _env("REDIS_PASSWORD") or None

FILA_JOBS_MINERACAO = "fila:jobs_mineracao"        # Sub-Agente 1 -> Worker
FILA_JOBS_VIDEO = "fila:jobs_video"                # n8n -> Worker (renderização, legado)
FILA_APROVACAO = "fila:aguardando_aprovacao"       # Worker -> histórico/auditoria (ver nota abaixo)
FILA_DISTRIBUICAO = "fila:aprovados"               # Sub-Agente 3 -> Worker/Publicação
FILA_AUDITORIA = "fila:auditoria"                  # Registro durável de aprovações/rejeições

# --- IA Generativa (scripts/hooks) ---
GEMINI_API_KEY = _env("GEMINI_API_KEY")
ANTHROPIC_API_KEY = _env("ANTHROPIC_API_KEY")

# --- TTS / Legendagem ---
ELEVENLABS_API_KEY = _env("ELEVENLABS_API_KEY")
ELEVENLABS_VOICE_ID = _env("ELEVENLABS_VOICE_ID", "21m00Tcm4TlvDq8ikWAM")  # voz padrão "Rachel"
OPENAI_API_KEY = _env("OPENAI_API_KEY")
TTS_PROVIDER = _env("TTS_PROVIDER", "elevenlabs")  # "elevenlabs" | "openai"
WHISPER_MODEL_SIZE = _env("WHISPER_MODEL_SIZE", "base")  # tiny|base|small|medium|large

# --- Afiliados ---
SHOPEE_APP_ID = _env("SHOPEE_AFFILIATE_APP_ID")
SHOPEE_APP_KEY = _env("SHOPEE_APP_KEY")
ALIEXPRESS_APP_KEY = _env("ALIEXPRESS_APP_KEY")
ALIEXPRESS_APP_SECRET = _env("ALIEXPRESS_APP_SECRET")
ALIEXPRESS_TRACKING_ID = _env("ALIEXPRESS_TRACKING_ID")
MERCADO_LIVRE_CLIENT_ID = _env("MERCADO_LIVRE_CLIENT_ID")
MERCADO_LIVRE_CLIENT_SECRET = _env("MERCADO_LIVRE_CLIENT_SECRET")

# --- Distribuição / redes sociais ---
TELEGRAM_BOT_TOKEN = _env("TELEGRAM_BOT_TOKEN")
TELEGRAM_CHAT_ID = _env("TELEGRAM_CHAT_ID")
TELEGRAM_CHANNEL_ID = _env("TELEGRAM_CHANNEL_ID")  # canal público de vendas (opcional)
INSTAGRAM_GRAPH_API_TOKEN = _env("INSTAGRAM_GRAPH_API_TOKEN")
INSTAGRAM_BUSINESS_ACCOUNT_ID = _env("INSTAGRAM_BUSINESS_ACCOUNT_ID")
TIKTOK_CONTENT_POSTING_TOKEN = _env("TIKTOK_CONTENT_POSTING_TOKEN")

# --- n8n (o worker chama o n8n de volta via webhook após processar cada job) ---
N8N_WEBHOOK_URL = _env("N8N_WEBHOOK_URL", "http://n8n_orchestrator:5678").rstrip("/")
# CORREÇÃO CRÍTICA: antes o worker apenas dava RPUSH em FILA_APROVACAO, mas
# nenhum node do n8n consumia essa fila — o Sub-Agente 2 só é acionado por
# webhook HTTP. Sem esta chamada, nenhum conteúdo chegava ao Telegram.
N8N_WEBHOOK_APROVACAO_PATH = _env("N8N_WEBHOOK_APROVACAO_PATH", "/webhook/conteudo-pronto-aprovacao")

# Segredo enviado no header x-webhook-secret para os webhooks internos do
# n8n (novo-produto-minerado, conteudo-pronto-aprovacao) — ver nota de
# segurança em .env.example. Sem isso, esses endpoints ficavam abertos
# para qualquer requisição externa.
WEBHOOK_SHARED_SECRET = _env("WEBHOOK_SHARED_SECRET")

# --- Publicação de mídia (Instagram Graph API / TikTok exigem URL pública) ---
# Caminho local (/data/assets/processed/...) não é aceito por essas APIs.
# Configure esta variável apontando para onde `assets/processed` é servido
# publicamente (Cloudflare Tunnel com rota dedicada, bucket S3/R2 com CDN,
# ou um Nginx simples na frente do volume). Deixe em branco para desativar
# a publicação em Instagram/TikTok com um aviso claro em vez de uma
# tentativa silenciosa e fadada a falhar.
PUBLIC_ASSETS_BASE_URL = _env("PUBLIC_ASSETS_BASE_URL").rstrip("/")

# --- Caminhos de trabalho (montados via volume no docker-compose) ---
ASSETS_DIR = _env("ASSETS_DIR", "/data/assets")
RAW_DIR = f"{ASSETS_DIR}/raw"
PROCESSED_DIR = f"{ASSETS_DIR}/processed"
AUDIO_DIR = f"{ASSETS_DIR}/audio"
THUMBS_DIR = f"{ASSETS_DIR}/thumbnails"
CAPTIONS_DIR = f"{ASSETS_DIR}/captions"
