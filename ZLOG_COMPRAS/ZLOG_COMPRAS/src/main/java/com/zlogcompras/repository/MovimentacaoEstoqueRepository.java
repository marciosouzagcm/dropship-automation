package com.zlogcompras.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zlogcompras.model.MovimentacaoEstoque;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {
    List<MovimentacaoEstoque> findByCodigoMaterialOrderByDataMovimentacaoDesc(String codigoMaterial);
    // Outros métodos personalizados para rastreamento, se necessário
}