// N8N Code node (Run Once for All Items) — normaliza o e-mail e a mensagem gerada pelo modelo.
// O nó do modelo não repassa os dados do cliente; os itens são casados pela posição.
const clientes = $('Code - Normalizar Tabela').all();
const respostas = $input.all();

const emailRegex = /^[a-z0-9._%+-]+@[a-z0-9-]+(\.[a-z0-9-]+)*\.[a-z]{2,}$/;

const extrairTexto = j => {
  if (!j || j.error) return '';
  if (typeof j.output_text === 'string') return j.output_text;
  if (typeof j.text === 'string') return j.text;
  if (typeof j.output === 'string') return j.output;
  const partes = (Array.isArray(j.output) ? j.output : [])
    .flatMap(o => o.content || [])
    .map(c => c.text || '')
    .filter(Boolean);
  return partes.join('\n');
};

return clientes.map(({ json: c }, i) => {
  const email = String(c.email || '').trim().toLowerCase();
  const gerada = extrairTexto(respostas[i]?.json).replace(/[*_#`>]/g, '').trim();

  const padrao =
    `Olá, ${c.primeiro_nome}! Com seu perfil ${c.perfil} e saldo de ${c.saldo_formatado}, ` +
    `as opções disponíveis para você são: ${c.opcoes_elegiveis.join(', ') || 'nenhuma no momento'}.`;

  const corpo = gerada || padrao;

  return {
    json: {
      ...c,
      email,
      email_valido: email.length <= 254 && emailRegex.test(email),
      mensagem_origem: gerada ? 'ia' : 'modelo_padrao',
      assunto: `${c.primeiro_nome}, uma sugestão de investimento para o seu perfil ${c.perfil}`,
      corpo,
    },
  };
});
