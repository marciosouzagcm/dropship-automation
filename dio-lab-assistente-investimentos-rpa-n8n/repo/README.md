# Criando um Assistente de Investimentos com RPA e IA Generativa

## Descrição

Aprenda na prática como criar um fluxo de automação inteligente combinando técnicas de RPA (Robotic Process Automation) com workflows de IA no N8N.

Neste desafio, você vai construir um assistente de investimentos automatizado. O fluxo começa com a extração de dados de clientes em uma página web usando Python, passa pela orquestração de um workflow no N8N e termina com a geração de mensagens personalizadas para cada perfil de investidor.

O projeto foi pensado para ser simples e acessível, mesmo para quem está dando os primeiros passos em Python e automação. A ideia é que você entenda o conceito de RPA de forma leve e aplique tudo em um cenário realista do mercado financeiro.

## Objetivo do Projeto

Desenvolver um pipeline de automação que:

1. **Coleta dados de clientes** de uma página web simulada usando Python
2. **Processa as informações** através de um workflow no N8N
3. **Cruza perfis de investidor** com uma base de opções de investimento
4. **Gera mensagens personalizadas** para cada cliente

Ao final, você terá um sistema funcional que demonstra como empresas do setor financeiro podem automatizar a comunicação com clientes de forma inteligente.

## Arquitetura do Projeto

```mermaid
flowchart LR
  %% Pipeline RPA + N8N + IA (máx. 7 caixinhas)

  subgraph GH["GitHub Pages"]
    A["Clientes<br>(docs/index.html)"]
    E["Investimentos (docs/data.csv)"]
  end

  subgraph PY["RPA (Python)"]
    B["Extrair Clientes"]
  end

  subgraph N8["N8N (Workflow)"]
    C["Webhook<br>(Entrada)"]
    D["Cruzar Dados<br>(Clientes x Investimentos)"]
    M["Gerar Mensagem<br>(Template/LLM)"]
    C --> D --> M
  end

  subgraph OUT["Saída"]
    O["Mensagens Personalizadas"]
  end

  A <-->|HTTP| B --> C
  E <-->|HTTP| D
  M --> O

  %% Estilos
  classDef source fill:#E3F2FD,stroke:#1E88E5,stroke-width:1px,color:#0D47A1;
  classDef rpa fill:#E8F5E9,stroke:#43A047,stroke-width:1px,color:#1B5E20;
  classDef n8n fill:#FFF3E0,stroke:#FB8C00,stroke-width:1px,color:#E65100;
  classDef out fill:#FCE4EC,stroke:#D81B60,stroke-width:1px,color:#880E4F;

  class A,E source;
  class B rpa;
  class C,D,M n8n;
  class O out;

```

## Tecnologias e Ferramentas

O projeto utiliza ferramentas gratuitas e acessíveis, organizadas conforme cada etapa do fluxo:

| Etapa | Ferramenta | Função |
| ----- | ---------- | ------ |
| Hospedagem | GitHub Pages | Servir a página de clientes e o CSV de investimentos |
| Extração (RPA) | Python + BeautifulSoup | Coletar dados dos clientes via web scraping |
| Orquestração | N8N | Processar dados, cruzar perfis e gerar mensagens |
| Geração com IA | Agente de IA no N8N | Criar mensagens personalizadas com LLM (desafio extra) |

Além dessas, você pode usar IAs generativas como **Gemini**, **Claude** ou **ChatGPT** como copilotos para auxiliar na escrita de código e tirar dúvidas ao longo do desenvolvimento.

## Roteiro do Desafio

### Etapa 1: Entenda o Projeto

Antes de começar, explore o repositório base que já contém a estrutura inicial:

1. **Página de Clientes (`docs/index.html`):** Uma página HTML hospedada no GitHub Pages com uma lista de clientes fictícios contendo nome, email, saldo e perfil de investidor (Conservador, Moderado ou Arrojado). Disponível online [neste link](https://digitalinnovationone.github.io/dio-lab-assistente-investimentos-rpa-n8n).
2. **Dados de Investimentos (`docs/data.csv`):** Um arquivo CSV também hospedado no GitHub Pages com opções de investimento organizadas por perfil. Disponível online [neste link](https://digitalinnovationone.github.io/dio-lab-assistente-investimentos-rpa-n8n/data.csv).
3. **Script de RPA (`rpa/extrair_clientes.ipynb`):** Um notebook Python que acessa a página de clientes e extrai os dados da tabela usando BeautifulSoup.

> 🤖 **Por que o script é considerado RPA?** Ele faz exatamente o que um humano faria manualmente: abre uma página, lê os dados de uma tabela e os envia para outro sistema. A diferença é que o "robô" (código) executa isso automaticamente. Essa abordagem é útil quando não existe uma API disponível ou quando precisamos integrar sistemas legados.

### Etapa 2: Configure o Ambiente

1. Faça um **fork** do repositório base para sua conta do GitHub
2. Crie uma conta no [N8N Cloud](https://n8n.io/) ou instale localmente
3. Abra o notebook `rpa/extrair_clientes.ipynb` no [Google Colab](https://colab.research.google.com/) e execute para entender o fluxo de extração

> 💡 **Atenção:** O script já extrai os dados, mas o envio ao N8N está comentado (`TODO`). Você vai configurar a URL do Webhook após criá-lo na próxima etapa.

### Etapa 3: Desenvolva o Workflow no N8N

Este é o coração do desafio! Monte um fluxo que:

1. Receba os dados dos clientes via Webhook (copie a URL gerada e configure no script Python)
2. Leia o arquivo `docs/data.csv` com as opções de investimento
3. Cruze o perfil de cada cliente com a opção adequada
4. Gere uma mensagem de recomendação para cada cliente

### Etapa 4 (MVP): Mensagens Estáticas

Para a versão mínima, use templates de mensagem fixos baseados no perfil:

- **Conservador:** Foco em renda fixa e segurança
- **Moderado:** Mix equilibrado entre renda fixa e variável
- **Arrojado:** Ênfase em ações e maior potencial de retorno

### Etapa 5 (Desafio): Integração com IA Generativa

Conecte o Agente de IA do N8N a um modelo como Gemini ou GPT para:

- Analisar o contexto do cliente (saldo, perfil)
- Gerar mensagens únicas e personalizadas
- Criar recomendações mais inteligentes e humanizadas

## Entregáveis

### MVP (Mínimo Viável)

- [x] Repositório forkado com o workflow N8N implementado
- [x] Workflow N8N exportado (`n8n/workflow.json`) com mensagens estáticas
- [x] Script de RPA integrado ao Webhook do N8N (`rpa/extrair_clientes.py`)
- [ ] Print ou vídeo demonstrando o fluxo funcionando de ponta a ponta (grave após importar o workflow no seu N8N)

### Desafio Completo

- [x] Todos os itens do MVP
- [x] Integração com Agente de IA no N8N (`n8n/workflow_ia.json`)
- [x] Mensagens geradas dinamicamente via LLM (Google Gemini)
- [x] Documentação explicando as decisões técnicas (`docs/DECISOES_TECNICAS.md`)

## Estrutura do Repositório

```text
📁 dio-lab-assistente-investimentos-rpa-n8n/
├── 📄 README.md
├── 📄 requirements.txt
├── 📁 rpa/
│   ├── 📄 extrair_clientes.ipynb   # Notebook original (exploração)
│   └── 📄 extrair_clientes.py      # Script RPA: extrai e envia ao Webhook do N8N
├── 📁 n8n/
│   ├── 📄 workflow.json            # MVP: mensagens estáticas
│   ├── 📄 workflow_ia.json         # Desafio: Agente de IA (Gemini)
│   ├── 📄 workflow_completo.json   # Merge + OpenAI + validação de e-mail + envio (Gmail)
│   └── 📁 code/                    # Código dos nós Code, para leitura/revisão
└── 📁 docs/
    ├── 📄 index.html               # Página de clientes
    ├── 📄 data.csv                 # Opções de investimento
    └── 📄 DECISOES_TECNICAS.md
```

## Como executar

### 1. Disponibilizar os dados

No Docker, o serviço `docs` publica `docs/index.html` e `docs/data.csv` na porta 8080 do computador e em `http://docs/` dentro da rede Docker. Assim, o workflow não depende de configurar GitHub Pages.

### 2. Iniciar o N8N e o RPA com Docker (opcional)

Instale e inicie o Docker Desktop. Na pasta raiz do repositório, copie `.env.example` para `.env`. Inicie o N8N e o servidor local dos dados:

```bash
docker compose up -d n8n docs
```

Abra [http://localhost:5679](http://localhost:5679) e entre na instância deste projeto (a porta 5678 pode estar ocupada por outro N8N). O N8N mantém workflows, credenciais e configurações no volume `n8n_data`. Os arquivos locais ficam disponíveis em [http://localhost:8080](http://localhost:8080).

Para conferir a extração local sem enviar dados ao webhook:

```bash
docker compose run --rm rpa --arquivo docs/index.html --dry-run
```

### 3. Importar o workflow no N8N

1. No N8N deste projeto (`http://localhost:5679`): **Workflows → Import from File** → `n8n/workflow.json` (MVP) ou `n8n/workflow_ia.json` (IA).
2. O nó **Ler data.csv** já aponta para `http://docs/data.csv`, acessível pela rede Docker.
3. Só na versão IA: no nó **Google Gemini Chat Model**, crie a credencial com sua chave gratuita do [Google AI Studio](https://aistudio.google.com/apikey).
4. Abra o nó **Webhook** e clique em **Listen for test event** para testar. Para uso contínuo, salve e ative o workflow.

### 4. Rodar o robô de RPA

O workflow só executa quando recebe um POST no webhook. Com o workflow aberto, clique em **Listen for test event**. Enquanto ele estiver ouvindo, execute em outro terminal:

```bash
docker compose run --rm rpa --webhook http://n8n:5678/webhook-test/clientes
```

Depois de ativar o workflow, use a URL de produção:

```bash
docker compose run --rm rpa --webhook http://n8n:5678/webhook/clientes
```

O serviço RPA é executado sob demanda e não inicia automaticamente com `docker compose up`. A URL `http://n8n:5678/...` usa o nome interno do serviço na rede Docker; não substitua por `localhost` ao executar o RPA em um contêiner.

Para executar o RPA diretamente no computador, sem Docker:

```bash
pip install -r requirements.txt

# conferir a extração sem enviar nada
python rpa/extrair_clientes.py --dry-run

# extrair e enviar para o N8N
export N8N_WEBHOOK_URL="http://localhost:5679/webhook-test/clientes"
python rpa/extrair_clientes.py
```

No Windows PowerShell, defina a variável com `$env:N8N_WEBHOOK_URL="http://localhost:5679/webhook-test/clientes"`. Sem o servidor local, use `--arquivo docs/index.html` para extrair do HTML diretamente.

O N8N responde com uma lista de objetos `{nome, email, perfil, saldo, mensagem}`, um por cliente.

### Workflow completo (`n8n/workflow_completo.json`)

Fluxo: Webhook → Ler data.csv → Code (clientes) e Code (investimentos) → Merge por `perfil` → Code (normalizar tabela) → Message a model (OpenAI) → Code (regras do e-mail) → IF e-mail válido → Send a message (Gmail) → Merge → Responder ao Webhook.

O webhook é `/webhook/clientes-completo`. Configure as credenciais **OpenAI** e **Gmail** no n8n. Sem a credencial da OpenAI, o fluxo usa uma mensagem padrão; sem a do Gmail, o envio falha sem interromper a resposta.

```bash
docker compose run --rm rpa --webhook http://n8n:5678/webhook/clientes-completo
```

### Exemplo de saída (MVP)

> Olá, Bruno! Com seu perfil Moderado e saldo de R$ 3.200,00, sugerimos uma estratégia com mix equilibrado entre renda fixa e renda variável. Opções disponíveis para você: CDB Prefixado (mín. R$ 1.000,00, rentab. 14.0%); Tesouro IPCA+ (mín. R$ 1.000,00, rentab. IPCA+6.0%).

## Prompts Úteis para Copilotos de IA

| Tarefa | Sugestão de Prompt |
| ------ | ------------------- |
| Gerar dados fictícios | "Crie 10 clientes fictícios com nome, email, saldo e perfil de investidor em JSON" |
| Entender código | "Explique o que faz a biblioteca BeautifulSoup em Python" |
| Debugar erros | "Meu script Python está dando erro X, o que pode ser?" |
| Montar workflow | "Como configuro um webhook no N8N para receber dados JSON?" |

## Referências

- [Documentação do N8N](https://docs.n8n.io/)
- [BeautifulSoup: Web Scraping com Python](https://realpython.com/beautiful-soup-web-scraper-python/)
- [GitHub Pages: Guia Rápido](https://pages.github.com/)

---

**Bons estudos e mãos à obra** 🚀

Se tiver dúvidas, lembre-se: a melhor forma de aprender é experimentando. Erre, corrija e celebre cada pequena vitória no caminho.
