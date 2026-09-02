package blockchain.api_java_smart_contracts.controller;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.abi.datatypes.generated.Uint256;

import blockchain.api_java_smart_contracts.service.BlockchainService;
import blockchain.api_java_smart_contracts.service.BlockchainServiceException;
import blockchain.api_java_smart_contracts.service.Voting.Candidato;

@RestController // Indica que esta classe é um Controller REST, responsável por lidar com as
                // requisições HTTP
public class CandidatoController {

    @Autowired // Injeção de dependência do serviço BlockchainService, permitindo que o
               // controller utilize seus métodos
    private BlockchainService blockchainService;

    @PostMapping("/candidato") // Mapeia requisições HTTP POST para o path "/candidato"
    public ResponseEntity<String> adicionarCandidato(@RequestParam(required = true) String nome) {
        if (nome == null || nome.isEmpty()) {
            return ResponseEntity.badRequest().body("O parâmetro 'nome' é obrigatório.");
        }
        try {
            blockchainService.adicionarCandidato(nome).get(); // Chama o serviço para adicionar o candidato no contrato
            return ResponseEntity.ok("Candidato adicionado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao adicionar candidato: " + e.getMessage());
        }
    }

    @GetMapping("/candidato/{id}") // Mapeia requisições HTTP GET para o path "/candidato/{id}", onde {id} é uma
                                   // variável de path
    public ResponseEntity<?> obterCandidato(@PathVariable BigInteger id) {
        try {
            CompletableFuture<Candidato> futuroCandidato = blockchainService.obterCandidato(id); // Chama o serviço para
                                                                                                 // obter o candidato
                                                                                                 // por ID
            Candidato candidato = futuroCandidato.get(); // Espera a conclusão da operação assíncrona e obtém o objeto
                                                         // Candidato
            if (candidato != null) {
                return ResponseEntity.ok(candidato); // Retorna uma resposta HTTP 200 OK com o objeto Candidato
                                                     // encontrado
            } else {
                return ResponseEntity.notFound().build(); // Retorna uma resposta HTTP 404 Not Found se o candidato não
                                                          // for encontrado
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Interrompe a thread atual se a operação for interrompida
            return ResponseEntity.status(500).body("Erro ao obter candidato: Operação interrompida.");
        } catch (ExecutionException e) {
            return ResponseEntity.badRequest().body("Erro ao obter candidato: " + e.getMessage());
        } catch (BlockchainServiceException e) {
            return ResponseEntity.badRequest().body("Erro ao obter candidato: " + e.getMessage());
        }
    }

    @PostMapping("/voto") // Mapeia requisições HTTP POST para o path "/voto"
    public ResponseEntity<String> votar(@RequestParam BigInteger candidatoId) {
        try {
            blockchainService.votar(candidatoId).get(); // Chama o serviço para registrar o voto no contrato
            return ResponseEntity.ok("Voto registrado com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao votar: " + e.getMessage());
        }
    }

    @GetMapping("/candidatos/total") // Mapeia requisições HTTP GET para o path "/candidatos/total"
    public ResponseEntity<String> obterTotalDeCandidatos() {
        try {
            CompletableFuture<Uint256> totalFuture = blockchainService.obterTotalDeCandidatos(); // Chama o serviço para
                                                                                                 // obter o total de
                                                                                                 // candidatos
            Uint256 total = totalFuture.get(); // Espera a conclusão da operação assíncrona e obtém o valor Uint256
            return ResponseEntity.ok(total.getValue().toString()); // Retorna uma resposta HTTP 200 OK com o valor total
                                                                   // de candidatos como String
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao obter total de candidatos: " + e.getMessage());
        }
    }

    @PostMapping("/eleitor/autorizar") // Mapeia requisições HTTP POST para o path "/eleitor/autorizar"
    public ResponseEntity<String> autorizarEleitor(@RequestParam String endereco, @RequestParam boolean autorizado) {
        try {
            blockchainService.autorizarEleitor(endereco, autorizado).get(); // Chama o serviço para
                                                                            // autorizar/desautorizar o eleitor
            return ResponseEntity.ok("Eleitor " + (autorizado ? "autorizado" : "desautorizado") + " com sucesso.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao autorizar eleitor: " + e.getMessage());
        }
    }

    @GetMapping("/eleitor/{endereco}/votou") // Mapeia requisições HTTP GET para o path "/eleitor/{endereco}/votou"
    public ResponseEntity<Boolean> verificarSeVotou(@PathVariable String endereco) {
        try {
            CompletableFuture<Boolean> votouFuture = blockchainService.verificarSeVotou(endereco); // Chama o serviço
                                                                                                   // para verificar se
                                                                                                   // o eleitor já votou
            Boolean votou = votouFuture.get();
            return ResponseEntity.ok(votou);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }
}