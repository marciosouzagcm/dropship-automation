package repository;

import entity.GastosDia;
import org.springframework.stereotype.Repository;

@Repository
public interface GastosDiaRepository extends JpaRepository<GastosDia, Long> {
    // Métodos de consulta personalizados podem ser adicionados aqui, se necessário
}