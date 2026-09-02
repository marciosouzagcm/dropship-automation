package repository;

import entity.Motorista;
import org.springframework.stereotype.Repository;

@Repository
public interface MotoristaRepository extends JpaRepository<Motorista, Long> {
    // Métodos de consulta personalizados podem ser adicionados aqui, se necessário
}