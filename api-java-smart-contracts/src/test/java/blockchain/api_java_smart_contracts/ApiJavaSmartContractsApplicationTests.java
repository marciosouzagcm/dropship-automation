package blockchain.api_java_smart_contracts;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.votacao.api.ApiJavaSmartContractsApplication; // Importe sua classe principal

@SpringBootTest(classes = ApiJavaSmartContractsApplication.class) // Use a classe principal correta
class ApiJavaSmartContractsApplicationTests {

    @Test
    void contextLoads() {
        // Este método de teste é intencionalmente deixado vazio para verificar se o contexto da aplicação Spring é carregado com sucesso.
    }

    private static class ApiJavaSmartContractsApplication {

        public ApiJavaSmartContractsApplication() {
        }
    }
}