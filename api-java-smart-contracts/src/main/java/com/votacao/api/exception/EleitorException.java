package com.votacao.api.exception;

/**
 * Esta classe `EleitorException` representa uma exceção personalizada
 * para situações específicas relacionadas a eleitores dentro da aplicação
 * de votação. Similar a `CandidatoException`, ela estende `RuntimeException`,
 * tornando-a uma exceção não verificada (unchecked exception). O uso desta
 * exceção permite sinalizar e tratar erros que são específicos para a entidade
 * "eleitor" no contexto da sua aplicação.
 */
public class EleitorException extends RuntimeException {

    /**
     * Construtor que aceita uma mensagem detalhada sobre a exceção.
     * Esta mensagem deve descrever a natureza do problema relacionado ao eleitor
     * (por exemplo, "Eleitor com endereço X já votou", "Eleitor não autorizado").
     * @param message Uma string contendo detalhes sobre a exceção relacionada ao eleitor.
     */
    public EleitorException(String message) {
        super(message); // Chama o construtor da superclasse (RuntimeException) passando a mensagem de erro.
    }

    /**
     * Construtor que aceita uma mensagem detalhada e a causa original da exceção.
     * Isso é útil quando um erro relacionado ao eleitor é causado por outra exceção
     * (por exemplo, uma falha ao acessar o banco de dados de eleitores autorizados
     * ou um erro na interação com o smart contract ao verificar a autorização).
     * Encadear a causa original facilita a depuração e o rastreamento da origem do erro.
     * @param message Uma string contendo detalhes sobre a exceção relacionada ao eleitor.
     * @param cause A exceção que causou esta `EleitorException`.
     */
    public EleitorException(String message, Throwable cause) {
        super(message, cause); // Chama o construtor da superclasse (RuntimeException) passando a mensagem de erro e a exceção original.
    }
}