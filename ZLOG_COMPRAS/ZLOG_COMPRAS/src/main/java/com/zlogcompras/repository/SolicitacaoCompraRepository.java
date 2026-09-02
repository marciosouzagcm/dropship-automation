package com.zlogcompras.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zlogcompras.model.SolicitacaoCompra;

public interface SolicitacaoCompraRepository extends JpaRepository<SolicitacaoCompra, Long> {
    // Métodos personalizados de consulta podem ser adicionados aqui, se necessário
}