package com.zlogcompras.model;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "orcamentos")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitacao_compra_id")
    private SolicitacaoCompra solicitacaoCompra;

    @ManyToOne
    @JoinColumn(name = "item_solicitacao_compra_id") // Adicione esta anotação
    private ItemSolicitacaoCompra itemSolicitacaoCompra; // Adicione este campo

    private Long fornecedorId;
    private LocalDate dataOrcamento;
    private Double valorTotal;
    private String numeroOrcamento;
    private String status;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SolicitacaoCompra getSolicitacaoCompra() {
        return solicitacaoCompra;
    }

    public void setSolicitacaoCompra(SolicitacaoCompra solicitacaoCompra) {
        this.solicitacaoCompra = solicitacaoCompra;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public LocalDate getDataOrcamento() {
        return dataOrcamento;
    }

    public void setDataOrcamento(LocalDate dataOrcamento) {
        this.dataOrcamento = dataOrcamento;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getNumeroOrcamento() {
        return numeroOrcamento;
    }

    public void setNumeroOrcamento(String numeroOrcamento) {
        this.numeroOrcamento = numeroOrcamento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ItemSolicitacaoCompra getItemSolicitacaoCompra() { // Getter para o novo campo
        return itemSolicitacaoCompra;
    }

    public void setItemSolicitacaoCompra(ItemSolicitacaoCompra itemSolicitacaoCompra) { // Setter para o novo campo
        this.itemSolicitacaoCompra = itemSolicitacaoCompra;
    }
}
