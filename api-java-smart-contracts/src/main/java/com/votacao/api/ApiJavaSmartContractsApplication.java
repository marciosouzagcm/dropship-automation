package com.votacao.api;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;

import blockchain.api_java_smart_contracts.service.VotingService;

@SpringBootApplication(scanBasePackages = {
        "com.votacao.api",
        "blockchain.api_java_smart_contracts.service",
        "blockchain.api_java_smart_contracts.config",
        "blockchain.api_java_smart_contracts.controller"
})
public class ApiJavaSmartContractsApplication implements CommandLineRunner {

    private static final Logger LOGGER = Logger.getLogger(ApiJavaSmartContractsApplication.class.getName());

    @Value("${ethereum.rpc.url}")
    private String nodeUrl;

    @Value("${blockchain.voting.contract-address}")
    private String contractAddress;

    @Autowired
    @Qualifier("web3jPrincipal")
    private Web3j web3j;

    @Autowired
    @Qualifier("credentialsPrincipal")
    private Credentials credentials;

    @Autowired
    private VotingService votingService;

    public static void main(String[] args) {
        SpringApplication.run(ApiJavaSmartContractsApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().send();
            System.out.println("Versão do cliente Ethereum: " + clientVersion.getWeb3ClientVersion());
            LOGGER.log(Level.INFO, "Conectado ao nó Ethereum: {0}", nodeUrl);

            if (credentials != null) {
                EthGetBalance balance = web3j.ethGetBalance(credentials.getAddress(), DefaultBlockParameterName.LATEST)
                        .send();
                LOGGER.log(Level.INFO, "Saldo da conta {0}: {1} Wei",
                        new Object[]{credentials.getAddress(), balance.getBalance()});
            }

            // Log the contract address to ensure it is read
            LOGGER.log(Level.INFO, "Endereço do contrato de votação: {0}", contractAddress);

            if (votingService != null) {
                interagirComContrato();
            }

        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Erro geral na inicialização ou interação com a blockchain", e);
        }
    }

    private void interagirComContrato() {
        try {
            // Exemplo: adicionando um candidato de teste
            votingService.adicionarCandidato("Candidato Teste");
            System.out.println("Candidato adicionado com sucesso!");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao interagir com o contrato", e);
        }
    }
}