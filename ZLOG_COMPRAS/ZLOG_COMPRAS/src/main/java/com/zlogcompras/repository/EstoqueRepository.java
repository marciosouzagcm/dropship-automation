package com.zlogcompras.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.zlogcompras.model.Estoque;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    Optional<Estoque> findByCodigoMaterial(String codigoMaterial);
    // Outros métodos personalizados, se necessário
}