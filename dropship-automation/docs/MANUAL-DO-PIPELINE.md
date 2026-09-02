# Manual do Pipeline de Automação e Processamento de Mídia
**Arquitetura:** Multi-Agente n8n + Worker Python (`media_processor`) + Redis

---

## Visão Geral da Arquitetura

O sistema opera em um modelo assíncrono baseado em filas (Producer/Consumer) via **Redis**, garantindo resiliência e desacoplamento de responsabilidades:

1. **Sub-Agente 1 (Mineração & Infra OAuth):** Recebe e padroniza dados brutos do produto, atribui um `product_id` único e insere a mensagem na fila `fila:jobs_mineracao`. Além disso, executa em background o gerenciamento dinâmico de tokens OAuth2 do Mercado Livre.
2. **Worker Python (`media_processor`):** Consome a mensagem de `fila:jobs_mineracao`, baixa o vídeo, realiza a edição/renderização de mídia, salva o ativo no volume local compartilhado e dispara o webhook para o Sub-Agente 2.
3. **Sub-Agente 2 (Aprovação):** Recebe a notificação do worker, monta a visualização com botões de ação e envia o preview via Telegram ao usuário.
4. **Sub-Agente 3 (Distribuição/Callback):** Escuta o evento de clique nos botões inline do Telegram (`approve:` ou `reject:`), responde imediatamente à requisição da API do Telegram (`answerCallbackQuery`) para liberar a interface do celular e grava o evento na `fila:aprovados` ou na `fila:auditoria` do Redis.

---

## Mapeamento Nó por Nó

### 🟢 Sub-Agente 1 - Mineração e Infraestrutura (`Sub-Agente 1 - Mineracao`)

#### 1. Fluxo de Mineração de Produtos
* **`Ao Clicar em Executar` (`manualTrigger`):** Permite o disparo manual de testes dentro da interface do n8n.
* **`Dados de Teste Manual` (`set`):** Injeta um objeto mock simulando a entrada bruta de um anúncio do Mercado Livre, Shopee ou AliExpress.
* **`Webhook: Novo Produto Minerado` (`webhook`):** Endpoint HTTP POST (`/webhook/novo-produto-minerado`) para recepção de requisições de scrapers externos.
* **`Normalizar Entrada + Gerar product_id` (`code`):**
  * Extrai a URL bruta e atribui um UUID v4 como `product_id` (caso não seja fornecido).
  * Auto-detecta a plataforma (`mercado_livre`, `shopee`, `aliexpress`) com base nos domínios na URL.
  * Normaliza preços, moedas e os arrays de mídia (`raw_images` e `raw_videos`).
* **`IF: Origem e URL Válidas?` (`if`):** Valida se a URL não está vazia e se a origem pertence ao conjunto de marketplaces suportados.
  * **Branch True:** Encaminha para o Redis.
  * **Branch False:** Encaminha para o nó de rejeição.
* **`Redis: Push fila:jobs_mineracao` (`redis`):** Realiza a operação `LPUSH` do JSON do job na lista `fila:jobs_mineracao` do Redis.
* **`Log: Entrada Inválida Rejeitada` (`code`):** Emite um log de aviso no console do n8n para jobs que não passaram nos critérios de validação de URL ou origem.

#### 2. Workflow de Infraestrutura: Refresh Token Mercado Livre (Cron)
* **`A cada 5 horas` (`scheduleTrigger`):** Gatilho temporal ajustado para renovar o token antes da expiração de 6 horas do Mercado Livre.
* **`Redis: Buscar Token Atual` (`redis`):** Lê a chave `ml_auth_tokens` no Redis. Usa a opção `neverError: true` para evitar travamentos caso a chave ainda não exista na primeira execução.
* **`Preparar Payload OAuth` (`code`):**
  * Recupera as credenciais de ambiente (`MERCADO_LIVRE_CLIENT_ID`, `MERCADO_LIVRE_CLIENT_SECRET`).
  * Tenta obter o `refresh_token` do JSON armazenado no Redis (`storedData.refresh_token`). Caso seja a primeira execução, utiliza a variável de ambiente `MERCADO_LIVRE_REFRESH_TOKEN` como fallback.
  * Cancela a execução com erro explícito se alguma credencial essencial estiver ausente.
* **`HTTP: Refresh Token ML` (`httpRequest`):** Faz requisição POST `form-urlencoded` para `https://api.mercadolibre.com/oauth/token` atualizando o par de tokens.
* **`IF: Refresh Bem-Sucedido?` (`if`):** Valida se o retorno HTTP contém um `access_token` válido.
  * **Branch True:** Direciona para persistência no Redis.
  * **Branch False:** Direciona para alerta de falha de segurança.
* **`Redis: Salvar Novos Tokens` (`redis`):** Armazena o JSON completo com `access_token`, `refresh_token`, `expires_in` e `updated_at` na chave `ml_auth_tokens` com TTL dinâmico (`Math.max((expires_in - 1800), 300)` segundos).
* **`Telegram: Alertar Falha no Refresh` (`telegram`):** Envia alerta emergencial ao administrador no Telegram em caso de falha na renovação OAuth (ex.: token revogado/inválido).

---

### 🟡 Sub-Agente 2 - Aprovação via Telegram (`Sub-Agente 2 - Aprovacao via Telegram`)
* **`Ao Clicar em Executar` (`manualTrigger`):** Gatilho de disparo manual.
* **`Dados de Teste Manual` (`code`):** Gera um objeto estruturado de teste simulando a saída processada pelo worker Python.
* **`Webhook: Conteúdo Pronto (do Worker)` (`webhook`):** Endpoint POST (`/webhook/conteudo-pronto-aprovacao`) chamado pelo container `media_processor` quando o vídeo é baixado/processado.
* **`Montar Legenda de Aprovação` (`code`):**
  * Monta a mensagem legível em Markdown contendo Título, Preço, Hook do Telegram e a flag `erroPipeline`.
  * Define os botões `inline_keyboard` com as ações `approve:<job_id>` e `reject:<job_id>`.
* **`Switch: Estado do Conteúdo` (`switch`):**
  * **Saída `erro`:** Ativada se `approval.status === 'ERROR'`. Encaminha para o nó de alerta de falhas.
  * **Saída `com_video`:** Ativada se `rendered_video_path` contém um caminho válido.
  * **Saída `sem_video` (Fallback):** Ativada quando não há vídeo renderizado, mas a cópia do anúncio é válida (postagens baseadas em texto/imagem).
* **`Ler Arquivo de Vídeo` (`readWriteFile`):** Carrega o arquivo `.mp4` do sistema de arquivos compartilhado entre containers Docker.
* **`Telegram: Enviar Vídeo + Botões` (`telegram`):** Envia o arquivo de vídeo como mídia nativa no chat configurado, contendo a legenda e o Inline Keyboard.
* **`Telegram: Enviar Texto + Botões (sem vídeo)` (`telegram`):** Envia apenas a mensagem de texto com os botões inline para jobs que não possuem arquivo de vídeo associado.
* **`Telegram: Alertar Erro de Pipeline` (`telegram`):** Notifica o administrador sobre eventuais falhas durante o processamento do Python no worker.
* **`Telegram: Alertar Falha ao Ler Vídeo` (`telegram`):** Notifica quando o worker informa um caminho de vídeo, mas o n8n não consegue acessar o arquivo no volume.

---

### 🔵 Sub-Agente 3 - Callback Telegram e Distribuição (`Sub-Agente 3 - Callback Telegram e Distribuicao`)
* **`Ao Clicar em Executar` (`manualTrigger`):** Gatilho manual de testes.
* **`Dados de Teste Manual` (`code`):** Injeta um mock seguro simulando uma chamada de callback do Telegram (`approve:job_teste_123`) para garantir testes manuais sem travar o canvas.
* **`Webhook: Telegram Callback` (`webhook`):** Endpoint POST (`/webhook/telegram-callback`) que recebe as atualizações (`callback_query`) do Telegram em produção.
* **`Parse Seguro do Callback` (`code`):**
  * Extrai os dados brutos do `callback_data` e faz o split resiliênte entre a ação (`approve` / `reject`) e o `job_id`.
  * Trata origens de execução reais (Webhook API) e testes manuais, gerando metadados como `aprovado_por` e `aprovado_em`.
* **`Switch: Ação do Callback` (`switch`):** Roteia a execução com base no prefixo da ação:
  * **Saída `approve`:** Aprovado pelo usuário.
  * **Saída `reject`:** Rejeitado pelo usuário.
  * **Saída `extra`:** Callbacks com formato desconhecido.
* **`Telegram: Confirmar Aprovação` (`telegram`):**
  * Executa a operação `answerCallbackQuery` utilizando a opção *Continue on fail*.
  * **Função principal:** Remove o ícone de carregamento do botão no celular do usuário e exibe o alerta pop-up: *"✅ Aprovado! Publicando nos canais configurados..."*.
* **`Redis: Push fila:aprovados` (`redis`):** Realiza o `LPUSH` com a estrutura JSON completa do produto aprovado na lista `fila:aprovados` para consumo pelos publicadores finais.
* **`Redis: Log Auditoria (aprovação)` (`redis`):** Persiste o registro do usuário que aprovou o item e a data/hora na lista `fila:auditoria`.
* **`Telegram: Confirmar Rejeição` (`telegram`):** Responde ao callback informando que o conteúdo foi descartado.
* **`Redis: Log Auditoria (rejeição)` (`redis`):** Persiste a rejeição na `fila:auditoria`.
* **`Telegram: Alertar Callback Desconhecido` (`telegram`):** Notifica caso chegue um callback que não siga o padrão do sistema.

---

## Estrutura de Filas e Chaves do Redis

| Nome da Chave / Fila | Tipo | Produtor | Consumidor | Descrição |
| :--- | :--- | :--- | :--- | :--- |
| **`fila:jobs_mineracao`** | List | Sub-Agente 1 (n8n) | Worker Python (`media_processor`) | Jobs minerados aguardando download e edição de vídeo. |
| **`fila:aprovados`** | List | Sub-Agente 3 (n8n) | Publisher Bot / Agente de Postagem | Jobs revisados e aprovados via Telegram, prontos para publicação externa. |
| **`fila:auditoria`** | List | Sub-Agente 3 (n8n) | Sistema de Logs / Dashboard | Histórico durável de decisões de aprovação e rejeição de conteúdo. |
| **`ml_auth_tokens`** | String / JSON | Sub-Agente 1 (Cron) | Worker Python / Scrapers ML | Armazena o par `access_token` e `refresh_token` do Mercado Livre com TTL automático. |