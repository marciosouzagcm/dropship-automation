package controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import entity.ArrecadadoDia;
import service.ArrecadadoDiaService;

@RestController
@RequestMapping("/api/arrecadado-dia")
public class ArrecadadoDiaController {

    @Autowired
    private ArrecadadoDiaService arrecadadoDiaService;

    // Endpoint para obter todos os registros de ArrecadadoDia
    @GetMapping
    public List<ArrecadadoDia> getAllArrecadadoDia() {
        return arrecadadoDiaService.findAll();
    }

    // Endpoint para obter um registro de ArrecadadoDia por ID
    @GetMapping("/{id}")
    public ResponseEntity<ArrecadadoDia> getArrecadadoDiaById(@PathVariable Long id) {
        Optional<ArrecadadoDia> arrecadadoDia = arrecadadoDiaService.findById(id);
        // Retorna o registro encontrado ou um status 404 (Not Found) se não encontrado
        return arrecadadoDia.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint para criar um novo registro de ArrecadadoDia
    @PostMapping
    public ArrecadadoDia createArrecadadoDia(@RequestBody ArrecadadoDia arrecadadoDia) {
        return arrecadadoDiaService.save(arrecadadoDia);
    }

    // Endpoint para atualizar um registro de ArrecadadoDia existente
    @PutMapping("/{id}")
    public ResponseEntity<ArrecadadoDia> updateArrecadadoDia(@PathVariable Long id,
            @RequestBody ArrecadadoDia arrecadadoDiaDetails) {
        Optional<ArrecadadoDia> arrecadadoDia = arrecadadoDiaService.findById(id);
        if (arrecadadoDia.isPresent()) {
            ArrecadadoDia updatedArrecadadoDia = arrecadadoDia.get();
            // Atualiza os campos do registro encontrado com os novos valores
            updatedArrecadadoDia.setData(arrecadadoDiaDetails.getData());
            updatedArrecadadoDia.setArrecadadoUber(arrecadadoDiaDetails.getArrecadadoUber());
            updatedArrecadadoDia.setArrecadado99(arrecadadoDiaDetails.getArrecadado99());
            updatedArrecadadoDia.setArrecadadoIndrive(arrecadadoDiaDetails.getArrecadadoIndrive());
            updatedArrecadadoDia.setArrecadadoOutros(arrecadadoDiaDetails.getArrecadadoOutros());
            // Salva o registro atualizado e retorna o mesmo
            return ResponseEntity.ok(arrecadadoDiaService.save(updatedArrecadadoDia));
        } else {
            // Retorna um status 404 (Not Found) se o registro não for encontrado
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint para deletar um registro de ArrecadadoDia por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArrecadadoDia(@PathVariable Long id) {
        arrecadadoDiaService.deleteById(id);
        // Retorna um status 204 (No Content) após a exclusão
        return ResponseEntity.noContent().build();
    }
}