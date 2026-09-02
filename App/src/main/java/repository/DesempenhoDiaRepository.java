package repository;

import entity.DesempenhoDia;
import org.springframework.stereotype.Repository;

@Repository
public interface DesempenhoDiaRepository extends JpaRepository<DesempenhoDia, Long> {
    // Métodos de consulta personalizados podem ser adicionados aqui, se necessário
}