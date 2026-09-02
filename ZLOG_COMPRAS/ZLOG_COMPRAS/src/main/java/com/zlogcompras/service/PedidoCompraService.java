package com.zlogcompras.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zlogcompras.model.ItemPedidoCompra;
import com.zlogcompras.model.PedidoCompra;
import com.zlogcompras.repository.ItemPedidoCompraRepository;
import com.zlogcompras.repository.PedidoCompraRepository;

@Service
public class PedidoCompraService {

    @Autowired
    private PedidoCompraRepository pedidoCompraRepository;

    @Autowired
    private ItemPedidoCompraRepository itemPedidoCompraRepository;
    private PedidoCompra pedidoAtualizado;

    // No futuro, podemos precisar de FornecedorService para validar o fornecedor

    public List<PedidoCompra> listarTodosPedidosCompra() {
        return pedidoCompraRepository.findAll();
    }

    public Optional<PedidoCompra> buscarPedidoCompraPorId(Long id) {
        return pedidoCompraRepository.findById(id);
    }

    @Transactional
    public PedidoCompra criarPedidoCompra(PedidoCompra pedidoCompra) {
        pedidoCompra.setDataPedido(LocalDate.now());
        pedidoCompra.setStatus("Pendente"); // Status inicial do pedido
        PedidoCompra novoPedido = pedidoCompraRepository.save(pedidoCompra);

        if (pedidoCompra.getItens() != null && !pedidoCompra.getItens().isEmpty()) {
            for (ItemPedidoCompra item : pedidoCompra.getItens()) {
                item.setPedidoCompra(novoPedido);
                itemPedidoCompraRepository.save(item);
            }
        }

        return novoPedido;
    }

    @Transactional
    public PedidoCompra atualizarPedidoCompra(PedidoCompra pedidoCompraAtualizado) {
        PedidoCompra pedidoExistente = pedidoCompraRepository.findById(pedidoCompraAtualizado.getId())
                .orElseThrow(() -> new RuntimeException("Pedido de compra não encontrado com ID: " + pedidoCompraAtualizado.getId()));

        pedidoAtualizado.setDataPedido(pedidoExistente.getDataPedido()); // Mantém a data original de criação
        return pedidoCompraRepository.save(pedidoCompraAtualizado);
    }

    public void atualizarStatusPedidoCompra(Long id, String novoStatus) {
        Optional<PedidoCompra> pedidoExistente = pedidoCompraRepository.findById(id);
        pedidoExistente.ifPresent(pedido -> {
            pedido.setStatus(novoStatus);
            pedidoCompraRepository.save(pedido);
        });
    }

    // Métodos para buscar pedidos por fornecedor, status, etc., podem ser adicionados aqui
}