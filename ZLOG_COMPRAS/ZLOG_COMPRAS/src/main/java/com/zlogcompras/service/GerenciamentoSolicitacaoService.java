package com.zlogcompras.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zlogcompras.model.SolicitacaoCompra;

@Service
public class GerenciamentoSolicitacaoService {

    @Autowired
    private SolicitacaoCompraService solicitacaoCompraService;

    @Autowired
    private EstoqueService estoqueService;

    @Transactional
    public SolicitacaoCompra criarNovaSolicitacao(SolicitacaoCompra solicitacao) {
        SolicitacaoCompra novaSolicitacao = solicitacaoCompraService.criarSolicitacaoBase(solicitacao);
        solicitacaoCompraService.salvarItensSolicitacao(novaSolicitacao, solicitacao.getItens());
        solicitacao.getItens().forEach(estoqueService::verificarEstoque);
        return novaSolicitacao;
    }
}