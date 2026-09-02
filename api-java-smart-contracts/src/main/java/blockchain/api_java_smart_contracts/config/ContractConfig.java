package blockchain.api_java_smart_contracts.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

@Configuration // Indica que esta classe contém definições de beans gerenciados pelo Spring.
public class ContractConfig {

    @Value("${ethereum.rpc.url}") // Injeta o valor da propriedade 'ethereum.rpc.url' definida no arquivo de configuração (application.properties ou application.yml).
    private String nodeUrl; // Armazena a URL do nó Ethereum ao qual a aplicação se conectará.

    @Value("${blockchain.voting.contract-address}") // Injeta o valor da propriedade 'blockchain.voting.contract-address' do arquivo de configuração.
    private String contractAddress; // Armazena o endereço do contrato inteligente de votação implantado na blockchain.

    @Value("${contract.private-key}") // Injeta o valor da propriedade 'contract.private-key' do arquivo de configuração. Esta é a chave privada da conta que interagirá com o contrato.
    private String privateKey; // Armazena a chave privada da conta Ethereum que será usada para assinar as transações com o contrato.

    @Bean("contractWeb3j") // Define um bean chamado 'contractWeb3j' gerenciado pelo Spring. O nome do bean é usado para injeção de dependência.
    public Web3j contractWeb3j() { // Este método cria e configura uma instância de Web3j.
        return Web3j.build(new HttpService(nodeUrl)); // Cria uma nova instância de Web3j, que é a principal classe Java para interagir com a blockchain Ethereum.
                                                     // 'HttpService' é usado para se conectar a um nó Ethereum através de uma interface HTTP RPC, cuja URL é fornecida por 'nodeUrl'.
    }

    @Bean("credentialsContract") // Define um bean chamado 'credentialsContract' que contém as credenciais da conta que interagirá com o contrato.
    public Credentials credentialsContract() { // Este método cria e configura as credenciais da conta Ethereum.
        return Credentials.create(privateKey); // Cria um objeto 'Credentials' a partir da chave privada fornecida ('privateKey').
                                               // 'Credentials' é usado para assinar transações antes de serem enviadas para a blockchain.
    }

    @Bean // Define um bean anônimo (o nome será o nome do método) para o provedor de gás do contrato.
    public ContractGasProvider contractGasProvider() { // Este método cria e configura o provedor de gás para as transações do contrato.
        return new DefaultGasProvider(); // Retorna uma instância de 'DefaultGasProvider'. Este provedor de gás estima o preço do gás e o limite de gás para as transações.
                                         // Em um ambiente de produção, pode ser necessário um provedor de gás mais sofisticado para lidar com a volatilidade dos preços do gás.
    }

    @Bean("votingContract") // Define um bean chamado 'votingContract' que representa a abstração Java do contrato inteligente de votação.
    public blockchain.api_java_smart_contracts.service.Voting votingContract( // Este método instancia e carrega a representação Java do contrato.
            @Qualifier("contractWeb3j") Web3j web3j, // Injeta o bean 'contractWeb3j' (a instância de Web3j configurada).
            @Qualifier("credentialsContract") Credentials credentials, // Injeta o bean 'credentialsContract' (as credenciais da conta do contrato).
            ContractGasProvider contractGasProvider) { // Injeta o bean 'contractGasProvider' (o provedor de gás).
        return new blockchain.api_java_smart_contracts.service.Voting(web3j, credentials, contractGasProvider).load(contractAddress);
        // Cria uma nova instância da classe 'Voting' (gerada pelo web3j a partir do ABI do contrato).
        // O construtor recebe a instância de Web3j, as credenciais e o provedor de gás.
        // O método 'load(contractAddress)' é chamado para conectar essa instância Java a uma instância específica do contrato inteligente já implantada na blockchain, cujo endereço é 'contractAddress'.
    }
}