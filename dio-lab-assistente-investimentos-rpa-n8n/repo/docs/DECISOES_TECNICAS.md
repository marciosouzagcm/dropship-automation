# Decisões Técnicas

## Fluxo
`index.html` → **RPA (Python)** → `POST /webhook/clientes` → **N8N**: Webhook → HTTP Request (`data.csv`) → Code (cruzamento) → [Agente de IA] → Respond to Webhook.

## Por que cada escolha

| Decisão | Motivo |
|---|---|
| **Um único POST com todos os clientes** (`{"clientes": [...]}`) | Uma execução do workflow por lote; o CSV é baixado uma vez só, e não uma vez por cliente. |
| **Resposta síncrona** (`Respond to Webhook`) | O script Python recebe as mensagens geradas e pode imprimi-las/gravá-las, o que facilita testar e demonstrar o fluxo ponta a ponta. |
| **CSV lido por HTTP Request + parse no nó Code** | O CSV é minúsculo e sem aspas/vírgulas internas; evita depender de nós extras e deixa a regra de negócio em um só lugar, legível e testável fora do N8N (`n8n/code/`). |
| **Cruzamento por perfil *e* saldo** | Recomendar um produto com mínimo acima do saldo seria uma má recomendação. Só entram opções com `minimo <= saldo`; se nenhuma serve, a mensagem indica a opção mais acessível. |
| **Saldo convertido de "R$ 12.500,00" para número** | Permite comparar com o mínimo; a página original só traz texto. |
| **MVP: templates por perfil** | Conservador (segurança/renda fixa), Moderado (mix), Arrojado (ações/retorno), conforme o enunciado. Determinístico e auditável. |
| **IA: Agente de IA + Google Gemini** | Modelo com camada gratuita, o que mantém o projeto acessível. O Code node monta o contexto (perfil, saldo, opções elegíveis) e o agente só redige o texto. |
| **IA só recebe opções do CSV** | O *system prompt* proíbe inventar produtos, taxas ou garantir retorno e exige mencionar risco em renda variável. Em finanças, a IA redige; os fatos vêm dos dados. |
| **Pré-filtro de elegibilidade antes da IA** | A regra de negócio fica em código (confiável); o LLM não precisa "decidir" elegibilidade. |
| **Credencial do Gemini fora do JSON** | O workflow exportado traz apenas um *placeholder*; a chave é configurada no N8N. Nunca versionar chaves. |
| **Script com `--dry-run`, `--arquivo`, variáveis de ambiente** | Permite testar a extração sem N8N e sem internet; a URL do webhook não fica hardcoded. |

## Limitações conhecidas
- Webhook de teste (`/webhook-test/`) só aceita uma chamada por vez após "Listen"; para uso contínuo, ative o workflow e use `/webhook/`.
- O script não envia e-mails; a saída são as mensagens. Um nó Gmail/SMTP após o Code (ou Agente) completaria o envio.
- Mensagens de IA devem ser revisadas antes de uso real (compliance/suitability).
- Falhas de LLM em um cliente interrompem o lote; poderia-se ativar *Continue on Fail* no agente.
