package com.zlogcompras.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zlogcompras.model.ItemSolicitacaoCompra;

public interface ItemSolicitacaoCompraRepository extends JpaRepository<ItemSolicitacaoCompra, Long> {
    List<ItemSolicitacaoCompra> findBySolicitacaoCompraId(Long solicitacaoCompraId);
    // Outros métodos personalizados, se necessário
}