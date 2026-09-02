package com.zlogcompras.controller;

import java.util.List;
import java.util.Optional; // Usaremos o repositório diretamente aqui por simplicidade neste momento

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zlogcompras.model.Fornecedor;
import com.zlogcompras.repository.FornecedorRepository;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @GetMapping
    public ResponseEntity<List<Fornecedor>> listarTodosFornecedores() {
        List<Fornecedor> fornecedores = fornecedorRepository.findAll();
        return new ResponseEntity<>(fornecedores, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fornecedor> buscarFornecedorPorId(@PathVariable Long id) {
        Optional<Fornecedor> fornecedor = fornecedorRepository.findById(id);
        return fornecedor.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Fornecedor> criarFornecedor(@RequestBody Fornecedor fornecedor) {
        Fornecedor novoFornecedor = fornecedorRepository.save(fornecedor);
        return new ResponseEntity<>(novoFornecedor, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Fornecedor> atualizarFornecedor(@PathVariable Long id, @RequestBody Fornecedor fornecedorAtualizado) {
        Optional<Fornecedor> fornecedorExistente = fornecedorRepository.findById(id);
        if (fornecedorExistente.isPresent()) {
            fornecedorAtualizado.setId(id); // Garante que o ID seja o mesmo
            Fornecedor fornecedorSalvo = fornecedorRepository.save(fornecedorAtualizado);
            return new ResponseEntity<>(fornecedorSalvo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarFornecedor(@PathVariable Long id) {
        if (fornecedorRepository.existsById(id)) {
            fornecedorRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}