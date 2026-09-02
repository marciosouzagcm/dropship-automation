// BlockchainService.java
package blockchain.api_java_smart_contracts.service;

import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier; // Usado para especificar qual bean injetar quando há múltiplas opções
import org.springframework.beans.factory.annotation.Value; // Usado para injetar valores de configuração (ex: application.properties)
import org.springframework.stereotype.Service; // Indica que esta classe é um serviço Spring, contendo lógica de negócios
import org.web3j.abi.datatypes.Address; // Representa um endereço Ethereum
import org.web3j.abi.datatypes.Bool; // Representa um valor booleano Solidity
import org.web3j.abi.datatypes.generated.Uint256; // Representa um inteiro não assinado de 256 bits Solidity
import org.web3j.crypto.Credentials; // Contém as credenciais (chave privada) para assinar transações
import org.web3j.protocol.Web3j; // Interface principal para interagir com um nó Ethereum
import org.web3j.protocol.core.methods.response.TransactionReceipt; // Representa o recibo de uma transação na blockchain
import org.web3j.protocol.http.HttpService; // Implementação do Web3j para comunicação via HTTP com um nó Ethereum
import org.web3j.tx.gas.DefaultGasProvider; // Interface para fornecer o preço do gas e o limite de gas para as transações

import blockchain.api_java_smart_contracts.service.Voting.Candidato; // Importe o DefaultGasProvider explicitamente
import jakarta.annotation.PostConstruct; // Importa a classe Candidato gerada pelo Web3j a partir do contrato Solidity

@Service // Marca esta classe como um serviço Spring, um componente que contém a lógica de negócios da aplicação
public class BlockchainService {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainService.class); // Cria um objeto Logger para registrar eventos

    @Value("${ethereum.rpc.url}") // Injeta o valor da propriedade 'ethereum.rpc.url' definida no arquivo de configuração (ex: application.properties)
    private String nodeUrl; // Armazena a URL do nó Ethereum ao qual a aplicação se conectará

    private Web3j web3j; // Instância da biblioteca Web3j, utilizada para interagir com a blockchain Ethereum

    @Autowired // Injeta uma instância da classe 'Voting', que é uma representação Java do nosso contrato inteligente Solidity (gerada pelo Web3j)
    private Voting voting;

    @Autowired
    @Qualifier("credentialsContract") // Especifica qual bean de 'Credentials' injetar caso haja múltiplos definidos. Neste caso, o bean nomeado 'credentialsContract'
    private Credentials credentials; // Contém as credenciais (chave privada) da conta Ethereum que enviará as transações para o contrato

    @Autowired
    private DefaultGasProvider gasProvider; // Injeta o 'DefaultGasProvider', responsável por fornecer os valores padrão de gas para as transações

    @PostConstruct // Anotação que indica que o método 'init' deve ser executado após a criação da instância deste bean e a injeção de suas dependências
    public void init() {
        web3j = Web3j.build(new HttpService(nodeUrl)); // Cria uma nova conexão com o nó Ethereum usando a URL configurada
        try {
            String clientVersion = web3j.web3ClientVersion().send().getWeb3ClientVersion(); // Envia uma chamada RPC para obter a versão do cliente Ethereum do nó
            logger.info("Conectado ao nó Ethereum: {} - Versão: {}", nodeUrl, clientVersion); // Loga uma mensagem de sucesso indicando a conexão e a versão do nó
        } catch (Exception e) {
            logger.error("Erro ao conectar ao nó Ethereum: {}", e.getMessage(), e); // Loga uma mensagem de erro caso a conexão com o nó falhe
        }
    }

    // Método para adicionar um novo candidato ao sistema de votação através do contrato inteligente
    public Future<TransactionReceipt> adicionarCandidato(String nome) throws BlockchainServiceException {
        try {
            // Chama a função 'adicionarCandidato' do contrato inteligente 'voting', passando o nome do candidato e o 'gasProvider' para estimativa de gas
            return voting.adicionarCandidato(nome, gasProvider).sendAsync(); // 'sendAsync' envia a transação de forma assíncrona e retorna um 'Future' para acompanhar o resultado
        } catch (Exception e) {
            logger.error("Erro ao chamar adicionarCandidato no contrato: {}", e.getMessage(), e); // Loga um erro se a chamada à função do contrato falhar
            throw new BlockchainServiceException("Erro ao adicionar candidato no contrato", e); // Lança uma exceção personalizada indicando a falha na interação com o contrato
        }
    }

    // Método para autorizar ou desautorizar um eleitor a votar no contrato inteligente
    public Future<TransactionReceipt> autorizarEleitor(String enderecoEleitor, boolean autorizado)
            throws BlockchainServiceException {
        try {
            // Chama a função 'autorizarEleitor' do contrato, passando o endereço do eleitor, o status de autorização e o 'gasProvider'
            return ((org.web3j.protocol.core.RemoteCall<TransactionReceipt>) voting.autorizarEleitor(enderecoEleitor, autorizado, gasProvider)).sendAsync();
        } catch (Exception e) {
            logger.error("Erro ao chamar autorizarEleitor no contrato para {}: {}", enderecoEleitor, e.getMessage(), e);
            throw new BlockchainServiceException("Erro ao autorizar eleitor no contrato", e);
        }
    }

    // Método para registrar um voto para um candidato específico no contrato inteligente
    public Future<TransactionReceipt> votar(BigInteger candidatoId) throws BlockchainServiceException {
        try {
            // Chama a função 'votar' do contrato, passando o ID do candidato e o 'gasProvider'
            return voting.votar(candidatoId).sendAsync();
        } catch (Exception e) {
            logger.error("Erro ao chamar votar no contrato para candidato ID {}: {}", candidatoId, e.getMessage(), e);
            throw new BlockchainServiceException("Erro ao votar no contrato", e);
        }
    }

    // Método para obter os detalhes de um candidato específico do contrato inteligente
    public CompletableFuture<Voting.Candidato> obterCandidato(BigInteger id) throws BlockchainServiceException {
        // Utiliza CompletableFuture para realizar a chamada ao contrato de forma assíncrona e tratar possíveis erros
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Chama a função 'obterCandidato' do contrato inteligente 'voting' com o ID fornecido
                return voting.obterCandidato(id);
            } catch (Exception e) {
                logger.error("Erro ao obter candidato do contrato para ID {}: {}", id, e.getMessage(), e);
                throw new RuntimeException(new BlockchainServiceException("Erro ao obter candidato do contrato", e));
            }
        }).exceptionallyComposeAsync(e -> {
            // Trata exceções que ocorreram durante a execução da chamada assíncrona
            if (e.getCause() instanceof BlockchainServiceException) {
                return CompletableFuture.failedFuture((BlockchainServiceException) e.getCause());
            } else {
                logger.error("Erro inesperado ao obter candidato: {}", e.getMessage(), e);
                return CompletableFuture
                        .failedFuture(new BlockchainServiceException("Erro inesperado ao obter candidato", e));
            }
        });
    }

    // Método para verificar se um determinado eleitor já votou através do contrato inteligente
    public CompletableFuture<Boolean> verificarSeVotou(String enderecoEleitor) throws BlockchainServiceException {
        try {
            // Chama a função 'verificarSeVotou' do contrato, passando o endereço do eleitor
            return voting.verificarSeVotou(new Address(enderecoEleitor)).sendAsync().thenApply(Bool::getValue); // Converte o resultado 'Bool' (Web3j) para 'Boolean' (Java)
        } catch (Exception e) {
            logger.error("Erro ao chamar verificarSeVotou no contrato para {}: {}", enderecoEleitor, e.getMessage(), e);
            throw new BlockchainServiceException("Erro ao verificar se eleitor votou no contrato", e);
        }
    }

    // Método para obter o número total de candidatos registrados no contrato inteligente
    public CompletableFuture<Uint256> obterTotalDeCandidatos() throws BlockchainServiceException {
        try {
            // Chama a função 'obterTotalDeCandidatos' do contrato
            return voting.obterTotalDeCandidatos().sendAsync();
        } catch (Exception e) {
            logger.error("Erro ao chamar obterTotalDeCandidatos no contrato: {}", e.getMessage(), e);
            throw new BlockchainServiceException("Erro ao obter total de candidatos do contrato", e);
        }
    }

    // Método para obter o endereço do administrador do contrato inteligente
    public CompletableFuture<Address> obterAdministrador() throws BlockchainServiceException {
        try {
            // Chama a função 'obterAdministrador' do contrato
            return voting.obterAdministrador().sendAsync();
        } catch (Exception e) {
            logger.error("Erro ao chamar obterAdministrador no contrato: {}", e.getMessage(), e);
            throw new BlockchainServiceException("Erro ao obter administrador do contrato", e);
        }
    }

    // Método para desautorizar um eleitor a votar no contrato inteligente
    public Future<TransactionReceipt> desautorizarEleitor(String enderecoEleitor) throws BlockchainServiceException {
        try {
            // Chama a função 'autorizarEleitor' do contrato com 'autorizado' como false para desautorizar
            return ((org.web3j.protocol.core.RemoteCall<TransactionReceipt>) voting.autorizarEleitor(enderecoEleitor, false, gasProvider)).sendAsync();
        } catch (Exception e) {
            logger.error("Erro ao chamar autorizarEleitor (desautorizar) no contrato para {}: {}", enderecoEleitor,
                    e.getMessage(), e);
            throw new BlockchainServiceException("Erro ao desautorizar eleitor no contrato", e);
        }
    }

    // Método para obter os detalhes de um candidato e encapsulá-los em um Optional
    public CompletableFuture<Optional<Candidato>> obterCandidatoDetalhado(BigInteger id) throws BlockchainServiceException {
        // Chama o método 'obterCandidato' para obter os detalhes e então envolve o resultado em um Optional
        return obterCandidato(id)
                .thenApply(Optional::ofNullable) // Se o resultado de obterCandidato não for nulo, cria um Optional com o Candidato, senão cria um Optional vazio
                .exceptionallyAsync(e -> {
                    logger.error("Erro ao obter candidato", e);
                    throw new RuntimeException(e);
                });
    }

    @SuppressWarnings("unused") // Esta anotação indica que o método 'handleObterCandidatoDetalhadoException' não está sendo usado diretamente neste código
    private CompletableFuture<Candidato> handleObterCandidatoDetalhadoException(BigInteger id, Throwable e) {
        logger.error("Erro ao obter detalhes do candidato com ID {}: {}", id, e.getMessage(), e);
        return CompletableFuture.failedFuture(new BlockchainServiceException("Erro ao obter detalhes do candidato", e));
    }
}