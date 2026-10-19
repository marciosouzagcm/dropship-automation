// N8N Code node (Run Once for All Items) — versão MVP com mensagens estáticas.
// Entrada: resposta do HTTP Request com o conteúdo de data.csv (campo "data").
// Os clientes vêm do nó Webhook (body.clientes).

const clientes = $('Webhook').first().json.body.clientes || [];
const csv = $input.first().json.data;

// 1) Ler o CSV de investimentos
const [cabecalho, ...linhas] = csv.trim().split(/\r?\n/);
const colunas = cabecalho.split(',').map(c => c.trim());
const opcoes = linhas.filter(Boolean).map(l => {
  const v = l.split(',');
  const o = {};
  colunas.forEach((c, i) => (o[c] = (v[i] || '').trim()));
  o.minimo = Number(o.minimo);
  return o;
});

// 2) Utilitários
const parseSaldo = s => Number(String(s).replace(/[^\d,]/g, '').replace(',', '.'));
const brl = n => 'R$ ' + n.toFixed(2).replace('.', ',').replace(/\B(?=(\d{3})+(?!\d))/g, '.');

const templates = {
  Conservador: 'foco em segurança e liquidez, com renda fixa',
  Moderado: 'mix equilibrado entre renda fixa e renda variável',
  Arrojado: 'ênfase em ações e maior potencial de retorno, aceitando mais volatilidade',
};

// 3) Cruzar perfil x saldo e montar a mensagem
return clientes.map(c => {
  const saldo = parseSaldo(c.saldo);
  const doPerfil = opcoes.filter(o => o.perfil === c.perfil);
  const elegiveis = doPerfil.filter(o => o.minimo <= saldo);
  const primeiroNome = c.nome.split(' ')[0];
  const estrategia = templates[c.perfil] || 'diversificação adequada ao seu momento';

  let mensagem;
  if (elegiveis.length) {
    const lista = elegiveis
      .map(o => `${o.produto} (mín. ${brl(o.minimo)}, rentab. ${o.rentabilidade})`)
      .join('; ');
    mensagem =
      `Olá, ${primeiroNome}! Com seu perfil ${c.perfil} e saldo de ${brl(saldo)}, ` +
      `sugerimos uma estratégia com ${estrategia}. Opções disponíveis para você: ${lista}.`;
  } else {
    const menor = doPerfil.slice().sort((a, b) => a.minimo - b.minimo)[0];
    mensagem =
      `Olá, ${primeiroNome}! Seu perfil ${c.perfil} pede ${estrategia}. ` +
      `Seu saldo atual (${brl(saldo)}) ainda está abaixo do mínimo das opções do perfil` +
      (menor ? `; a mais acessível é ${menor.produto}, a partir de ${brl(menor.minimo)}.` : '.');
  }

  return {
    json: {
      nome: c.nome,
      email: c.email,
      perfil: c.perfil,
      saldo,
      opcoes_elegiveis: elegiveis.map(o => o.produto),
      mensagem,
    },
  };
});
