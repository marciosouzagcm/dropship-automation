#!/usr/bin/env bash
set -euo pipefail

if [ -z "${TELEGRAM_BOT_TOKEN:-}" ]; then
  echo "TELEGRAM_BOT_TOKEN não está definido."
  exit 1
fi

if [ -z "${WEBHOOK_URL:-}" ] || [[ "${WEBHOOK_URL}" == "https://SEU_DOMINIO_PUBLICO" ]] || [[ "${WEBHOOK_URL}" == "https://SEU_DOMINIO_PUBLICO/webhook/telegram-callback" ]]; then
  echo "WEBHOOK_URL não está configurado corretamente no .env"
  echo "Exemplo: WEBHOOK_URL=https://seu-dominio-ou-ngrok/webhook/telegram-callback"
  exit 1
fi

if [[ "${WEBHOOK_URL}" != */webhook/telegram-callback ]]; then
  WEBHOOK_URL="${WEBHOOK_URL%/}/webhook/telegram-callback"
fi

# CORREÇÃO (auditoria v2.1): registra o secret_token junto ao setWebhook.
# O Telegram passa a reenviar esse mesmo valor no header
# X-Telegram-Bot-Api-Secret-Token em toda chamada de callback, permitindo
# ao Sub-Agente 3 (node "Parse Seguro do Callback") confirmar que a
# requisição realmente veio do Telegram e não de terceiros que descobriram
# a URL do webhook. Sem TELEGRAM_WEBHOOK_SECRET_TOKEN definido, o webhook
# ainda é registrado (compatibilidade), mas sem essa proteção extra.
if [ -n "${TELEGRAM_WEBHOOK_SECRET_TOKEN:-}" ]; then
  curl -sS -X POST "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/setWebhook" \
    -d "url=${WEBHOOK_URL}" \
    -d "secret_token=${TELEGRAM_WEBHOOK_SECRET_TOKEN}"
else
  echo "[!] TELEGRAM_WEBHOOK_SECRET_TOKEN não definido — registrando webhook sem secret_token (menos seguro)."
  curl -sS -X POST "https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/setWebhook" \
    -d "url=${WEBHOOK_URL}"
fi
