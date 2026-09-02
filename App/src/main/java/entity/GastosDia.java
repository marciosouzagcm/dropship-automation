package entity;

import java.time.LocalDate; // Importação da classe LocalDate para representar a data

public class GastosDia {

    // Atributos da classe Gastos
    private LocalDate data; // Data do gasto
    private float aluguelParcelamentoVeiculo; // Aluguel ou parcelamento do veículo
    private float combustivel; // Gasto com combustível
    private float pedagio; // Gasto com pedágio
    private float multa; // Gasto com multas
    private float alimentacao; // Gasto com alimentação
    private float manutencaoVeiculo; // Gasto com manutenção do veículo
    private float investimento; // Gasto com investimentos
    private float consorcio; // Gasto com consórcio

    // Getter e Setter para o atributo data
    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    // Getter e Setter para o atributo aluguelParcelamentoVeiculo
    public float getAluguelParcelamentoVeiculo() {
        return aluguelParcelamentoVeiculo;
    }

    public void setAluguelParcelamentoVeiculo(float aluguelParcelamentoVeiculo) {
        this.aluguelParcelamentoVeiculo = aluguelParcelamentoVeiculo;
    }

    // Getter e Setter para o atributo combustivel
    public float getCombustivel() {
        return combustivel;
    }

    public void setCombustivel(float combustivel) {
        this.combustivel = combustivel;
    }

    // Getter e Setter para o atributo pedagio
    public float getPedagio() {
        return pedagio;
    }

    public void setPedagio(float pedagio) {
        this.pedagio = pedagio;
    }

    // Getter e Setter para o atributo multa
    public float getMulta() {
        return multa;
    }

    public void setMulta(float multa) {
        this.multa = multa;
    }

    // Getter e Setter para o atributo alimentacao
    public float getAlimentacao() {
        return alimentacao;
    }

    public void setAlimentacao(float alimentacao) {
        this.alimentacao = alimentacao;
    }

    // Getter e Setter para o atributo manutencaoVeiculo
    public float getManutencaoVeiculo() {
        return manutencaoVeiculo;
    }

    public void setManutencaoVeiculo(float manutencaoVeiculo) {
        this.manutencaoVeiculo = manutencaoVeiculo;
    }

    // Getter e Setter para o atributo investimento
    public float getInvestimento() {
        return investimento;
    }

    public void setInvestimento(float investimento) {
        this.investimento = investimento;
    }

    // Getter e Setter para o atributo consorcio
    public float getConsorcio() {
        return consorcio;
    }

    public void setConsorcio(float consorcio) {
        this.consorcio = consorcio;
    }
}