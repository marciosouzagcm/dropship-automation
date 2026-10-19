// N8N Code node (Run Once for All Items) — versão com IA.
// Mesmo cruzamento da versão estática, mas em vez da mensagem pronta
// devolve um "prompt" com o contexto do cliente para o Agente de IA.

const clientes = $('Webhook').first().json.body.clientes || [];
const csv = $input.first().json.data;

const [cabecalho, ...linhas] = csv.trim().split(/\r?\n/);
const colunas = cabecalho.split(',').map(c => c.trim());
const opcoes = linhas.filter(Boolean).map(l => {
  const v = l.split(',');
  const o = {};
  colunas.forEach((c, i) => (o[c] = (v[i] || '').trim()));
  o.minimo = Number(o.minimo);
  return o;
});

const parseSaldo = s => Number(String(s).replace(/[^\d,]/g, '').replace(',', '.'));
const brl = n => 'R$ ' + n.toFixed(2).replace('.', ',').replace(/\B(?=(\d{3})+(?!\d))/g, '.');

return clientes.map(c => {
  const saldo = parseSaldo(c.saldo);
  const doPerfil = opcoes.filter(o => o.perfil === c.perfil);
  const elegiveis = doPerfil.filter(o => o.minimo <= saldo);
  const lista = (elegiveis.length ? elegiveis : doPerfil)
    .map(o => `- ${o.produto}: mínimo ${brl(o.minimo)}, rentabilidade ${o.rentabilidade}`)
    .join('\n');

  const prompt =
    `Cliente: ${c.nome}\n` +
    `Perfil de investidor: ${c.perfil}\n` +
    `Saldo disponível: ${brl(saldo)}\n` +
    `Situação: ${elegiveis.length ? 'saldo cobre o mínimo de ' + elegiveis.length + ' opção(ões)' : 'saldo abaixo do mínimo de todas as opções do perfil'}\n\n` +
    `Opções de investimento do perfil:\n${lista}`;

  return {
    json: {
      nome: c.nome,
      email: c.email,
      perfil: c.perfil,
      saldo,
      opcoes_elegiveis: elegiveis.map(o => o.produto),
      prompt,
    },
  };
});
