package blockchain.api_java_smart_contracts.model;

import java.math.BigInteger;
import java.util.Objects;

public class Candidato {

    private BigInteger id; // Identificador único do candidato
    private String nome; // Nome do candidato
    private BigInteger votos; // Número de votos recebidos pelo candidato

    // Construtor padrão sem argumentos
    public Candidato() {
        // Inicializa um objeto Candidato vazio
    }

    // Construtor com argumentos para inicializar todos os atributos
    public Candidato(BigInteger id, String nome, BigInteger votos) {
        this.id = id;
        this.nome = nome;
        this.votos = votos;
    }

    // Método getter para o atributo 'id'
    public BigInteger getId() {
        return id;
    }

    // Método setter para o atributo 'id'
    public void setId(BigInteger id) {
        this.id = id;
    }

    // Método getter para o atributo 'nome'
    public String getNome() {
        return nome;
    }

    // Método setter para o atributo 'nome'
    public void setNome(String nome) {
        this.nome = nome;
    }

    // Método getter para o atributo 'votos'
    public BigInteger getVotos() {
        return votos;
    }

    // Método setter para o atributo 'votos'
    public void setVotos(BigInteger votos) {
        this.votos = votos;
    }

    // Sobrescrita do método equals para comparar objetos Candidato pelo seu conteúdo
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Se os objetos são a mesma instância, são iguais
        if (o == null || getClass() != o.getClass()) return false; // Se o objeto é nulo ou de uma classe diferente, não são iguais
        Candidato candidato = (Candidato) o; // Faz um cast do objeto para a classe Candidato
        return Objects.equals(id, candidato.id) && Objects.equals(nome, candidato.nome) && Objects.equals(votos, candidato.votos); // Compara os atributos
    }

    // Sobrescrita do método hashCode para gerar um código hash para o objeto
    @Override
    public int hashCode() {
        return Objects.hash(id, nome, votos); // Gera um hash baseado nos atributos
    }

    // Sobrescrita do método toString para fornecer uma representação em String do objeto
    @Override
    public String toString() {
        return "Candidato{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", votos=" + votos +
                '}';
    }

    // Método getter obsoleto para o nome do candidato (para compatibilidade com versões antigas)
    @Deprecated
    public String getNomeCandidato() {
        return this.nome;
    }

    // Método setter obsoleto para o nome do candidato (para compatibilidade com versões antigas)
    @Deprecated
    public void setNomeCandidato(String nomeCandidato) {
        this.nome = nomeCandidato;
        this.nome = nomeCandidato; // Duplicação intencional para manter a assinatura do método deprecated
    }

    // Método getter obsoleto para o número de votos (para compatibilidade com versões antigas)
    @Deprecated
    public BigInteger getNumeroVotos() {
        return this.votos;
    }

    // Método setter obsoleto para o número de votos (para compatibilidade com versões antigas)
    @Deprecated
    public void setNumeroVotos(BigInteger numeroVotos) {
        this.votos = numeroVotos;
        this.votos = numeroVotos; // Duplicação intencional para manter a assinatura do método deprecated
    }

    public Object getContagemDeVotos() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getContagemDeVotos'");
    }
}