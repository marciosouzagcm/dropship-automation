const express = require("express");
const app = express();
const axios = require("axios");
require('dotenv').config();  // Carrega as variáveis de ambiente do arquivo .env

// Configurações da API
const ACCESS_TOKEN = process.env.ACCESS_TOKEN;  // Token de acesso para autenticação na API do Mercado Livre
const SITE_ID = process.env.SITE_ID;  // Identificador do site do Mercado Livre para o Brasil

// Rota para pesquisar produtos com base em um termo de pesquisa
app.get("/pesquisar", async (req, res) => {
  try {
    const termo = req.query.termo;  // Obtém o termo de pesquisa dos parâmetros da URL
    // Faz a requisição GET para a API do Mercado Livre com o termo de pesquisa e o token de acesso
    const response = await axios.get(`https://api.mercadolibre.com/sites/${SITE_ID}/search?q=${termo}`, {
      headers: {
        Authorization: `Bearer ${ACCESS_TOKEN}`,
      },
    });
    // Mapeia os resultados da resposta para extrair os dados relevantes
    const dados = response.data.results.map(item => ({
      titulo: item.title,
      preco: item.price,
      url: item.permalink
    }));
    // Retorna os dados em formato JSON
    res.json(dados);
  } catch (error) {
    // Retorna um erro 500 em caso de falha na requisição
    res.status(500).send(error.toString());
  }
});

// Configura o servidor para escutar na porta especificada
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Servidor rodando na porta ${PORT}`);
});