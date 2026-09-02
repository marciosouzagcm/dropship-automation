package com.zlogcompras.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zlogcompras.model.Fornecedor;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    // Métodos personalizados de consulta podem ser adicionados aqui, se necessário
}