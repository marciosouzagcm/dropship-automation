package com.zlogcompras.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zlogcompras.model.SolicitacaoCompra;
import com.zlogcompras.service.GerenciamentoSolicitacaoService;
import com.zlogcompras.service.SolicitacaoCompraService;

@RestController
@RequestMapping("/api/solicitacoes")
public class SolicitacaoCompraController {

    @Autowired
    private GerenciamentoSolicitacaoService gerenciamentoSolicitacaoService;

    @Autowired
    private SolicitacaoCompraService solicitacaoCompraService;

    @PostMapping
    public ResponseEntity<SolicitacaoCompra> criarSolicitacao(@RequestBody SolicitacaoCompra solicitacao) {
        SolicitacaoCompra novaSolicitacao = gerenciamentoSolicitacaoService.criarNovaSolicitacao(solicitacao);
        return new ResponseEntity<>(novaSolicitacao, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SolicitacaoCompra> buscarSolicitacao(@PathVariable Long id) {
        SolicitacaoCompra solicitacao = solicitacaoCompraService.buscarSolicitacaoPorId(id);
        if (solicitacao != null) {
            return new ResponseEntity<>(solicitacao, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<java.util.List<SolicitacaoCompra>> listarTodasSolicitacoes() {
        java.util.List<SolicitacaoCompra> solicitacoes = solicitacaoCompraService.listarTodasSolicitacoes();
        return new ResponseEntity<>(solicitacoes, HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatusSolicitacao(@PathVariable Long id, @RequestParam String novoStatus) {
        solicitacaoCompraService.atualizarStatusSolicitacao(id, novoStatus);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}