// N8N Code node (Run Once for All Items) — transforma o CSV de investimentos em itens.
const csv = $input.first().json.data || '';

const [cabecalho, ...linhas] = csv.trim().split(/\r?\n/);
const colunas = cabecalho.split(',').map(c => c.trim());

return linhas.filter(Boolean).map(l => {
  const v = l.split(',');
  const o = {};
  colunas.forEach((c, i) => (o[c] = (v[i] || '').trim()));
  return {
    json: {
      perfil: o.perfil,
      produto: o.produto,
      minimo: Number(o.minimo),
      rentabilidade: o.rentabilidade,
    },
  };
});
