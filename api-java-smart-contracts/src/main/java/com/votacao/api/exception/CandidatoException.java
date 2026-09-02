package com.votacao.api.exception;

/**
 * Esta classe `CandidatoException` representa uma exceção personalizada
 * para situações específicas relacionadas a candidatos dentro da aplicação
 * de votação. Ela estende a classe `RuntimeException`, o que significa que
 * é uma exceção não verificada (unchecked exception). Exceções não verificadas
 * não precisam ser explicitamente declaradas nos métodos que podem lançá-las
 * ou tratadas com blocos `try-catch`, embora seja uma boa prática tratá-las
 * quando apropriado para evitar o encerramento abrupto da aplicação.
 *
 * O uso de exceções personalizadas como esta ajuda a tornar o código mais
 * semântico e facilita a identificação e o tratamento de erros específicos
 * do domínio da aplicação.
 */
public class CandidatoException extends RuntimeException {

    /**
     * Construtor que aceita uma mensagem detalhada sobre a exceção.
     * Esta mensagem pode descrever a natureza específica do problema que ocorreu.
     * @param message Uma string contendo detalhes sobre a exceção.
     */
    public CandidatoException(String message) {
        super(message); // Chama o construtor da superclasse (RuntimeException) passando a mensagem de erro.
    }

    /**
     * Construtor que aceita uma mensagem detalhada e a causa original da exceção.
     * Isso é útil quando a exceção atual é resultado de outra exceção (por exemplo,
     * uma exceção de baixo nível da biblioteca Web3j causando uma exceção de
     * alto nível específica do domínio). Encadear a causa original ajuda na
     * depuração e no rastreamento da origem do problema.
     * @param message Uma string contendo detalhes sobre a exceção.
     * @param cause A exceção que causou esta exceção.
     */
    public CandidatoException(String message, Throwable cause) {
        super(message, cause); // Chama o construtor da superclasse (RuntimeException) passando a mensagem de erro e a exceção original.
    }
}