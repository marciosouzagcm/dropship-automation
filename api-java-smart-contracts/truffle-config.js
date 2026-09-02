module.exports = {
  contracts_directory: './contracts',
  networks: {
    development: {
      host: "127.0.0.1",
      port: 8545,
      network_id: "*" // Match any network ID
    }
  },
  compilers: {
    solc: {
      version: "0.8.0" // Ou a versão do seu compilador Solidity
    }
  }
};