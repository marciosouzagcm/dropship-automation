package com.zlogcompras.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "pedidos_compra")
public class PedidoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataPedido;
    private Long fornecedorId; // Referência ao fornecedor
    private String status; // Ex: "Pendente", "Enviado", "Recebido"

    @OneToMany(mappedBy = "pedidoCompra", cascade = CascadeType.ALL)
    private List<ItemPedidoCompra> itens;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(LocalDate dataPedido) {
        this.dataPedido = dataPedido;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ItemPedidoCompra> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedidoCompra> itens) {
        this.itens = itens;
    }
}