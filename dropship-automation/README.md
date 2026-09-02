Markdown
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

🚀 Como Executar o Projeto em Produção

1. Pré-requisitos
Docker e Docker Compose V2 instalados no ambiente de host/servidor.

Bot criado no Telegram via @BotFather.

Token e domínio/URL configurados no Cloudflare Tunnel.

2. Configuração das Variáveis de Ambiente
Copie o arquivo de modelo e preencha todas as credenciais seguindo as diretrizes do GUIA-CONFIGURACAO-APIS.md:

Bash
cp .env.example .env
Atenção: Certifique-se de definir os segredos de autenticação no .env antes da inicialização (REDIS_PASSWORD, WEBHOOK_SHARED_SECRET e TELEGRAM_WEBHOOK_SECRET_TOKEN).

3. Inicialização dos Containers
Suba a pilha inteira de microsserviços e garanta a compilação do container do worker Python:

Bash
docker compose up -d --build

4. Configuração e Ativação dos Workflows no n8n
Acesse o painel do n8n (ex.: http://localhost:5678 ou pela sua URL de domínio público).

Configure a credencial do tipo Telegram API utilizando o TELEGRAM_BOT_TOKEN.

Importe os arquivos da pasta workflows/:

sub-agente-1-mineracao.json

sub-agente-2-aprovacao.json

sub-agente-3-callback.json

Importante: Alterne a chave Active de todos os sub-agentes para o estado ativo (Verde/Ativado) para registrar os Webhooks em modo de produção.

5. Registro do Webhook de Callbacks do Telegram
Verifique a URL pública exposta pelo Cloudflare Tunnel:

Bash
docker compose logs cloudflared | grep -i "trycloudflare.com"
Substitua a URL pública no comando abaixo e registre o endpoint oficial do Sub-Agente 3:

Bash
curl -X POST "[https://api.telegram.org/bot](https://api.telegram.org/bot)<SEU_TELEGRAM_BOT_TOKEN>/setWebhook" \
  -H "Content-Type: application/json" \
  -d "{\"url\": \"https://<SUA-URL-TUNNEL>/webhook/telegram-callback\", \"secret_token\": \"<SEU_TELEGRAM_WEBHOOK_SECRET_TOKEN>\"}"

⚠️ Solução de Problemas em Produção (Troubleshooting)
A. O Vídeo não chega no Telegram no Sub-Agente 2
Causa: O worker Python não conseguiu acessar ou gerar o arquivo no volume compartilhado (/data/media).

Solução: Verifique as permissões de gravação da pasta montada no Docker Compose e analise os logs do worker:

Bash
docker compose logs -f worker
B. O botão do Telegram fica carregando infinitamente ao clicar
Causa: O Sub-Agente 3 não enviou a resposta de answerCallbackQuery dentro da janela de tempo exigida pela API do Telegram (10 segundos) ou o webhook está inativo no n8n.

Solução: Confirme se o workflow do Sub-Agente 3 está Ativado no n8n e se o parâmetro TELEGRAM_WEBHOOK_SECRET_TOKEN no cabeçalho condiz com o cadastrado no Telegram.

C. Conexão Recusada no Redis
Causa: Tentativa de conexão sem autenticação de senha ou nome do host incorreto no worker Python.

Solução: No ambiente Docker, o nome do host do Redis deve ser redis (nome do serviço) e a senha precisa corresponder ao REDIS_PASSWORD do .env.