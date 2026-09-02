package com.zlogcompras.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zlogcompras.model.ItemPedidoCompra;

public interface ItemPedidoCompraRepository extends JpaRepository<ItemPedidoCompra, Long> {
    List<ItemPedidoCompra> findByPedidoCompraId(Long pedidoCompraId);
    // Outros métodos personalizados, se necessário
}