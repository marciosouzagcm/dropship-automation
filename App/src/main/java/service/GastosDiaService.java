package service;

import entity.GastosDia;
import repository.GastosDiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GastosDiaService {

    @Autowired
    private GastosDiaRepository gastosDiaRepository;

    // Método para obter todos os registros de GastosDia
    public List<GastosDia> findAll() {
        return ((GastosDiaService) gastosDiaRepository).findAll();
    }

    // Método para obter um registro de GastosDia por ID
    public Optional<GastosDia> findById(Long id) {
        return ((GastosDiaService) gastosDiaRepository).findById(id);
    }

    // Método para salvar um novo registro de GastosDia
    public GastosDia save(GastosDia gastosDia) {
        return ((GastosDiaService) gastosDiaRepository).save(gastosDia);
    }

    // Método para deletar um registro de GastosDia por ID
    public void deleteById(Long id) {
        ((GastosDiaService) gastosDiaRepository).deleteById(id);
    }
}