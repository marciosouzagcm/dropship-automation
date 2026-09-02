package com.zlogcompras.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zlogcompras.model.Estoque;
import com.zlogcompras.model.ItemSolicitacaoCompra;
import com.zlogcompras.model.MovimentacaoEstoque;
import com.zlogcompras.repository.EstoqueRepository;
import com.zlogcompras.repository.MovimentacaoEstoqueRepository;

@Service
public class EstoqueService {

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    private ItemSolicitacaoCompraService itemSolicitacaoCompraService;

    public Optional<Estoque> buscarPorCodigo(String codigoMaterial) {
        return estoqueRepository.findByCodigoMaterial(codigoMaterial);
    }

    @Transactional
    public void verificarEstoque(ItemSolicitacaoCompra itemSolicitacao) {
        Optional<Estoque> estoqueExistente = estoqueRepository.findByCodigoMaterial(itemSolicitacao.getMaterialServico());
        if (estoqueExistente.isPresent() && estoqueExistente.get().getQuantidade() >= itemSolicitacao.getQuantidade()) {
            // Tem no estoque: atender a solicitação e atualizar o estoque
            atenderSolicitacao(estoqueExistente.get(), itemSolicitacao.getQuantidade());
            itemSolicitacaoCompraService.atualizarStatusItem(itemSolicitacao.getId(), "Em Estoque");
            registrarMovimentacaoEstoque(itemSolicitacao.getMaterialServico(), -itemSolicitacao.getQuantidade(), "SAIDA", "SOLICITACAO_" + itemSolicitacao.getSolicitacaoCompra().getId());
        } else {
            // Não tem no estoque ou quantidade insuficiente: marcar item para compra
            System.out.println("Material " + itemSolicitacao.getMaterialServico() + " não disponível em estoque ou quantidade insuficiente. Marcando para compra.");
            itemSolicitacaoCompraService.atualizarStatusItem(itemSolicitacao.getId(), "Aguardando Compra");
        }
    }

    @Transactional
    public void atenderSolicitacao(Estoque estoque, int quantidadeAtendida) {
        estoque.setQuantidade(estoque.getQuantidade() - quantidadeAtendida);
        estoqueRepository.save(estoque);
    }

    @Transactional
    public void receberMaterial(String codigoMaterial, int quantidadeRecebida, String referencia) {
        Optional<Estoque> estoqueExistente = estoqueRepository.findByCodigoMaterial(codigoMaterial);
        if (estoqueExistente.isPresent()) {
            estoqueExistente.get().setQuantidade(estoqueExistente.get().getQuantidade() + quantidadeRecebida);
            estoqueRepository.save(estoqueExistente.get());
        } else {
            Estoque novoEstoque = new Estoque();
            novoEstoque.setCodigoMaterial(codigoMaterial);
            novoEstoque.setQuantidade(quantidadeRecebida);
            estoqueRepository.save(novoEstoque);
        }
        registrarMovimentacaoEstoque(codigoMaterial, quantidadeRecebida, "ENTRADA", referencia);
    }

    private void registrarMovimentacaoEstoque(String codigoMaterial, int quantidade, String tipo, String referencia) {
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque();
        movimentacao.setCodigoMaterial(codigoMaterial);
        movimentacao.setQuantidade(quantidade);
        movimentacao.setTipoMovimentacao(tipo);
        movimentacao.setDataMovimentacao(LocalDateTime.now());
        movimentacao.setReferencia(referencia);
        movimentacaoEstoqueRepository.save(movimentacao);
    }

    // Métodos para atualizar estoque (genérico), buscar histórico de movimentações, etc.
}