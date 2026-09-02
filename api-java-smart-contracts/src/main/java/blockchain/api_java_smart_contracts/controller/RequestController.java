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
import blockchain.api_java_smart_contracts.service.Voting;

@RestController // Indica que esta classe é um controlador REST, responsável por lidar com as requisições HTTP.
@RequestMapping("/api") // Define o prefixo para todos os endpoints definidos nesta classe. As URLs começarão com "/api".
public class RequestController {

    private final BlockchainService blockchainService; // Declara uma instância do serviço BlockchainService.

    @Autowired // Utiliza a injeção de dependência do Spring para fornecer uma instância de BlockchainService.
    public RequestController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @PostMapping("/candidatos") // Mapeia requisições HTTP POST para o caminho "/api/candidatos".
    public ResponseEntity<String> adicionarCandidato(@RequestParam String nome) {
        // Endpoint para adicionar um novo candidato através do nome.
        // @RequestParam String nome: Indica que o parâmetro 'nome' será obtido dos parâmetros da requisição.
        try {
            blockchainService.adicionarCandidato(nome).get(); // Chama o serviço para adicionar o candidato e aguarda a conclusão da operação assíncrona.
            return ResponseEntity.ok("Candidato adicionado com sucesso!"); // Retorna uma resposta HTTP com status 200 (OK) e uma mensagem de sucesso.
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao adicionar candidato: " + e.getMessage()); // Em caso de erro, retorna uma resposta HTTP com status 400 (Bad Request) e a mensagem de erro.
        }
    }

    @GetMapping("/candidatos/{id}") // Mapeia requisições HTTP GET para o caminho "/api/candidatos/{id}", onde {id} é uma variável de caminho.
    public ResponseEntity<?> obterCandidato(@PathVariable BigInteger id) {
        // Endpoint para obter informações de um candidato específico pelo seu ID.
        // @PathVariable BigInteger id: Indica que o parâmetro 'id' será obtido da variável de caminho na URL.
        try {
            CompletableFuture<Voting.Candidato> futuroCandidato = blockchainService.obterCandidato(id); // Chama o serviço para obter o candidato de forma assíncrona.
            Voting.Candidato candidato = futuroCandidato.get(); // Aguarda a conclusão da operação assíncrona para obter o objeto Candidato.
            return candidato != null ? ResponseEntity.ok(candidato) : ResponseEntity.notFound().build(); // Se o candidato for encontrado, retorna uma resposta HTTP com status 200 (OK) e o objeto candidato. Caso contrário, retorna status 404 (Not Found).
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaura o status de interrupção da thread atual.
            return ResponseEntity.status(500).body("Erro ao obter candidato: Operação interrompida."); // Retorna uma resposta HTTP com status 500 (Internal Server Error) se a operação for interrompida.
        } catch (ExecutionException | BlockchainServiceException e) {
            return ResponseEntity.badRequest().body("Erro ao obter candidato: " + e.getMessage()); // Retorna uma resposta HTTP com status 400 (Bad Request) em caso de erro durante a execução ou erro específico do serviço de blockchain.
        }
    }

    @PostMapping("/votar") // Mapeia requisições HTTP POST para o caminho "/api/votar".
    public ResponseEntity<String> votar(@RequestParam BigInteger idCandidato) {
        // Endpoint para registrar um voto para um candidato específico pelo seu ID.
        // @RequestParam BigInteger idCandidato: Indica que o parâmetro 'idCandidato' será obtido dos parâmetros da requisição.
        try {
            blockchainService.votar(idCandidato).get(); // Chama o serviço para registrar o voto e aguarda a conclusão.
            return ResponseEntity.ok("Voto registrado com sucesso!"); // Retorna uma resposta HTTP com status 200 (OK) em caso de sucesso.
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao registrar voto: " + e.getMessage()); // Retorna uma resposta HTTP com status 400 (Bad Request) em caso de erro.
        }
    }

    @PostMapping("/eleitor/autorizar") // Mapeia requisições HTTP POST para o caminho "/api/eleitor/autorizar".
    public ResponseEntity<String> autorizarEleitor(@RequestParam String endereco, @RequestParam boolean autorizado) {
        // Endpoint para autorizar ou desautorizar um eleitor a votar, usando seu endereço Ethereum.
        // @RequestParam String endereco: Endereço do eleitor a ser autorizado/desautorizado.
        // @RequestParam boolean autorizado: Valor booleano indicando se o eleitor deve ser autorizado (true) ou desautorizado (false).
        try {
            blockchainService.autorizarEleitor(endereco, autorizado).get(); // Chama o serviço para autorizar/desautorizar o eleitor e aguarda a conclusão.
            return ResponseEntity.ok("Eleitor " + (autorizado ? "autorizado" : "desautorizado") + " com sucesso!"); // Retorna uma resposta HTTP com status 200 (OK) e uma mensagem indicando o status do eleitor.
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao autorizar eleitor: " + e.getMessage()); // Retorna uma resposta HTTP com status 400 (Bad Request) em caso de erro.
        }
    }
}