package com.votacao.api.exception;

/**
 * Esta classe `VotingException` representa uma exceção personalizada
 * para situações gerais de erro relacionadas ao processo de votação
 * dentro da aplicação. Ela estende `RuntimeException`, sendo uma exceção
 * não verificada (unchecked exception). O uso desta exceção permite
 * agrupar erros que não se encaixam especificamente nas categorias de
 * `CandidatoException` ou `EleitorException`, mas ainda são relevantes
 * para a lógica de votação.
 */
public class VotingException extends RuntimeException {

    /**
     * Construtor que aceita uma mensagem detalhada sobre a exceção.
     * Esta mensagem deve descrever a natureza do erro no processo de votação
     * (por exemplo, "Erro ao registrar o voto", "Período de votação encerrado").
     * @param message Uma string contendo detalhes sobre a exceção de votação.
     */
    public VotingException(String message) {
        super(message); // Chama o construtor da superclasse (RuntimeException) passando a mensagem de erro.
    }

    /**
     * Construtor que aceita uma mensagem detalhada e a causa original da exceção.
     * Isso é útil quando um erro de votação é causado por outra exceção, como
     * uma falha na comunicação com o smart contract durante o registro do voto.
     * Encadear a causa original ajuda na depuração e no rastreamento da origem do problema.
     * @param message Uma string contendo detalhes sobre a exceção de votação.
     * @param cause A exceção que causou esta `VotingException`.
     */
    public VotingException(String message, Throwable cause) {
        super(message, cause); // Chama o construtor da superclasse (RuntimeException) passando a mensagem de erro e a exceção original.
    }
}