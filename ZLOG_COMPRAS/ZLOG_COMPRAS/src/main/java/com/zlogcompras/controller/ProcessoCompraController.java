package com.zlogcompras.controller;

import com.zlogcompras.service.ProcessoCompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/processo-compra")
public class ProcessoCompraController {

    @Autowired
    private ProcessoCompraService processoCompraService;

    @PostMapping("/iniciar/{solicitacaoId}")
    public ResponseEntity<Void> iniciarProcessoCompra(@PathVariable Long solicitacaoId) {
        // Primeiro, precisamos buscar a solicitação de compra pelos itens que não estão em estoque
        // Podemos delegar essa lógica para o ProcessoCompraService

        // Por enquanto, vamos simplificar e assumir que queremos iniciar o processo
        // de compra para todos os itens da solicitação que não foram atendidos.

        // Uma abordagem melhor seria ter no ItemSolicitacaoCompra um status (ex: "Pendente", "Em Estoque", "Aguardando Compra")
        // e iniciar o processo apenas para os itens com status "Aguardando Compra".

        // Para simplificar o protótipo, vamos buscar a solicitação e iniciar o processo
        // de solicitação de orçamentos para ela.

        // Uma forma de fazer isso seria adicionar um método no ProcessoCompraService
        // que recebe o ID da solicitação e inicia o fluxo.

        // Vamos chamar o método 'solicitarOrcamentos' que já criamos no ProcessoCompraService
        // (precisaremos ajustá-lo para receber o ID da solicitação).

        // Ajustando o ProcessoCompraService para receber o ID da solicitação:
        // (Voltaremos e ajustaremos o ProcessoCompraService)

        // Por enquanto, vamos apenas chamar um método que inicia o processo
        // para a solicitação inteira.

        // Supondo que tenhamos um método no ProcessoCompraService para isso:
        // processoCompraService.iniciarProcessoCompraParaSolicitacao(solicitacaoId);

        // Como o método 'iniciarProcessoCompra' atual no ProcessoCompraService
        // recebe um ItemSolicitacaoCompra, precisaremos adaptá-lo ou criar um novo.

        // Vamos criar um novo método no ProcessoCompraService para iniciar
        // o processo com base no ID da solicitação.

        processoCompraService.iniciarProcessoCompraPorSolicitacaoId(solicitacaoId);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}