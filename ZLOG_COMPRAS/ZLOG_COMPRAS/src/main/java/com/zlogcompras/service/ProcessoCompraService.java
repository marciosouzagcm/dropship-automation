package com.zlogcompras.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zlogcompras.model.ItemPedidoCompra;
import com.zlogcompras.model.ItemSolicitacaoCompra;
import com.zlogcompras.model.Orcamento;
import com.zlogcompras.model.PedidoCompra;
import com.zlogcompras.model.SolicitacaoCompra;
import com.zlogcompras.repository.OrcamentoRepository;

@Service
public class ProcessoCompraService {

    @Autowired
    private SolicitacaoCompraService solicitacaoCompraService;

    @Autowired
    private ItemSolicitacaoCompraService itemSolicitacaoCompraService;

    @Autowired
    private PedidoCompraService pedidoCompraService;

    @Autowired
    private OrcamentoService orcamentoService;

    @Autowired
    private OrcamentoRepository orcamentoRepository;

    // Método para iniciar o processo de compra quando um item não está em estoque (chamado pelo EstoqueService)
    public void iniciarProcessoCompra(ItemSolicitacaoCompra itemSolicitacao) {
        SolicitacaoCompra solicitacao = itemSolicitacao.getSolicitacaoCompra();
        solicitacaoCompraService.atualizarStatusSolicitacao(solicitacao.getId(), "Compra Autorizada");
        solicitarOrcamentos(List.of(itemSolicitacao)); // Solicita orçamento apenas para o item atual
    }

    // Método para iniciar o processo de compra para uma solicitação inteira (chamado pelo ProcessoCompraController)
    public void iniciarProcessoCompraPorSolicitacaoId(Long solicitacaoId) {
        SolicitacaoCompra solicitacao = solicitacaoCompraService.buscarSolicitacaoPorId(solicitacaoId);
        if (solicitacao != null) {
            // Buscar todos os itens da solicitação que estão aguardando compra
            List<ItemSolicitacaoCompra> itensParaComprar = itemSolicitacaoCompraService.buscarItensPorSolicitacaoId(solicitacaoId)
                    .stream()
                    .filter(item -> "Aguardando Compra".equals(item.getStatus()))
                    .toList();

            if (!itensParaComprar.isEmpty()) {
                solicitacaoCompraService.atualizarStatusSolicitacao(solicitacao.getId(), "Compra em Andamento");
                solicitarOrcamentos(itensParaComprar); // Solicita orçamentos para cada item pendente
            } else {
                System.out.println("Não há itens aguardando compra na solicitação " + solicitacaoId);
            }
        } else {
            System.out.println("Solicitação de compra não encontrada com ID: " + solicitacaoId);
        }
    }

    // Método para solicitar orçamentos para uma lista de itens
    public void solicitarOrcamentos(List<ItemSolicitacaoCompra> itens) {
        // Para cada item, vamos simular a criação de alguns orçamentos
        SolicitacaoCompra solicitacao = itens.get(0).getSolicitacaoCompra(); // Assumindo que todos os itens são da mesma solicitação
        solicitacaoCompraService.atualizarStatusSolicitacao(solicitacao.getId(), "Aguardando Orçamento"); // Atualiza o status da solicitação
        simularCriacaoOrcamentosPorItem(itens);
    }

    // Simulação da criação de orçamentos por item
    private void simularCriacaoOrcamentosPorItem(List<ItemSolicitacaoCompra> itens) {
        for (ItemSolicitacaoCompra item : itens) {
            // Simula a obtenção de orçamentos de diferentes fornecedores para o item
            Orcamento orcamento1 = new Orcamento();
            orcamento1.setSolicitacaoCompra(item.getSolicitacaoCompra());
            orcamento1.setItemSolicitacaoCompra(item); // Associa o orçamento ao item
            orcamento1.setFornecedorId(1L); // ID do fornecedor
            orcamento1.setDataOrcamento(java.time.LocalDate.now());
            orcamento1.setValorTotal(Math.random() * 50 + 50); // Valor aleatório
            orcamento1.setNumeroOrcamento("ORC_" + item.getId() + "_1");
            orcamentoService.salvarOrcamento(orcamento1);

            Orcamento orcamento2 = new Orcamento();
            orcamento2.setSolicitacaoCompra(item.getSolicitacaoCompra());
            orcamento2.setItemSolicitacaoCompra(item); // Associa o orçamento ao item
            orcamento2.setFornecedorId(2L); // ID de outro fornecedor
            orcamento2.setDataOrcamento(java.time.LocalDate.now().plusDays(1));
            orcamento2.setValorTotal(Math.random() * 60 + 60); // Valor aleatório
            orcamento2.setNumeroOrcamento("ORC_" + item.getId() + "_2");
            orcamentoService.salvarOrcamento(orcamento2);
        }
        // Após simular os orçamentos para todos os itens, podemos atualizar o status da solicitação
        if (!itens.isEmpty()) {
            solicitacaoCompraService.atualizarStatusSolicitacao(itens.get(0).getSolicitacaoCompra().getId(), "Orçamentos Recebidos");
        }
    }

    // Método para aprovar um orçamento e gerar uma ordem de compra
    @Transactional
    public void aprovarOrcamentoGerarPedido(Long orcamentoId) {
        Optional<Orcamento> orcamento = orcamentoService.buscarOrcamentoPorId(orcamentoId);
        if (orcamento != null) {
            orcamento.ifPresent(o -> {
                o.setStatus("Selecionado");
                orcamentoRepository.save(o);
            });
            SolicitacaoCompra solicitacao = orcamento.get().getSolicitacaoCompra();
            solicitacaoCompraService.atualizarStatusSolicitacao(solicitacao.getId(), "Compra Aprovada");
            gerarOrdemDeCompra(solicitacao, orcamento.get().getFornecedorId(), orcamento.get().getItemSolicitacaoCompra()); // Passa o item do orçamento
        }
    }

    // Método para gerar a ordem de compra para um item específico
    private void gerarOrdemDeCompra(SolicitacaoCompra solicitacao, Long fornecedorId, ItemSolicitacaoCompra itemSolicitacao) {
        PedidoCompra pedidoCompra = new PedidoCompra();
        pedidoCompra.setFornecedorId(fornecedorId);
        // Cria um item de pedido de compra apenas para o item solicitado
        ItemPedidoCompra itemPedido = new ItemPedidoCompra();
        itemPedido.setMaterialServico(itemSolicitacao.getMaterialServico());
        itemPedido.setQuantidade(itemSolicitacao.getQuantidade());
        // Aqui você buscaria o preço unitário do orçamento selecionado
        // Por enquanto, vamos usar um placeholder
        Orcamento orcamentoSelecionado = orcamentoService.buscarOrcamentosPorItemSolicitacao(itemSolicitacao.getId())
                .stream().filter(orc -> "Selecionado".equals(orc.getStatus())).findFirst().orElse(null);
        itemPedido.setPrecoUnitario(orcamentoSelecionado != null ? orcamentoSelecionado.getValorTotal() / itemSolicitacao.getQuantidade() : 0.0);
        itemPedido.setPedidoCompra(pedidoCompra);
        pedidoCompra.setItens(List.of(itemPedido)); // Define a lista de itens do pedido com apenas este item
        PedidoCompra novoPedido = pedidoCompraService.criarPedidoCompra(pedidoCompra);
        solicitacaoCompraService.atualizarStatusSolicitacao(solicitacao.getId(), "Gerada OC para item");
        itemSolicitacaoCompraService.atualizarStatusItem(itemSolicitacao.getId(), "Comprado");
    }

    // Método para simular o envio da ordem de compra ao fornecedor
    public void enviarOrdemDeCompra(Long pedidoId) {
        pedidoCompraService.atualizarStatusPedidoCompra(pedidoId, "Enviado Fornecedor");
    }

    // Método para gerenciar a entrega e o recebimento
    public void gerenciarEntregaRecebimento(Long pedidoId) {
        pedidoCompraService.atualizarStatusPedidoCompra(pedidoId, "Recebido");
    }

    // Método para enviar para a obra
    public void enviarParaObra(Long pedidoId) {
        pedidoCompraService.atualizarStatusPedidoCompra(pedidoId, "Enviada Obra");
    }

    // Método para atualizar o estoque após o recebimento
    public void atualizarEstoqueRecebimento(Long pedidoId) {
        PedidoCompra pedido = pedidoCompraService.buscarPedidoCompraPorId(pedidoId).orElse(null);
        if (pedido != null && pedido.getStatus().equals("Recebido")) {
            pedido.getItens().forEach(item ->
                    estoqueService.receberMaterial(item.getMaterialServico(), item.getQuantidade(), "PEDIDO_" + pedido.getId())
            );
            pedidoCompraService.atualizarStatusPedidoCompra(pedidoId, "Estoque Atualizado");
        }
    }

    // Método para o processo de prestação de contas
    public void processoPrestacaoContas(Long pedidoId, String numeroNotaFiscal, java.time.LocalDate dataNotaFiscal, Double valorNota) {
        // Lógica para registrar a nota fiscal e associá-la ao pedido
        // Atualizar o status do pedido para "Contabilizado"
    }

    private static class estoqueService {

        private static void receberMaterial(String materialServico, Integer quantidade, String string) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public estoqueService() {
        }
    }
}