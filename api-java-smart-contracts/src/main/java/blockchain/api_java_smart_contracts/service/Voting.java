package blockchain.api_java_smart_contracts.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;

@Service // Indica que esta classe é um serviço Spring, responsável por interagir com o contrato inteligente de votação
public class Voting extends Contract {
    private static final Logger LOGGER = Logger.getLogger(Voting.class.getName()); // Logger para registrar eventos desta classe
    private static final String ABI; // String estática para armazenar a Interface Binária da Aplicação (ABI) do contrato

    @Value("${blockchain.voting.contract-address}") // Injeta o valor da propriedade 'blockchain.voting.contract-address' do arquivo de configuração
    private String contractAddress; // Endereço do contrato inteligente de votação na blockchain

    private final Web3j web3j; // Instância do Web3j para comunicação com o nó Ethereum
    private final Credentials credentials; // Credenciais da conta Ethereum para assinar transações
    private final ContractGasProvider gasProvider; // Provedor de gás para estimar o custo das transações

    // Bloco estático para carregar o ABI do contrato a partir do arquivo Voting.json
    static {
        try (InputStream inputStream = Voting.class.getClassLoader().getResourceAsStream("abi/Voting.json")) {
            if (inputStream == null) {
                throw new RuntimeException("Arquivo Voting.json não encontrado em src/main/resources/abi");
            }
            final org.json.JSONArray jsonArray = new org.json.JSONArray(new String(inputStream.readAllBytes()));
            ABI = jsonArray.toString(); // Converte o conteúdo do arquivo JSON (ABI) para uma String
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar o ABI do contrato de Voting.json", e);
        }
    }

    // Construtor da classe Voting, injetando as dependências necessárias
    @Autowired
    public Voting(@Qualifier("contractWeb3j") Web3j web3j,
                  @Qualifier("credentialsContract") Credentials credentials,
                  ContractGasProvider gasProvider) {
        super(ABI, null, web3j, credentials, gasProvider); // Chama o construtor da classe pai (Contract) com o ABI, endereço (inicialmente nulo), Web3j, credenciais e provedor de gás
        this.web3j = web3j;
        this.credentials = credentials;
        this.gasProvider = gasProvider;
    }

    // Método para carregar o contrato com o endereço configurado
    public Voting load() {
        super.setContractAddress(this.contractAddress); // Define o endereço do contrato para as futuras interações
        return this; // Retorna a instância carregada do contrato Voting
    }

    // Função para interagir com a função 'adicionarCandidato' do contrato inteligente
    public RemoteCall<TransactionReceipt> adicionarCandidato(String nome, DefaultGasProvider gasProvider1) {
        Function function = new Function(
                "adicionarCandidato", // Nome da função no contrato inteligente
                Arrays.asList(new Utf8String(nome)), // Lista dos parâmetros de entrada da função (nome do candidato como Utf8String)
                Collections.emptyList()); // Lista dos tipos de retorno da função (vazia, pois é uma transação)
        return executeRemoteCallTransaction(function); // Executa a chamada remota para a transação
    }

    // Função para interagir com a função 'votar' do contrato inteligente
    public RemoteCall<TransactionReceipt> votar(BigInteger idCandidato) {
        Function function = new Function(
                "votar", // Nome da função no contrato inteligente
                Arrays.asList(new Uint256(idCandidato)), // Lista dos parâmetros de entrada (ID do candidato como Uint256)
                Collections.emptyList()); // Lista dos tipos de retorno (vazia, pois é uma transação)
        return executeRemoteCallTransaction(function); // Executa a chamada remota para a transação
    }

    // Função para interagir com a função 'autorizarEleitor' do contrato inteligente
    public RemoteCall<TransactionReceipt> autorizarEleitor(String eleitorAddress, boolean autorizado) {
        Function function = new Function(
                "autorizarEleitor", // Nome da função no contrato inteligente
                Arrays.asList(new Address(eleitorAddress), new Bool(autorizado)), // Lista dos parâmetros de entrada (endereço do eleitor como Address e autorização como Bool)
                Collections.emptyList()); // Lista dos tipos de retorno (vazia, pois é uma transação)
        return executeRemoteCallTransaction(function); // Executa a chamada remota para a transação
    }

    // Função para chamar a função 'obterCandidato' do contrato e obter os valores brutos de retorno
    public RemoteFunctionCall<List<Type>> obterCandidatoRaw(BigInteger idCandidato) {
        Function function = new Function(
                "obterCandidato", // Nome da função no contrato inteligente
                Arrays.asList(new Uint256(idCandidato)), // Lista dos parâmetros de entrada (ID do candidato como Uint256)
                Arrays.asList( // Lista dos tipos de retorno da função
                        new TypeReference<Uint256>() {}, // ID do candidato (Uint256)
                        new TypeReference<Utf8String>() {}, // Nome do candidato (Utf8String)
                        new TypeReference<Uint256>() {} // Contagem de votos (Uint256)
                ));
        return executeRemoteCallMultipleValueReturn(function); // Executa a chamada remota para obter múltiplos valores de retorno
    }

    // Função para obter os detalhes de um candidato, convertendo os valores brutos para um objeto Candidato
    public Candidato obterCandidato(BigInteger idCandidato) throws Exception {
        List<Type> result = this.obterCandidatoRaw(idCandidato).send(); // Chama a função raw e obtém os resultados

        LOGGER.log(Level.INFO, "Resultado bruto do contrato: {0}", result); // Loga o resultado bruto obtido do contrato

        BigInteger id = (BigInteger) result.get(0).getValue(); // Extrai o ID do candidato do resultado
        String nome = (String) result.get(1).getValue(); // Extrai o nome do candidato do resultado
        BigInteger contagemDeVotos = (BigInteger) result.get(2).getValue(); // Extrai a contagem de votos do resultado

        return new Candidato(id, nome, contagemDeVotos); // Cria e retorna um novo objeto Candidato com os dados obtidos
    }

    // Sobrecarga do método load para permitir carregar o contrato com um endereço específico
    public Voting load(String contractAddress) {
        super.setContractAddress(contractAddress); // Define o endereço do contrato
        return this; // Retorna a instância carregada do contrato Voting
    }

    // Este método parece ser um erro ou incompleto, pois lança uma exceção de operação não suportada
    Object autorizarEleitor(String enderecoEleitor, boolean b, DefaultGasProvider gasProvider) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Classe interna estática para representar um Candidato
    public static class Candidato {
        public BigInteger id; // ID do candidato
        public String nome; // Nome do candidato
        public BigInteger contagemDeVotos; // Número de votos do candidato

        // Construtor da classe Candidato
        public Candidato(BigInteger id, String nome, BigInteger contagemDeVotos) {
            this.id = id;
            this.nome = nome;
            this.contagemDeVotos = contagemDeVotos;
        }

        // Este método parece ser um erro ou incompleto, pois lança uma exceção de operação não suportada
        public CompletableFuture<TransactionReceipt> sendAsync() {
            throw new UnsupportedOperationException("Unimplemented method 'sendAsync'");
        }
    }

    // Método para exibir as informações de um candidato logando-as
    public void exibirCandidato(BigInteger idCandidato) throws Exception {
        Voting.Candidato candidato = obterCandidato(idCandidato); // Obtém os detalhes do candidato
        LOGGER.log(Level.INFO, "Candidato: {0}, Votos: {1}", // Loga o nome e a contagem de votos do candidato
                new Object[] { candidato.nome, candidato.contagemDeVotos });
    }

    // Função para interagir com a função 'verificarSeVotou' do contrato inteligente
    public RemoteFunctionCall<Bool> verificarSeVotou(Address address) {
        Function function = new Function(
                "verificarSeVotou", // Nome da função no contrato inteligente
                Arrays.asList(address), // Lista dos parâmetros de entrada (endereço do eleitor como Address)
                Arrays.asList(new TypeReference<Bool>() { // Lista dos tipos de retorno (um booleano)
                }));
        return executeRemoteCallSingleValueReturn(function, Bool.class); // Executa a chamada remota para obter um único valor de retorno (booleano)
    }

    // Função para interagir com a função 'obterTotalDeCandidatos' do contrato inteligente
    public RemoteFunctionCall<Uint256> obterTotalDeCandidatos() {
        Function function = new Function(
                "obterTotalDeCandidatos", // Nome da função no contrato inteligente
                Collections.emptyList(), // Lista dos parâmetros de entrada (vazia)
                Arrays.asList(new TypeReference<Uint256>() { // Lista dos tipos de retorno (um inteiro não assinado de 256 bits)
                }));
        return executeRemoteCallSingleValueReturn(function, Uint256.class); // Executa a chamada remota para obter um único valor de retorno (Uint256)
    }

    // Função para interagir com a função 'administrador' do contrato inteligente para obter o endereço do administrador
    public RemoteFunctionCall<Address> obterAdministrador() {
        Function function = new Function(
                "administrador", // Nome da função no contrato inteligente
                Collections.emptyList(), // Lista dos parâmetros de entrada (vazia)
                Arrays.asList(new TypeReference<Address>() { // Lista dos tipos de retorno (um endereço Ethereum)
                }));
        return executeRemoteCallSingleValueReturn(function, Address.class); // Executa a chamada remota para obter um único valor de retorno (Address)
    }
}