package blockchain.api_java_smart_contracts.service;

public class CandidatoServiceException extends Exception {
    // Construtor que recebe uma mensagem de erro como argumento.
    // Este construtor é usado quando você quer fornecer uma descrição simples do erro.
    public CandidatoServiceException(String message) {
        super(message); // Chama o construtor da classe pai (Exception) passando a mensagem.
    }

    // Construtor que recebe uma mensagem de erro e a causa original do erro como argumentos.
    // Este construtor é útil para encadear exceções, permitindo que você saiba a causa raiz do problema.
    public CandidatoServiceException(String message, Throwable cause) {
        super(message, cause); // Chama o construtor da classe pai (Exception) passando a mensagem e a causa.
    }
}