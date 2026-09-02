package com.votacao.api.exception;

/**
 * Esta classe `CandidatoNotFoundException` representa uma exceção específica
 * que indica que um candidato procurado na aplicação de votação não foi encontrado.
 * Ela estende a classe `RuntimeException`, tornando-a uma exceção não verificada
 * (unchecked exception). O uso de uma exceção específica para este cenário
 * melhora a clareza do código e permite um tratamento mais direcionado quando
 * um candidato não é encontrado.
 */
public class CandidatoNotFoundException extends RuntimeException {

    /**
     * Construtor que aceita uma mensagem detalhada sobre a exceção.
     * Esta mensagem deve especificar por que o candidato não foi encontrado
     * (por exemplo, "Candidato com ID 123 não encontrado").
     * @param message Uma string descrevendo o motivo pelo qual o candidato não foi encontrado.
     */
    public CandidatoNotFoundException(String message) {
        super(message); // Chama o construtor da superclasse (RuntimeException) passando a mensagem de erro.
    }

    /**
     * Construtor que aceita uma mensagem detalhada e a causa original da exceção.
     * Isso é útil quando a falha em encontrar o candidato é resultado de outra
     * exceção (por exemplo, um erro de acesso ao banco de dados ou uma falha na
     * comunicação com o blockchain). Encadear a causa original ajuda no diagnóstico
     * do problema subjacente.
     * @param message Uma string descrevendo o motivo pelo qual o candidato não foi encontrado.
     * @param cause A exceção que levou à falha em encontrar o candidato.
     */
    public CandidatoNotFoundException(String message, Throwable cause) {
        super(message, cause); // Chama o construtor da superclasse (RuntimeException) passando a mensagem de erro e a exceção original.
    }
}