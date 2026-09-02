App_MercadoLivre_Pesquisas/
├── server.js
├── package.json
├── package-lock.json
├── .vscode/
│   ├── launch.json
│   └── settings.json
├── .gitignore
├── README.md
└── templates/
    ├── index.html
    └── result.html
```

# Documentação Técnica

## Descrição
Este projeto é uma aplicação para realizar pesquisas no Mercado Livre, focada em marketing digital e vendas.

## Funcionalidades
- Pesquisa de produtos
- Filtros de pesquisa
- Visualização de detalhes do produto
- Comparação de preços

## Tecnologias Utilizadas
- Linguagem de Programação: Python
- Framework: Flask
- Banco de Dados: SQLite
- API: Mercado Livre

## Instalação
1. Clone o repositório:
   ```bash
   git clone https://github.com/usuario/repo.git
   ```
2. Navegue até o diretório do projeto:
   ```bash
   cd App_MercadoLivre_pesquisas
   ```
3. Crie um ambiente virtual:
   ```bash
   python -m venv venv
   ```
4. Ative o ambiente virtual:
   - No Windows:
     ```bash
     venv\Scripts\activate
     ```
   - No Linux/Mac:
     ```bash
     source venv/bin/activate
     ```
5. Instale as dependências:
   ```bash
   pip install -r requirements.txt
   ```

## Uso
1. Inicie a aplicação:
   ```bash
   flask run
   ```
2. Acesse a aplicação no navegador:
   ```
   http://127.0.0.1:5000
   ```

## Contribuição
1. Faça um fork do projeto
2. Crie uma branch para sua feature:
   ```bash
   git checkout -b minha-feature
   ```
3. Commit suas mudanças:
   ```bash
   git commit -m 'Minha nova feature'
   ```
4. Faça um push para a branch:
   ```bash
   git push origin minha-feature
   ```
5. Abra um Pull Request

## Enviando o Projeto para o GitHub

1. Inicialize um repositório Git:
   ```bash
   git init
   ```

2. Adicione os arquivos ao repositório:
   ```bash
   git add .
   ```

3. Faça o commit das mudanças:
   ```bash
   git commit -m "Primeiro commit"
   ```

4. Adicione o repositório remoto:
   ```bash
   git remote add origin https://github.com/usuario/repo.git
   ```

5. Envie as mudanças para o GitHub:
   ```bash
   git push -u origin master
   ```

## Licença
Este projeto está licenciado sob a Licença MIT - veja o arquivo LICENSE para mais detalhes.
