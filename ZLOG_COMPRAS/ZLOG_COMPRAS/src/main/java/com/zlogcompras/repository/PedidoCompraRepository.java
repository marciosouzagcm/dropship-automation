package com.zlogcompras.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zlogcompras.model.PedidoCompra;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {
    // Métodos personalizados de consulta podem ser adicionados aqui, se necessário
}