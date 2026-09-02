package entity;

public class Motorista {

    // Atributos da classe Motorista
    private String nome; // Nome do motorista
    private String placaVeiculo; // Placa do veículo no formato XXX-XXXX

    // Construtor da classe Motorista
    public Motorista(String nome, String placaVeiculo) {
                    this.nome = nome;
                    this.placaVeiculo = placaVeiculo;
    }

    public Motorista() {
        //TODO Auto-generated constructor stub
    }

    // Getter e Setter para o atributo nome
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // Getter e Setter para o atributo placaVeiculo
    public String getPlacaVeiculo() {
        return placaVeiculo;
    }

    public void setPlacaVeiculo(String placaVeiculo) {
        // Validação para garantir que a placa está no formato XXX-XXXX
        if (placaVeiculo.matches("[A-Z]{3}-\\d{4}")) {
            this.placaVeiculo = placaVeiculo;
        } else {
            throw new IllegalArgumentException("Placa do veículo deve estar no formato XXX-XXXX");
        }
    }
}