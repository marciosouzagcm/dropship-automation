package com.zlogcompras.service;

import java.util.List;

import com.zlogcompras.model.ItemSolicitacaoCompra;
import com.zlogcompras.repository.ItemSolicitacaoCompraRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ItemSolicitacaoCompraService {

    @Autowired
    private ItemSolicitacaoCompraRepository itemSolicitacaoCompraRepository;

    public Optional<ItemSolicitacaoCompra> buscarItemPorId(Long id) {
        return itemSolicitacaoCompraRepository.findById(id);
    }

    public void salvarItem(ItemSolicitacaoCompra item) {
        itemSolicitacaoCompraRepository.save(item);
    }

    public List<ItemSolicitacaoCompra> buscarItensPorSolicitacaoId(Long solicitacaoId) {
        return itemSolicitacaoCompraRepository.findBySolicitacaoCompraId(solicitacaoId);
    }

    public void atualizarStatusItem(Long id, String novoStatus) {
        Optional<ItemSolicitacaoCompra> itemOptional = itemSolicitacaoCompraRepository.findById(id);
        itemOptional.ifPresent(item -> {
            item.setStatus(novoStatus);
            itemSolicitacaoCompraRepository.save(item);
        });
    }

    // Outros métodos conforme a necessidade
}