const Voting = artifacts.require("Voting");

module.exports = function (deployer) {
  // Este script de migração para o Truffle tem como objetivo
  // implantar o contrato inteligente Voting na blockchain.

  // 'artifacts.require("Voting")' instrui o Truffle a procurar
  // pelo artefato de construção do contrato Voting (o arquivo JSON
  // gerado após a compilação do seu código Solidity). Este artefato
  // contém informações importantes sobre o contrato, como seu ABI
  // (Application Binary Interface) e bytecode.

  // 'module.exports = function (deployer) { ... };' é a estrutura
  // padrão para um script de migração do Truffle. A função que você
  // exporta recebe um objeto 'deployer' como argumento. Este objeto
  // é responsável por orquestrar a implantação de seus contratos.

  // 'deployer.deploy(Voting);' é a linha de código chave neste script.
  // Ela instrui o 'deployer' a implantar uma nova instância do contrato
  // Voting na blockchain. O Truffle cuidará de enviar a transação de
  // implantação com o bytecode do contrato para a rede Ethereum (ou
  // qualquer outra rede compatível que você esteja usando).

  // Após a execução bem-sucedida desta migração, o contrato Voting
  // estará disponível no endereço da blockchain retornado pelo Truffle.
  // Você poderá então interagir com as funções do contrato usando
  // ferramentas como o console do Truffle, Web3.js ou sua aplicação
  // Java Spring Boot.
  deployer.deploy(Voting);
};