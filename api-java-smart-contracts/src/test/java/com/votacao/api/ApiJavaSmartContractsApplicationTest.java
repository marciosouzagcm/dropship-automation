package com.votacao.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ApiJavaSmartContractsApplication.class) // Use a classe principal da sua aplicação
public class ApiJavaSmartContractsApplicationTest {

    @Test
    void contextLoads() {
        // Este método de teste é intencionalmente deixado vazio para verificar
        // se o contexto da aplicação Spring é carregado com sucesso.
    }

    private static class ApiJavaSmartContractsApplication {

        public ApiJavaSmartContractsApplication() {
        }
    
    }
}