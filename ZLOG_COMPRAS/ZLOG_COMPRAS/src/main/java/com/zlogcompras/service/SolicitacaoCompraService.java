package com.zlogcompras.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zlogcompras.model.ItemSolicitacaoCompra;
import com.zlogcompras.model.SolicitacaoCompra;
import com.zlogcompras.repository.ItemSolicitacaoCompraRepository;
import com.zlogcompras.repository.SolicitacaoCompraRepository;

@Service
public class SolicitacaoCompraService {

    @Autowired
    private SolicitacaoCompraRepository solicitacaoCompraRepository;

    @Autowired
    private ItemSolicitacaoCompraRepository itemSolicitacaoCompraRepository;

    public SolicitacaoCompra criarSolicitacaoBase(SolicitacaoCompra solicitacao) {
        solicitacao.setDataSolicitacao(LocalDate.now());
        solicitacao.setStatus("Pendente");
        return solicitacaoCompraRepository.save(solicitacao);
    }

    public void salvarItensSolicitacao(SolicitacaoCompra solicitacao, List<ItemSolicitacaoCompra> itens) {
        if (itens != null && !itens.isEmpty()) {
            for (ItemSolicitacaoCompra item : itens) {
                item.setSolicitacaoCompra(solicitacao);
                itemSolicitacaoCompraRepository.save(item);
            }
        }
    }

    public SolicitacaoCompra buscarSolicitacaoPorId(Long id) {
        return solicitacaoCompraRepository.findById(id).orElse(null);
    }

    public List<SolicitacaoCompra> listarTodasSolicitacoes() {
        return solicitacaoCompraRepository.findAll();
    }

    public void atualizarStatusSolicitacao(Long id, String novoStatus) {
        SolicitacaoCompra solicitacao = buscarSolicitacaoPorId(id);
        if (solicitacao != null) {
            solicitacao.setStatus(novoStatus);
            solicitacaoCompraRepository.save(solicitacao);
        }
    }
}