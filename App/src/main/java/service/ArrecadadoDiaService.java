package service;

import entity.ArrecadadoDia;
import repository.ArrecadadoDiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ArrecadadoDiaService {

    @Autowired
    private ArrecadadoDiaRepository arrecadadoDiaRepository;

    public List<ArrecadadoDia> findAll() {
        return ((ArrecadadoDiaService) arrecadadoDiaRepository).findAll();
    }

    public Optional<ArrecadadoDia> findById(Long id) {
        return ((ArrecadadoDiaService) arrecadadoDiaRepository).findById(id);
    }

    public ArrecadadoDia save(ArrecadadoDia arrecadadoDia) {
        return ((ArrecadadoDiaService) arrecadadoDiaRepository).save(arrecadadoDia);
    }

    public void deleteById(Long id) {
        ((ArrecadadoDiaService) arrecadadoDiaRepository).deleteById(id);
    }
}