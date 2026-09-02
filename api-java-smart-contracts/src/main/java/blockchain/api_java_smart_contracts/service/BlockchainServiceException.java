package blockchain.api_java_smart_contracts.service;

public class BlockchainServiceException extends Exception {
    // Construtor que aceita apenas uma mensagem como argumento
    public BlockchainServiceException(String message) {
        super(message); // Chama o construtor da classe pai (Exception) passando a mensagem de erro
    }

    // Construtor que aceita uma mensagem e a causa da exceção como argumentos
    public BlockchainServiceException(String message, Throwable cause) {
        super(message, cause); // Chama o construtor da classe pai (Exception) passando a mensagem de erro e a exceção original que causou esta exceção
    }
}