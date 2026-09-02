package com.zlogcompras.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zlogcompras.model.Orcamento;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
    List<Orcamento> findBySolicitacaoCompraId(Long solicitacaoCompraId);
    List<Orcamento> findByItemSolicitacaoCompraId(Long itemSolicitacaoCompraId);
    // Outros métodos personalizados, como buscar por fornecedor ou status poderiam ser adicionados aqui
    // Exemplo:
    // List<Orcamento> findByFornecedorId(Long fornecedorId);
    // List<Orcamento> findByStatus(String status);

    public void save(Optional<Orcamento> orcamento);
}