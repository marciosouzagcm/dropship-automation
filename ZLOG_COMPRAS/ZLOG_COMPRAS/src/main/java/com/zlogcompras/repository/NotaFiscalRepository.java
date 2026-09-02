package com.zlogcompras.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zlogcompras.model.NotaFiscal;

public interface NotaFiscalRepository extends JpaRepository<NotaFiscal, Long> {
    List<NotaFiscal> findByPedidoCompraId(Long pedidoCompraId);
    // Outros métodos personalizados, se necessário
}