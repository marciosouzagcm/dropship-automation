package com.zlogcompras.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "solicitacoes_compra")
public class SolicitacaoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataSolicitacao;
    private String solicitante;
    private String status; // Ex: "Pendente", "Aprovada", "Em Estoque", "Aguardando Orçamento", "Compra Autorizada", "Gerada OC", "Enviada Fornecedor", "Recebida", "Enviada Obra", "Contabilizada"

    @OneToMany(mappedBy = "solicitacaoCompra", cascade = CascadeType.ALL)
    private List<ItemSolicitacaoCompra> itens;

    // Getters e Setters (podes usar o Lombok para gerar automaticamente)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDate dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public String getSolicitante() {
        return solicitante;
    }

    public void setSolicitante(String solicitante) {
        this.solicitante = solicitante;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ItemSolicitacaoCompra> getItens() {
        return itens;
    }

    public void setItens(List<ItemSolicitacaoCompra> itens) {
        this.itens = itens;
    }
}