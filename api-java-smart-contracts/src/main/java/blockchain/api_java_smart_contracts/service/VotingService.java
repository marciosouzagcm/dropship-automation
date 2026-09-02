package blockchain.api_java_smart_contracts.service;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.gas.ContractGasProvider;

import com.votacao.api.exception.EleitorException;

import blockchain.api_java_smart_contracts.contracts.Voting;
import blockchain.api_java_smart_contracts.model.Candidato;

@Service
@SuppressWarnings("UnnecessaryReturnStatement")
public class VotingService {

    private final Web3j web3j;
    private final String contractAddress;
    private final ContractGasProvider gasProvider;
    private final Voting votingContract;
    private final Credentials credentials;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Logger LOGGER = Logger.getLogger(VotingService.class.getName());

    @Autowired
    public VotingService(@Qualifier("contractWeb3j") Web3j web3j,
                         @Value("${blockchain.voting.contract-address}") String contractAddress,
                         @Qualifier("credentialsContract") Credentials credentials,
                         ContractGasProvider gasProvider) {
        this.web3j = web3j;
        this.contractAddress = contractAddress;
        this.gasProvider = gasProvider;
        this.credentials = credentials;
        this.votingContract = Voting.load(contractAddress, web3j, credentials, gasProvider);
    }

    /**
     * Chama a função `adicionarCandidato` do smart contract `Voting`.
     *
     * @param nome O nome do candidato a ser adicionado.
     * @return String O hash da transação enviada para adicionar o candidato.
     * @throws Exception Se ocorrer algum erro durante a interação com o contrato.
     */
    public String adicionarCandidato(String nome) throws Exception {
        Function function = new Function(
                "adicionarCandidato",
                Arrays.asList(new Utf8String(nome)),
                Collections.emptyList());
        String encodedFunction = FunctionEncoder.encode(function);

        String fromAddress = credentials.getAddress();

        // 1. Estimar o gas necessário para a transação
        Transaction ethCallTransaction = new Transaction(fromAddress, null, null, null, contractAddress, BigInteger.ZERO, encodedFunction);
        EthEstimateGas estimateGas = web3j.ethEstimateGas(ethCallTransaction).sendAsync().get();

        if (estimateGas.hasError()) {
            throw new Exception("Erro ao estimar o gas: " + estimateGas.getError().getMessage());
        }
        BigInteger estimatedGas = estimateGas.getAmountUsed();

        LOGGER.log(Level.INFO,"Gas estimado para adicionar o candidato '" + nome + "': " + estimatedGas);

        // 2. Enviar a transação com um limite de gás maior (para contornar o erro)
        TransactionReceipt receipt = votingContract.adicionarCandidato(nome).send();

        return receipt.getTransactionHash();
    }
    /**
     * Chama a função `autorizarEleitor` do smart contract `Voting` para autorizar
     * um eleitor.
     *
     * @param eleitorAddress O endereço Ethereum do eleitor a ser autorizado.
     * @return String O hash da transação enviada para autorizar o eleitor.
     * @throws Exception Se ocorrer algum erro durante a interação com o contrato.
     */
    public String autorizarEleitor(String eleitorAddress) throws Exception {
        TransactionReceipt receipt = votingContract.autorizarEleitor(eleitorAddress, true).send();
        return receipt.getTransactionHash();
    }

    /**
     * Chama a função `votar` do smart contract `Voting` usando as credenciais
     * padrão configuradas.
     *
     * @param idCandidato O ID do candidato em quem votar.
     * @return String O hash da transação de voto.
     * @throws Exception Se ocorrer algum erro durante a interação com o contrato.
     */
    public String votar(Long idCandidato) throws Exception {
        TransactionReceipt receipt = votingContract.votar(BigInteger.valueOf(idCandidato)).send();
        return receipt.getTransactionHash();
    }

    /**
     * Chama a função `votar` do smart contract `Voting` usando credenciais
     * específicas de um eleitor.
     * Isso permite que diferentes eleitores votem usando suas próprias chaves
     * privadas (cenário avançado).
     *
     * @param idCandidato         O ID do candidato em quem votar.
     * @param eleitorPrivateKey A chave privada do eleitor que está votando.
     * @return String O hash da transação de voto.
     * @throws Exception Se ocorrer algum erro durante a interação com o contrato.
     */
    public String votar(Long idCandidato, String eleitorPrivateKey) throws Exception {
        Credentials eleitorCredentials = Credentials.create(eleitorPrivateKey);
        Voting eleitorVotingContract = Voting.load(contractAddress, web3j, eleitorCredentials, gasProvider);
        TransactionReceipt receipt = eleitorVotingContract.votar(BigInteger.valueOf(idCandidato)).send();
        return receipt.getTransactionHash();
    }

    /**
     * Chama a função `obterCandidato` do smart contract `Voting`
     * para obter informações de um candidato e as mapeia para um objeto `Candidato`
     * Java.
     *
     * @param id O ID do candidato a ser consultado.
     * @return Candidato Um objeto `Candidato` contendo as informações obtidas do
     * contrato.
     * @throws RuntimeException Se ocorrer algum erro ao obter as informações do
     * contrato.
     */
    public Candidato obterCandidato(Long id) {
        try {
            Tuple3<BigInteger, String, BigInteger> result = votingContract.obterCandidato(BigInteger.valueOf(id))
                    .send();
            BigInteger candidatoId = result.getValue1();
            String nome = result.getValue2();
            BigInteger contagemDeVotos = result.getValue3();
            return new Candidato(candidatoId, nome, contagemDeVotos);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao obter informações do candidato", e);
            throw new RuntimeException("Erro ao obter informações do candidato: " + e.getMessage(), e);
        }
    }

    /**
     * Método para autorizar um eleitor, verificando suas credenciais em um banco de
     * dados.
     * Este método demonstra uma possível integração com um sistema de autenticação
     * externo.
     *
     * @param endereco O endereço Ethereum do eleitor a ser autorizado.
     * @param usuario  O nome de usuário do eleitor.
     * @param senha    A senha do eleitor.
     * @return String O hash da transação de autorização do eleitor, se as
     * credenciais forem válidas.
     * @throws EleitorException Se as credenciais fornecidas forem inválidas.
     * @throws RuntimeException Se ocorrer um erro ao autorizar o eleitor na
     * blockchain.
     */
    public String autorizarEleitorComCredenciais(String endereco, String usuario, String senha)
            throws EleitorException {
        if (verificarCredenciais(usuario, senha)) {
            try {
                return autorizarEleitor(endereco);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Erro ao autorizar o eleitor", e);
                throw new RuntimeException("Erro ao autorizar o eleitor: " + e.getMessage(), e);
            }
        } else {
            throw new EleitorException("Credenciais inválidas.");
        }
    }

    /**
     * Verifica as credenciais de um eleitor em um banco de dados usando
     * `JdbcTemplate`.
     *
     * @param usuario O nome de usuário do eleitor.
     * @param senha   A senha do eleitor.
     * @return boolean `true` se as credenciais forem válidas, `false` caso
     * contrário.
     */
    private boolean verificarCredenciais(String usuario, String senha) {
        String sql = "SELECT COUNT(*) FROM autorizarEleitor WHERE login = ? AND senha = ?";
        Integer count = jdbcTemplate.query(sql, new Object[]{usuario, senha}, rs -> rs.next() ? rs.getInt(1) : 0);
        return count != null && count > 0;
    }

    /**
     * Verifica o status da conexão com o nó Ethereum.
     *
     * @return String Uma mensagem indicando o status da conexão.
     */
    public String verificarStatusContrato() {
        try {
            Web3ClientVersion clientVersion = web3j.web3ClientVersion().send();
            return "Conectado ao nó Ethereum: " + clientVersion.getWeb3ClientVersion();
        } catch (Exception e) {
            return "Erro ao conectar ao contrato: " + e.getMessage();
        }
    }
}