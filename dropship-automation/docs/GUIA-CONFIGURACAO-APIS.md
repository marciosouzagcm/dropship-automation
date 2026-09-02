Markdown

# Guia de Configuração de APIs e Tokens

Este guia cobre a obtenção de todas as credenciais usadas no arquivo `.env`. Preencha cada valor e reinicie os containers (`docker compose up -d --build`) após a edição.

---

## 0. Segurança Básica (Auditoria v2.1)

Antes de preencher qualquer chave de API, gere valores criptograficamente fortes para as variáveis abaixo:

```bash
REDIS_PASSWORD=$(openssl rand -hex 32)
WEBHOOK_SHARED_SECRET=$(openssl rand -hex 32)
TELEGRAM_WEBHOOK_SECRET_TOKEN=$(openssl rand -hex 32)
REDIS_PASSWORD: Garante autenticação no Redis na porta 6379, protegendo tokens do Mercado Livre e filas de processamento.

WEBHOOK_SHARED_SECRET: Protege os webhooks internos (novo-produto-minerado e conteudo-pronto-aprovacao) contra requisições não autorizadas.

TELEGRAM_WEBHOOK_SECRET_TOKEN: Utilizado na validação do Sub-Agente 3 para garantir que as chamadas no endpoint de callback venham estritamente da infraestrutura do Telegram.

1. Shopee Affiliate Open API
Acesse o Shopee Affiliate Portal e entre na sua conta.

Vá em Developer / API Access e solicite acesso à Open API.

Após aprovação, obtenha o App ID e App Key.

Preencha no .env:

Fragmento do código
SHOPEE_AFFILIATE_APP_ID=seu_app_id
SHOPEE_APP_KEY=sua_app_key
A assinatura (SHA256) é calculada dinamicamente pelo worker Python em worker/services/affiliate.py.

2. AliExpress Affiliate API (Portals)
Crie uma conta no AliExpress Affiliate Portal.

Em Manage Apps, crie uma aplicação para gerar o App Key e App Secret.

Anote seu Tracking ID.

Preencha no .env:

Fragmento do código
ALIEXPRESS_APP_KEY=sua_app_key
ALIEXPRESS_APP_SECRET=seu_app_secret
ALIEXPRESS_TRACKING_ID=seu_tracking_id
O cálculo da assinatura HMAC-SHA256 com sign_method é gerenciado automaticamente pelo worker.

3. Mercado Livre
3.1 Refresh Token (Cron de Renovação Automatizada)
O sub-workflow Cron - Refresh Token Mercado Livre (Redis) no Sub-Agente 1 mantém o access_token renovado continuamente. É necessário fornecer o refresh_token inicial:

Crie uma aplicação em Mercado Livre Developers.

Faça o fluxo de autorização OAuth 2.0 inicial via navegador para obter o primeiro refresh_token.

Preencha no .env:

Fragmento do código
MERCADO_LIVRE_CLIENT_ID=seu_client_id
MERCADO_LIVRE_CLIENT_SECRET=seu_client_secret
MERCADO_LIVRE_REFRESH_TOKEN=seu_refresh_token_inicial
A partir da primeira execução do Cron no n8n, os novos tokens passam a ser gravados e lidos diretamente do Redis na chave ml_auth_tokens.

3.2 Link de Afiliado (Fallback)
Caso a conta não possua acesso ao programa de afiliados fechado do Mercado Livre, o sistema utiliza uma camada de fallback via worker/services/affiliate.py, injetando parâmetros de rastreamento utm_source e sanitizando a URL do produto.

4. ElevenLabs (Sintetização de Voz / TTS)
Crie uma conta em elevenlabs.io.

Acesse Profile > API Keys e gere uma nova chave.

Selecione a voz desejada em Voice Library e copie o Voice ID.

Preencha no .env:

Fragmento do código
ELEVENLABS_API_KEY=sua_chave_api
ELEVENLABS_VOICE_ID=id_da_voz
TTS_PROVIDER=elevenlabs
5. OpenAI (Fallback TTS e Transcrição Whisper)
Obtenha a chave em platform.openai.com/api-keys.

Preencha no .env:

Fragmento do código
OPENAI_API_KEY=sua_chave_api
Usada automaticamente como fallback se a cota do ElevenLabs for excedida.

6. Telegram Bot & Webhook Callback
Crie o bot falando com o @BotFather no Telegram.

Copie o token HTTP API gerado:

Fragmento do código
TELEGRAM_BOT_TOKEN=123456789:ABC-seu-token
Para obter seu TELEGRAM_CHAT_ID pessoal (aprovação), envie uma mensagem ao bot e acesse:

Bash
curl -s "[https://api.telegram.org/bot$](https://api.telegram.org/bot$){TELEGRAM_BOT_TOKEN}/getUpdates"
Configure a credencial do tipo "Telegram API" no painel do n8n utilizando o mesmo token, nomeando-a como telegram_bot_credential.

Registre a URL do Cloudflare Tunnel no Webhook do Telegram executando o comando no seu terminal CMD/Bash:

Bash
curl -X POST "[https://api.telegram.org/bot$](https://api.telegram.org/bot$){TELEGRAM_BOT_TOKEN}/setWebhook" \
  -H "Content-Type: application/json" \
  -d "{\"url\": \"[https://SUA-URL-TUNNEL.trycloudflare.com/webhook/telegram-callback](https://SUA-URL-TUNNEL.trycloudflare.com/webhook/telegram-callback)\"}"
7. Instagram Graph API (Reels)
Conecte sua conta do Instagram Business a uma Página do Facebook.

Crie uma aplicação Business no Facebook Developers.

Adicione o produto Instagram Graph API e gere um Long-Lived User Access Token com as permissões instagram_content_publish, pages_show_list e instagram_basic.

Preencha no .env:

Fragmento do código
INSTAGRAM_GRAPH_API_TOKEN=seu_long_lived_token
INSTAGRAM_BUSINESS_ACCOUNT_ID=seu_instagram_account_id
PUBLIC_ASSETS_BASE_URL=[https://sua-url-publica-assets.com](https://sua-url-publica-assets.com)
8. TikTok Content Posting API
Habilite o produto Content Posting API no TikTok for Developers.

Configure as credenciais no .env:

Fragmento do código
TIKTOK_CONTENT_POSTING_TOKEN=seu_token_tiktok
O envio exige que os vídeos estejam acessíveis via a URL estipulada em PUBLIC_ASSETS_BASE_URL.

9. Cloudflare Tunnel (Exposição Segura de Webhooks)
Para túnel temporário/dev (sem conta): O container cloudflared gera uma URL pública no boot. Consulte via:

Bash
docker compose logs cloudflared | findstr /C:"trycloudflare.com"
Para produção (domínio fixo): Preencha CLOUDFLARE_TUNNEL_TOKEN no .env e configure a rota para apontar para o container do n8n na porta 5678.


---

### File: `README.md`

```markdown
# Pipeline de Automação de Dropshipping & Processamento de Mídia Multi-Agente

Sistema completo de mineração, edição automatizada de vídeo, moderação remota via Telegram e distribuição assíncrona para marketplaces (Mercado Livre, Shopee, AliExpress) e redes sociais (Instagram Reels, TikTok).

---

## 📸 Arquitetura do Sistema

```text
[ Scrapers / APIs ] ──> [ Sub-Agente 1: Mineração (n8n) ]
                                    │
                                    ▼ (LPUSH)
                           [ Redis: fila:jobs_mineracao ]
                                    │
                                    ▼ (BRPOP)
                       [ Worker Python: media_processor ]
                                    │
                                    ▼ (HTTP POST /webhook)
                       [ Sub-Agente 2: Aprovação (n8n) ]
                                    │
                                    ▼ (Mídia + Inline Buttons)
                          [ Telegram User (Celular) ]
                                    │
                         (Clique: Approve/Reject)
                                    │
                                    ▼
                     [ Sub-Agente 3: Callback (n8n) ]
                                    │
             ┌──────────────────────┴──────────────────────┐
             ▼                                             ▼
[ Redis: fila:aprovados ]                       [ Redis: fila:auditoria ]
             │
             ▼
[ Agentes de Postagem Final ]
🛠️ Tecnologias Utilizadas
Orquestração: n8n (Multi-Agente)

Backend Worker: Python 3.11 (FFmpeg, OpenCV, Pillow, MoviePy)

Mensageria & Cache: Redis (Filas assíncronas + OAuth Token Store)

Infraestrutura: Docker & Docker Compose

Túnel Seguro: Cloudflare Tunnel (cloudflared)

Integrações: Telegram Bot API, Mercado Livre OAuth2 API, Shopee Affiliate API, AliExpress Portals API, ElevenLabs TTS, OpenAI.

📁 Estrutura do Repositório
Plaintext
.
├── docker-compose.yml           # Definição dos containers (n8n, redis, worker, cloudflared)
├── .env.example                 # Modelo de variáveis de ambiente
├── README.md                    # Documentação principal
├── MANUAL-DO-PIPELINE.md        # Documentação detalhada dos nós e sub-agentes n8n
├── GUIA-CONFIGURACAO-APIS.md    # Guia passo a passo para obtenção de credenciais
├── worker/                      # Código fonte do worker Python (media_processor)
│   ├── main.py                  # Loop principal de consumo do Redis
│   ├── services/                # Editores de vídeo, TTS, legenda e afiliadores
│   └── requirements.txt         # Dependências Python
└── workflows/                   # JSONs dos workflows do n8n para importação
    ├── sub-agente-1-mineracao.json
    ├── sub-agente-2-aprovacao.json
    └── sub-agente-3-callback.json
🚀 Como Executar o Projeto
1. Pré-requisitos
Docker e Docker Compose instalados na máquina host.

Bot criado no Telegram via @BotFather.

2. Configuração de Variáveis de Ambiente
Copie o arquivo de exemplo e preencha suas chaves e senhas conforme instruído no GUIA-CONFIGURACAO-APIS.md:

Bash
cp .env.example .env
3. Inicialização dos Containers
Suba toda a pilha de serviços em segundo plano:

Bash
docker compose up -d --build
4. Importação dos Workflows no n8n
Acesse o n8n no navegador (http://localhost:5678).

Crie sua conta de administrador na primeira inicialização.

Importe os arquivos JSON contidos na pasta workflows/ referentes aos Sub-Agente 1, Sub-Agente 2 e Sub-Agente 3.

Ative a chave Active (Verde) em todos os workflows importados.

5. Registro do Webhook do Telegram
Identifique a URL gerada pelo Cloudflare Tunnel:

Bash
docker compose logs cloudflared | findstr /C:"trycloudflare.com"
Em seguida, registre a URL no Telegram executando o comando no seu terminal:

Bash
curl -X POST "[https://api.telegram.org/bot](https://api.telegram.org/bot)<SEU_TELEGRAM_BOT_TOKEN>/setWebhook" \
  -H "Content-Type: application/json" \
  -d "{\"url\": \"https://<SUA-URL-TUNNEL>[.trycloudflare.com/webhook/telegram-callback](https://.trycloudflare.com/webhook/telegram-callback)\"}"
🧪 Validação e Teste do Fluxo
Mineração: Execute o nó Ao Clicar em Executar no Sub-Agente 1. O job normalizado será inserido na fila:jobs_mineracao do Redis.

Processamento: O worker Python consumirá o item da fila, processará a mídia e chamará o webhook do Sub-Agente 2.

Moderação: O Sub-Agente 2 enviará o vídeo renderizado ao chat do Telegram com os botões ✅ Aprovar e ❌ Rejeitar.

Callback: Ao clicar no botão no celular, o Sub-Agente 3 capturará a requisição, responderá à API do Telegram para liberar a interface e moverá o ID do produto para a fila:aprovados no Redis.