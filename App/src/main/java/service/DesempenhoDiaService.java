package service;

import entity.DesempenhoDia;
import repository.DesempenhoDiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DesempenhoDiaService {

    @Autowired
    private DesempenhoDiaRepository desempenhoDiaRepository;

    // Método para obter todos os registros de DesempenhoDia
    public List<DesempenhoDia> findAll() {
        return ((DesempenhoDiaService) desempenhoDiaRepository).findAll();
    }

    // Método para obter um registro de DesempenhoDia por ID
    public Optional<DesempenhoDia> findById(Long id) {
        return ((DesempenhoDiaService) desempenhoDiaRepository).findById(id);
    }

    // Método para salvar um novo registro de DesempenhoDia
    public DesempenhoDia save(DesempenhoDia desempenhoDia) {
        return ((DesempenhoDiaService) desempenhoDiaRepository).save(desempenhoDia);
    }

    // Método para deletar um registro de DesempenhoDia por ID
    public void deleteById(Long id) {
        ((DesempenhoDiaService) desempenhoDiaRepository).deleteById(id);
    }
}