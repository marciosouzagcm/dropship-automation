// N8N Code node (Run Once for All Items) — a saída do Merge tem uma linha por cliente x produto;
// aqui ela vira uma linha por cliente, com a tabela de opções e o prompt para o modelo.
const brl = n =>
  'R$ ' + Number(n).toFixed(2).replace('.', ',').replace(/\B(?=(\d{3})+(?!\d))/g, '.');

const grupos = new Map();
for (const { json: r } of $input.all()) {
  const chave = `${r.email}|${r.nome}`;
  if (!grupos.has(chave)) {
    grupos.set(chave, { nome: r.nome, email: r.email, perfil: r.perfil, saldo: r.saldo, opcoes: [] });
  }
  if (r.produto) {
    grupos.get(chave).opcoes.push({
      produto: r.produto,
      minimo: Number(r.minimo),
      rentabilidade: r.rentabilidade,
    });
  }
}

return [...grupos.values()].map(g => {
  const elegiveis = g.opcoes.filter(o => o.minimo <= g.saldo);
  const primeiroNome = g.nome.split(' ')[0];

  const tabela = [
    '| Produto | Mínimo | Rentabilidade | Elegível |',
    '| --- | --- | --- | --- |',
    ...g.opcoes.map(
      o => `| ${o.produto} | ${brl(o.minimo)} | ${o.rentabilidade} | ${o.minimo <= g.saldo ? 'Sim' : 'Não'} |`
    ),
  ].join('\n');

  const prompt =
    `Cliente: ${g.nome}\n` +
    `Perfil de investidor: ${g.perfil}\n` +
    `Saldo disponível: ${brl(g.saldo)}\n` +
    `Situação: ${
      elegiveis.length
        ? `saldo cobre o mínimo de ${elegiveis.length} opção(ões)`
        : 'saldo abaixo do mínimo de todas as opções do perfil'
    }\n\n` +
    `Opções de investimento do perfil:\n${tabela}`;

  return {
    json: {
      nome: g.nome,
      primeiro_nome: primeiroNome,
      email: g.email,
      perfil: g.perfil,
      saldo: g.saldo,
      saldo_formatado: brl(g.saldo),
      opcoes_elegiveis: elegiveis.map(o => o.produto),
      tabela_opcoes: tabela,
      prompt,
    },
  };
});
