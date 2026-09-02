package com.zlogcompras.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zlogcompras.model.Estoque;
import com.zlogcompras.service.EstoqueService;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    @Autowired
    private EstoqueService estoqueService;

    @GetMapping("/{codigoMaterial}")
    public ResponseEntity<Estoque> buscarEstoquePorCodigo(@PathVariable String codigoMaterial) {
        Optional<Estoque> estoque = estoqueService.buscarPorCodigo(codigoMaterial);
        return estoque.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // No futuro, você pode adicionar endpoints para:
    // - Listar todos os itens em estoque (@GetMapping)
    // - Adicionar novos itens ao estoque (@PostMapping)
    // - Atualizar informações de um item no estoque (@PutMapping)
    // - Ajustar o estoque (entrada/saída manual) (@PostMapping ou @PutMapping)
}