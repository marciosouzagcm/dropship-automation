package com.zlogcompras.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired; // Para aprovar o orçamento
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zlogcompras.model.Orcamento;
import com.zlogcompras.service.OrcamentoService;
import com.zlogcompras.service.ProcessoCompraService;

@RestController
@RequestMapping("/api/orcamentos")
public class OrcamentoController {

    @Autowired
    private OrcamentoService orcamentoService;

    @Autowired
    private ProcessoCompraService processoCompraService;

    @GetMapping
    public ResponseEntity<List<Orcamento>> listarTodosOrcamentos() {
        List<Orcamento> orcamentos = orcamentoService.listarTodosOrcamentos();
        return new ResponseEntity<>(orcamentos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orcamento> buscarOrcamentoPorId(@PathVariable Long id) {
        Optional<Orcamento> orcamento = orcamentoService.buscarOrcamentoPorId(id);
        return orcamento.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Orcamento> criarOrcamento(@RequestBody Orcamento orcamento) {
        Orcamento novoOrcamento = orcamentoService.salvarOrcamento(orcamento);
        return new ResponseEntity<>(novoOrcamento, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Orcamento> atualizarOrcamento(@PathVariable Long id, @RequestBody Orcamento orcamentoAtualizado) {
        Optional<Orcamento> orcamentoExistente = orcamentoService.buscarOrcamentoPorId(id);
        if (orcamentoExistente.isPresent()) {
            orcamentoAtualizado.setId(id);
            Orcamento orcamentoSalvo = orcamentoService.salvarOrcamento(orcamentoAtualizado);
            return new ResponseEntity<>(orcamentoSalvo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/solicitacao/{solicitacaoId}")
    public ResponseEntity<List<Orcamento>> buscarOrcamentosPorSolicitacao(@PathVariable Long solicitacaoId) {
        List<Orcamento> orcamentos = orcamentoService.buscarOrcamentosPorSolicitacao(solicitacaoId);
        return new ResponseEntity<>(orcamentos, HttpStatus.OK);
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<Void> aprovarOrcamentoGerarPedido(@PathVariable Long id) {
        processoCompraService.aprovarOrcamentoGerarPedido(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}