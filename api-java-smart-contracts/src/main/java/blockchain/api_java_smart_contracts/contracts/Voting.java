package blockchain.api_java_smart_contracts.contracts;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.9.4.
 */
@SuppressWarnings("rawtypes") // Suprime avisos relacionados a tipos genéricos não especificados.
public class Voting extends Contract {
    public static final String BINARY = "Bin file was not provided"; // Constante para o código binário do contrato inteligente (geralmente carregado de um arquivo .bin).

    // Constantes para os nomes das funções definidas no contrato inteligente Solidity.
    public static final String FUNC_ADICIONARCANDIDATO = "adicionarCandidato";
    public static final String FUNC_ADMINISTRADOR = "administrador";
    public static final String FUNC_AUTORIZARELEITOR = "autorizarEleitor";
    public static final String FUNC_CANDIDATOS = "candidatos";
    public static final String FUNC_CONTADORDECANDIDATOS = "contadorDeCandidatos";
    public static final String FUNC_ELEITORES = "eleitores";
    public static final String FUNC_OBTERCANDIDATO = "obterCandidato";
    public static final String FUNC_OBTERTOTALDECANDIDATOS = "obterTotalDeCandidatos";
    public static final String FUNC_VERIFICARSEVOTOU = "verificarSeVotou";
    public static final String FUNC_VOTAR = "votar";

    // Definição dos eventos que o contrato inteligente pode emitir.
    public static final Event CANDIDATOADICIONADO_EVENT = new Event("CandidatoAdicionado",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event ELEITORAUTORIZADO_EVENT = new Event("EleitorAutorizado",
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Bool>() {}));
    ;

    public static final Event VOTOFALHOU_EVENT = new Event("VotoFalhou",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}));
    ;

    public static final Event VOTOREGISTRADO_EVENT = new Event("VotoRegistrado",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}));
    ;

    // Construtores da classe Voting. Estes métodos são usados para criar instâncias da classe,
    // conectando-se a um contrato inteligente já implantado na blockchain Ethereum.
    @Deprecated // Indica que este construtor está obsoleto e pode ser removido em versões futuras.
    protected Voting(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Voting(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected Voting(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected Voting(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    // Métodos estáticos para obter os eventos CandidatoAdicionado de um recibo de transação.
    // Isso permite analisar os logs de uma transação para verificar se o evento foi emitido e obter seus valores.
    public static List<CandidatoAdicionadoEventResponse> getCandidatoAdicionadoEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(CANDIDATOADICIONADO_EVENT, transactionReceipt);
        ArrayList<CandidatoAdicionadoEventResponse> responses = new ArrayList<CandidatoAdicionadoEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            CandidatoAdicionadoEventResponse typedResponse = new CandidatoAdicionadoEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.nome = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    // Métodos para observar eventos CandidatoAdicionado em tempo real usando Flowable (programação reativa).
    // Isso permite que sua aplicação reaja quando um novo evento CandidatoAdicionado é emitido na blockchain.
    public Flowable<CandidatoAdicionadoEventResponse> candidatoAdicionadoEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, CandidatoAdicionadoEventResponse>() {
            @Override
            public CandidatoAdicionadoEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(CANDIDATOADICIONADO_EVENT, log);
                CandidatoAdicionadoEventResponse typedResponse = new CandidatoAdicionadoEventResponse();
                typedResponse.log = log;
                typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.nome = (String) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<CandidatoAdicionadoEventResponse> candidatoAdicionadoEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(CANDIDATOADICIONADO_EVENT));
        return candidatoAdicionadoEventFlowable(filter);
    }

    // Métodos estáticos para obter os eventos EleitorAutorizado de um recibo de transação.
    public static List<EleitorAutorizadoEventResponse> getEleitorAutorizadoEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ELEITORAUTORIZADO_EVENT, transactionReceipt);
        ArrayList<EleitorAutorizadoEventResponse> responses = new ArrayList<EleitorAutorizadoEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            EleitorAutorizadoEventResponse typedResponse = new EleitorAutorizadoEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.eleitor = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.autorizado = (Boolean) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    // Métodos para observar eventos EleitorAutorizado em tempo real usando Flowable.
    public Flowable<EleitorAutorizadoEventResponse> eleitorAutorizadoEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, EleitorAutorizadoEventResponse>() {
            @Override
            public EleitorAutorizadoEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(ELEITORAUTORIZADO_EVENT, log);
                EleitorAutorizadoEventResponse typedResponse = new EleitorAutorizadoEventResponse();
                typedResponse.log = log;
                typedResponse.eleitor = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.autorizado = (Boolean) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<EleitorAutorizadoEventResponse> eleitorAutorizadoEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ELEITORAUTORIZADO_EVENT));
        return eleitorAutorizadoEventFlowable(filter);
    }

    // Métodos estáticos para obter os eventos VotoFalhou de um recibo de transação.
    public static List<VotoFalhouEventResponse> getVotoFalhouEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(VOTOFALHOU_EVENT, transactionReceipt);
        ArrayList<VotoFalhouEventResponse> responses = new ArrayList<VotoFalhouEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            VotoFalhouEventResponse typedResponse = new VotoFalhouEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.idCandidato = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.eleitor = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.motivo = (String) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    // Métodos para observar eventos VotoFalhou em tempo real usando Flowable.
    public Flowable<VotoFalhouEventResponse> votoFalhouEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, VotoFalhouEventResponse>() {
            @Override
            public VotoFalhouEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(VOTOFALHOU_EVENT, log);
                VotoFalhouEventResponse typedResponse = new VotoFalhouEventResponse();
                typedResponse.log = log;
                typedResponse.idCandidato = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.eleitor = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.motivo = (String) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<VotoFalhouEventResponse> votoFalhouEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(VOTOFALHOU_EVENT));
        return votoFalhouEventFlowable(filter);
    }

    // Métodos estáticos para obter os eventos VotoRegistrado de um recibo de transação.
    public static List<VotoRegistradoEventResponse> getVotoRegistradoEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(VOTOREGISTRADO_EVENT, transactionReceipt);
        ArrayList<VotoRegistradoEventResponse> responses = new ArrayList<VotoRegistradoEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            VotoRegistradoEventResponse typedResponse = new VotoRegistradoEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.nome = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.eleitor = (String) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    // Métodos para observar eventos VotoRegistrado em tempo real usando Flowable.
    public Flowable<VotoRegistradoEventResponse> votoRegistradoEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, VotoRegistradoEventResponse>() {
            @Override
            public VotoRegistradoEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(VOTOREGISTRADO_EVENT, log);
                VotoRegistradoEventResponse typedResponse = new VotoRegistradoEventResponse();
                typedResponse.log = log;
                typedResponse.id = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.nome = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.eleitor = (String) eventValues.getNonIndexedValues().get(2).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<VotoRegistradoEventResponse> votoRegistradoEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(VOTOREGISTRADO_EVENT));
        return votoRegistradoEventFlowable(filter);
    }

    // Métodos para interagir com as funções do contrato inteligente.
    // RemoteFunctionCall representa uma chamada para uma função do contrato que pode retornar um valor ou executar uma transação.

    // Função para adicionar um novo candidato.
    public RemoteFunctionCall<TransactionReceipt> adicionarCandidato(String _nome) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_ADICIONARCANDIDATO,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(_nome)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    // Função para obter o endereço do administrador do contrato.
    public RemoteFunctionCall<String> administrador() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ADMINISTRADOR,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    // Função para autorizar ou desautorizar um eleitor a votar.
    public RemoteFunctionCall<TransactionReceipt> autorizarEleitor(String _eleitor, Boolean _autorizado) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_AUTORIZARELEITOR,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _eleitor),
                        new org.web3j.abi.datatypes.Bool(_autorizado)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    // Função para obter informações de um candidato pelo seu ID.
    public RemoteFunctionCall<Tuple3<BigInteger, String, BigInteger>> candidatos(BigInteger param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_CANDIDATOS,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple3<BigInteger, String, BigInteger>>(function,
                new Callable<Tuple3<BigInteger, String, BigInteger>>() {
                    @Override
                    public Tuple3<BigInteger, String, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, String, BigInteger>(
                                (BigInteger) results.get(0).getValue(),
                                (String) results.get(1).getValue(),
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }

    // Função para obter o número total de candidatos registrados.
    public RemoteFunctionCall<BigInteger> contadorDeCandidatos() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_CONTADORDECANDIDATOS,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    // Função para obter informações sobre um eleitor pelo seu endereço.
    // Retorna uma tupla contendo se o eleitor está autorizado, se já votou e em qual candidato votou.
    public RemoteFunctionCall<Tuple3<Boolean, Boolean, BigInteger>> eleitores(String param0) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_ELEITORES,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple3<Boolean, Boolean, BigInteger>>(function,
                new Callable<Tuple3<Boolean, Boolean, BigInteger>>() {
                    @Override
                    public Tuple3<Boolean, Boolean, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<Boolean, Boolean, BigInteger>(
                                (Boolean) results.get(0).getValue(),
                                (Boolean) results.get(1).getValue(),
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }

    // Função para obter informações de um candidato pelo seu ID.
    public RemoteFunctionCall<Tuple3<BigInteger, String, BigInteger>> obterCandidato(BigInteger _id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OBTERCANDIDATO,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_id)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple3<BigInteger, String, BigInteger>>(function,
                new Callable<Tuple3<BigInteger, String, BigInteger>>() {
                    @Override
                    public Tuple3<BigInteger, String, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<BigInteger, String, BigInteger>(
                                (BigInteger) results.get(0).getValue(),
                                (String) results.get(1).getValue(),
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }

    // Função para obter o número total de candidatos registrados (similar a contadorDeCandidatos).
    public RemoteFunctionCall<BigInteger> obterTotalDeCandidatos() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OBTERTOTALDECANDIDATOS,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    // Função para verificar se um determinado eleitor já votou.
    public RemoteFunctionCall<Boolean> verificarSeVotou(String _eleitor) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_VERIFICARSEVOTOU,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, _eleitor)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    // Função para um eleitor autorizado votar em um candidato.
    public RemoteFunctionCall<TransactionReceipt> votar(BigInteger _id) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_VOTAR,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_id)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    // Métodos estáticos deprecated para carregar o contrato.
    @Deprecated
    public static Voting load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static Voting load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Voting(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    // Métodos estáticos para carregar o contrato, utilizando ContractGasProvider para o gerenciamento de gás.
    public static Voting load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new Voting(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static Voting load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new Voting(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    // Classes internas para representar as respostas dos eventos.
    public static class CandidatoAdicionadoEventResponse extends BaseEventResponse {
        public BigInteger id;
        public String nome;
    }

    public static class EleitorAutorizadoEventResponse extends BaseEventResponse {
        public String eleitor;
        public Boolean autorizado;
    }

    public static class VotoFalhouEventResponse extends BaseEventResponse {
        public BigInteger idCandidato;
        public String eleitor;
        public String motivo;
    }

    public static class VotoRegistradoEventResponse extends BaseEventResponse {
        public BigInteger id;
        public String nome;
        public String eleitor;
    }
}