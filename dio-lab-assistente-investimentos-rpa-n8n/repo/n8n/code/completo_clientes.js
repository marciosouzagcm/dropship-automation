// N8N Code node (Run Once for All Items) — normaliza os clientes recebidos no Webhook.
const clientes = $('Webhook').first().json.body.clientes || [];

const parseSaldo = s => Number(String(s).replace(/[^\d,]/g, '').replace(',', '.')) || 0;

return clientes.map(c => ({
  json: {
    nome: String(c.nome || '').trim(),
    email: String(c.email || '').trim(),
    perfil: String(c.perfil || '').trim(),
    saldo: parseSaldo(c.saldo),
  },
}));
