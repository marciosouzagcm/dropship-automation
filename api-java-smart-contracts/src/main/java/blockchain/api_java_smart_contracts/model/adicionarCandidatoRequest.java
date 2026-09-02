package blockchain.api_java_smart_contracts.model;

public class adicionarCandidatoRequest {

    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // Construtor vazio (necessário para desserialização do JSON)
    public adicionarCandidatoRequest() {
    }

    public adicionarCandidatoRequest(String nome) {
        this.nome = nome;
    }
}