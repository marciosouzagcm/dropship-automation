# Plataforma de Votação Descentralizada com Blockchain Ethereum (API Java Spring Boot)

[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/technologies/javase-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Web3j](https://img.shields.io/badge/Web3j-4.9.4-orange.svg)](https://www.web3j.io/)
[![Solidity](https://img.shields.io/badge/Solidity-0.8.x-lightgrey.svg)](https://docs.soliditylang.org/)
[![Ganache](https://img.shields.io/badge/Ganache-v7.x-purple.svg)](https://trufflesuite.com/ganache/)

## Visão Geral do Projeto

Este projeto consiste em uma API robusta desenvolvida em Java utilizando o framework Spring Boot e a biblioteca Web3j. Seu objetivo principal é fornecer uma interface de comunicação com um contrato inteligente (smart contract) implementado na blockchain Ethereum. O contrato inteligente, escrito em Solidity, define a lógica de um sistema de votação eletrônica descentralizado, buscando oferecer maior transparência, segurança e auditabilidade ao processo eleitoral.

A API Java atua como uma camada intermediária, permitindo que outras aplicações (como interfaces web ou mobile) interajam com as funcionalidades do contrato inteligente na blockchain Ethereum. As principais funcionalidades implementadas através desta API incluem:

* **Adição de Candidatos:** Permite registrar novos candidatos no sistema de votação através do contrato inteligente.
* **Votação:** Facilita o processo de voto por eleitores autorizados, registrando os votos de forma segura e imutável na blockchain.
* **Gerenciamento de Eleitores:** Oferece mecanismos para autorizar endereços Ethereum como eleitores válidos.
* **Consulta de Dados:** Permite a recuperação de informações do contrato inteligente, como detalhes de candidatos (ID, nome, número de votos) e o total de candidatos registrados.
* **Verificação de Voto:** Permite verificar se um determinado endereço Ethereum já exerceu seu direito ao voto.
* **Administração:** Expõe funcionalidades para consultar o endereço do administrador do contrato.

## Tecnologias Utilizadas

* **Java:** Linguagem de programação principal para o desenvolvimento da API.
* **Spring Boot:** Framework Java para a criação de aplicações standalone e de nível empresarial de forma rápida e fácil.
* **Web3j:** Uma biblioteca Java leve e reativa para integração com blockchains baseadas em Ethereum.
* **Solidity:** Linguagem de programação para a implementação do contrato inteligente Ethereum.
* **Ganache:** Um ambiente de blockchain Ethereum local utilizado para desenvolvimento e testes.
* **Maven:** Ferramenta de gerenciamento de dependências e construção do projeto Java.
* **Markdown:** Linguagem de marcação leve utilizada para formatar este arquivo README.

## Configuração do Ambiente de Desenvolvimento

Para executar e contribuir com este projeto, você precisará ter as seguintes ferramentas instaladas em sua máquina:

1.  **Java Development Kit (JDK):** Versão 21 ou superior.
    * [Download JDK](https://www.oracle.com/java/technologies/javase-downloads.html)

2.  **Maven:** Ferramenta de gerenciamento de projetos Java.
    * [Download Maven](https://maven.apache.org/download.cgi)

3.  **Ganache:** Cliente Ethereum pessoal para desenvolvimento.
    * [Download Ganache](https://trufflesuite.com/ganache/)

## Execução do Projeto

Siga estas etapas para executar a API Java e interagir com o contrato inteligente de votação:

1.  **Inicie o Ganache:**
    * Abra o aplicativo Ganache e crie um novo workspace Ethereum.
    * Verifique a porta em que o Ganache está rodando (por padrão, é `8545`).

2.  **Implante o Contrato Inteligente:**
    * Certifique-se de ter o contrato inteligente Solidity (`Voting.sol`) compilado e implantado na rede Ganache. Você pode utilizar ferramentas como Truffle ou Remix para essa etapa.
    * **Importante:** Atualize o endereço do contrato inteligente no arquivo de configuração da aplicação Java (`src/main/resources/application.properties` ou `application.yml`) com o endereço da sua implantação no Ganache:
        ```properties
        blockchain.voting.contract-address=SEU_CONTRATO_ADDRESS
        blockchain.web3j.url=http://localhost:8545
        ```

3.  **Execute a API Java Spring Boot:**
    * Abra um terminal na raiz do projeto.
    * Execute o seguinte comando Maven para iniciar a aplicação:
        ```bash
        mvn spring-boot:run
        ```
    * A API estará disponível (por padrão) na porta `8081`.

## Interagindo com a API

A API expõe diversos endpoints que podem ser consumidos por outras aplicações para interagir com o contrato inteligente. A documentação detalhada dos endpoints (métodos HTTP, URLs, parâmetros e respostas) pode ser encontrada em um arquivo separado (e.g., `API_ENDPOINTS.md`) ou através de ferramentas como Swagger (se implementado).

Exemplos básicos de interação (assumindo que a API está rodando em `http://localhost:8081`):

* **Adicionar um Candidato (exemplo de requisição POST):**
    ```
    POST /api/candidatos
    Content-Type: application/json

    {
      "nome": "Novo Candidato"
    }
    ```

* **Votar em um Candidato (exemplo de requisição POST):**
    ```
    POST /api/votos
    Content-Type: application/json

    {
      "idCandidato": 1
    }
    ```

* **Obter Detalhes de um Candidato (exemplo de requisição GET):**
    ```
    GET /api/candidatos/1
    ```

**Observação:** Estes são apenas exemplos ilustrativos. A implementação real dos endpoints pode variar.

## Tratamento do Limite de Gás (Exceeds Block Gas Limit)

Durante o desenvolvimento, enfrentamos desafios relacionados ao limite de gás das transações na blockchain Ethereum. O erro "exceeds block gas limit" indica que uma transação (como adicionar um candidato) tentou consumir mais gás do que o permitido pelo bloco.

As estratégias adotadas para mitigar este problema incluem:

* **Aumento do Limite de Gás no Ganache (para desenvolvimento local):** Aumentar o limite de gás nas configurações do Ganache permite transações mais complexas em um ambiente de teste. Geralmente, configurar um valor significativamente maior (por exemplo, `0x47E7E7` ou 5 milhões) pode resolver o problema para desenvolvimento.

* **Configuração Explícita do Limite de Gás na API Java (Web3j):** Ao enviar transações através da API, é possível configurar um limite de gás maior para garantir que a transação seja processada. A implementação específica para o método `adicionarCandidato` na classe `Voting` foi ajustada para incluir um `TransactionOptions` com um limite de gás elevado:

    ```java
    import org.web3j.tx.TransactionOptions;
    import java.math.BigInteger;
    // ... outras importações ...

    public RemoteCall<TransactionReceipt> adicionarCandidato(String nome) {
        Function function = new Function(
                "adicionarCandidato",
                Arrays.asList(new Utf8String(nome)),
                Collections.emptyList());
        return executeRemoteCallTransaction(function, TransactionOptions.create(credentials.getAddress(), null, null, BigInteger.valueOf(3000000)));
    }
    ```

    Este ajuste tenta especificar um limite de gás de 3 milhões de unidades de gás para a transação de adição de candidato. Se você ainda encontrar o erro, pode tentar aumentar ainda mais esse valor.

## Contribuição

Contribuições são bem-vindas! Se você tiver ideias para melhorar este projeto, encontrou algum bug ou deseja adicionar novas funcionalidades, siga estas etapas:

1.  Faça um fork do repositório.
2.  Crie uma branch para sua feature (`git checkout -b feature/sua-feature`).
3.  Faça seus commits (`git commit -am 'Adiciona nova feature'`).
4.  Faça o push para a branch (`git push origin feature/sua-feature`).
5.  Abra um Pull Request.

## Licença

Este projeto está licenciado sob a [MIT License](https://opensource.org/licenses/MIT). Consulte o arquivo `LICENSE` para obter mais detalhes.

## Próximos Passos

* Implementação de testes unitários e de integração para a API Java.
* Criação de uma interface de usuário (web ou mobile) para interagir com a API.
* Implementação de mecanismos de segurança e autenticação para a API.
* Consideração de otimizações no contrato inteligente para reduzir os custos de gás.
* Implantação em uma rede Ethereum de teste pública (e.g., Sepolia, Goerli).

## Contato

Se você tiver alguma dúvida ou sugestão, sinta-se à vontade para entrar em contato.

---

**Guarulhos, Estado de São Paulo, Brasil** - Maio de 2025