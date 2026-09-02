package com.zlogcompras.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zlogcompras.model.Orcamento;
import com.zlogcompras.repository.OrcamentoRepository;

@Service
public class OrcamentoService {

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    public List<Orcamento> listarTodosOrcamentos() {
        return orcamentoRepository.findAll();
    }

    public Optional<Orcamento> buscarOrcamentoPorId(Long id) {
        return orcamentoRepository.findById(id);
    }

    public Orcamento salvarOrcamento(Orcamento orcamento) {
        return orcamentoRepository.save(orcamento);
    }

    public List<Orcamento> buscarOrcamentosPorSolicitacao(Long solicitacaoId) {
        return orcamentoRepository.findBySolicitacaoCompraId(solicitacaoId);
    }

    public List<Orcamento> buscarOrcamentosPorItemSolicitacao(Long itemSolicitacaoId) {
        return orcamentoRepository.findByItemSolicitacaoCompraId(itemSolicitacaoId);
    }

    // Outros métodos para buscar orçamentos por fornecedor, status, etc., podem ser adicionados aqui
}