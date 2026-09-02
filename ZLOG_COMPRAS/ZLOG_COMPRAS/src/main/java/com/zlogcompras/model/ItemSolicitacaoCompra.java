package com.zlogcompras.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "itens_solicitacao_compra")
public class ItemSolicitacaoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitacao_compra_id")
    private SolicitacaoCompra solicitacaoCompra;

    private String materialServico;
    private Integer quantidade;
    private String descricao;

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

    public String getMaterialServico() {
        return materialServico;
    }

    public void setMaterialServico(String materialServico) {
        this.materialServico = materialServico;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setStatus(String novoStatus) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}