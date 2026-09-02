package blockchain.api_java_smart_contracts.controller;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import blockchain.api_java_smart_contracts.service.BlockchainService;
import blockchain.api_java_smart_contracts.service.BlockchainServiceException;
import blockchain.api_java_smart_contracts.service.Voting.Candidato;

@RestController
@RequestMapping("/api/voting") // Mapeia as requisições para este controlador com o prefixo "/api/voting"
public class VotingController {

    @Autowired // Injeção de dependência do serviço BlockchainService
    private BlockchainService blockchainService;

    @PostMapping("/candidato") // Mapeia requisições HTTP POST para "/api/voting/candidato"
    public ResponseEntity<String> adicionarCandidato(@RequestParam String nome) { // Recebe o nome do candidato como parâmetro da requisição
        try {
            blockchainService.adicionarCandidato(nome).get(); // Chama o serviço para adicionar o candidato e espera a conclusão
            return ResponseEntity.ok("Candidato adicionado com sucesso."); // Retorna uma resposta HTTP 200 (OK) com uma mensagem de sucesso
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao adicionar candidato: " + e.getMessage()); // Retorna uma resposta HTTP 400 (Bad Request) com a mensagem de erro
        }
    }

    @PostMapping("/voto") // Mapeia requisições HTTP POST para "/api/voting/voto"
    public ResponseEntity<String> votar(@RequestParam BigInteger candidatoId) { // Recebe o ID do candidato a ser votado como parâmetro
        try {
            blockchainService.votar(candidatoId).get(); // Chama o serviço para registrar o voto e espera a conclusão
            return ResponseEntity.ok("Voto registrado com sucesso."); // Retorna uma resposta HTTP 200 (OK) com uma mensagem de sucesso
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao votar: " + e.getMessage()); // Retorna uma resposta HTTP 400 (Bad Request) com a mensagem de erro
        }
    }

    @GetMapping("/candidato/{id}") // Mapeia requisições HTTP GET para "/api/voting/candidato/{id}", onde {id} é um parâmetro de caminho
    public ResponseEntity<?> obterCandidato(@PathVariable BigInteger id) { // Recebe o ID do candidato a ser obtido através do caminho da requisição
        try {
            CompletableFuture<Candidato> futuroCandidato = blockchainService.obterCandidato(id); // Chama o serviço para obter o candidato de forma assíncrona
            Candidato candidato = futuroCandidato.get(); // Espera o resultado da operação assíncrona
            if (candidato != null) {
                return ResponseEntity.ok(candidato); // Retorna uma resposta HTTP 200 (OK) com os dados do candidato
            } else {
                return ResponseEntity.notFound().build(); // Retorna uma resposta HTTP 404 (Not Found) se o candidato não for encontrado
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Interrompe a thread atual se a espera for interrompida
            return ResponseEntity.status(500).body("Erro ao obter candidato: Operação interrompida."); // Retorna um erro HTTP 500 (Internal Server Error)
        } catch (ExecutionException e) {
            return ResponseEntity.badRequest().body("Erro ao obter candidato: " + e.getMessage()); // Retorna um erro HTTP 400 (Bad Request) com a mensagem de erro da execução
        } catch (BlockchainServiceException e) {
            return ResponseEntity.badRequest().body("Erro ao obter candidato: " + e.getMessage()); // Retorna um erro HTTP 400 (Bad Request) com a mensagem de erro específica do serviço
        }
    }

    @PostMapping("/eleitor/autorizar") // Mapeia requisições HTTP POST para "/api/voting/eleitor/autorizar"
    public ResponseEntity<String> autorizarEleitor(@RequestParam String endereco) { // Recebe o endereço do eleitor a ser autorizado
        try {
            blockchainService.autorizarEleitor(endereco, true).get(); // Chama o serviço para autorizar o eleitor e espera a conclusão
            return ResponseEntity.ok("Eleitor autorizado com sucesso."); // Retorna uma resposta HTTP 200 (OK) com uma mensagem de sucesso
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao autorizar eleitor: " + e.getMessage()); // Retorna uma resposta HTTP 400 (Bad Request) com a mensagem de erro
        }
    }
}