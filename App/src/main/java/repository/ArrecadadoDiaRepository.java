package repository;

import entity.ArrecadadoDia;
import org.springframework.stereotype.Repository;

@Repository
public interface ArrecadadoDiaRepository extends JpaRepository<ArrecadadoDia, Long> {
    // Métodos de consulta personalizados podem ser adicionados aqui, se necessário
}