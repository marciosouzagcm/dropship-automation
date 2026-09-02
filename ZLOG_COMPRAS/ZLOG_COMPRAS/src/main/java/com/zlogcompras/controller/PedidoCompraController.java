package com.zlogcompras.controller;

import java.util.List;
import java.util.Optional;

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

import com.zlogcompras.model.PedidoCompra;
import com.zlogcompras.service.PedidoCompraService;

@RestController
@RequestMapping("/api/pedidos-compra")
public class PedidoCompraController {

    @Autowired
    private PedidoCompraService pedidoCompraService;

    @GetMapping
    public ResponseEntity<List<PedidoCompra>> listarTodosPedidosCompra() {
        List<PedidoCompra> pedidos = pedidoCompraService.listarTodosPedidosCompra();
        return new ResponseEntity<>(pedidos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoCompra> buscarPedidoCompraPorId(@PathVariable Long id) {
        Optional<PedidoCompra> pedido = pedidoCompraService.buscarPedidoCompraPorId(id);
        return pedido.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<PedidoCompra> criarPedidoCompra(@RequestBody PedidoCompra pedidoCompra) {
        PedidoCompra novoPedido = pedidoCompraService.criarPedidoCompra(pedidoCompra);
        return new ResponseEntity<>(novoPedido, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoCompra> atualizarPedidoCompra(@PathVariable Long id, @RequestBody PedidoCompra pedidoCompraAtualizado) {
        Optional<PedidoCompra> pedidoExistente = pedidoCompraService.buscarPedidoCompraPorId(id);
        if (pedidoExistente.isPresent()) {
            pedidoCompraAtualizado.setId(id);
            PedidoCompra pedidoSalvo = pedidoCompraService.atualizarPedidoCompra(pedidoCompraAtualizado);
            return new ResponseEntity<>(pedidoSalvo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatusPedidoCompra(@PathVariable Long id, @RequestParam String novoStatus) {
        pedidoCompraService.atualizarStatusPedidoCompra(id, novoStatus);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Outros endpoints que podem ser úteis:
    // - Buscar pedidos de compra por fornecedor
    // - Buscar pedidos de compra por status
    // - Endpoint para adicionar itens a um pedido de compra existente
}