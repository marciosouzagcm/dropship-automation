param()

if (-not $env:TELEGRAM_BOT_TOKEN) {
    Write-Error "TELEGRAM_BOT_TOKEN não está definido."
    exit 1
}

$webhookUrl = $env:WEBHOOK_URL
if (-not $webhookUrl -or $webhookUrl -eq "https://SEU_DOMINIO_PUBLICO" -or $webhookUrl -eq "https://SEU_DOMINIO_PUBLICO/webhook/telegram-callback") {
    Write-Error "WEBHOOK_URL não está configurado corretamente no .env"
    Write-Host "Exemplo: WEBHOOK_URL=https://seu-dominio-ou-ngrok/webhook/telegram-callback"
    exit 1
}

if (-not $webhookUrl.EndsWith("/webhook/telegram-callback")) {
    $webhookUrl = $webhookUrl.TrimEnd("/") + "/webhook/telegram-callback"
}

# CORREÇÃO (auditoria v2.1): mesma proteção via secret_token da versão bash
# (ver scripts/bash/setup-telegram-webhook.sh para detalhes).
$body = @{ url = $webhookUrl }
if ($env:TELEGRAM_WEBHOOK_SECRET_TOKEN) {
    $body["secret_token"] = $env:TELEGRAM_WEBHOOK_SECRET_TOKEN
} else {
    Write-Warning "TELEGRAM_WEBHOOK_SECRET_TOKEN não definido — registrando webhook sem secret_token (menos seguro)."
}

Invoke-RestMethod -Method Post -Uri "https://api.telegram.org/bot$($env:TELEGRAM_BOT_TOKEN)/setWebhook" -Body $body
