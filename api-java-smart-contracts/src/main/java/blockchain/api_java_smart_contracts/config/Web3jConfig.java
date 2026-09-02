package blockchain.api_java_smart_contracts.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

@Configuration // Indica que esta classe contém definições de beans gerenciados pelo Spring.
public class Web3jConfig {

    @Value("${ethereum.rpc.url}") // Injeta o valor da propriedade 'ethereum.rpc.url' definida no arquivo de
                                  // configuração.
    private String rpcUrl; // Armazena a URL do nó Ethereum ao qual esta configuração se conectará.

    @Value("${ethereum.private-key}") // Injeta o valor da propriedade 'ethereum.private-key' do arquivo de
                                      // configuração. Esta é a chave privada de uma conta Ethereum principal (não
                                      // necessariamente a do contrato).
    private String privateKey; // Armazena a chave privada da conta Ethereum principal que pode ser usada para
                               // interações gerais com a rede.

    @Bean("web3jPrincipal") // Define um bean chamado 'web3jPrincipal' para a instância principal de Web3j.
    public Web3j web3jPrincipal() { // Este método cria e configura a instância principal de Web3j.
        return Web3j.build(new HttpService(rpcUrl)); // Cria uma nova instância de Web3j, conectando-se ao nó Ethereum
                                                     // na URL fornecida por 'rpcUrl' através de HTTP RPC.
    }

    @Bean("credentialsPrincipal") // Define um bean chamado 'credentialsPrincipal' para as credenciais da conta
                                  // principal.
    public Credentials credentialsPrincipal() { // Este método cria e configura as credenciais da conta Ethereum
                                                // principal.
        return Credentials.create(privateKey); // Cria um objeto 'Credentials' a partir da chave privada principal
                                               // fornecida.
    }

    @Bean // Define um bean anônimo para o provedor de gás padrão.
    public DefaultGasProvider gasProvider() { // Este método cria e configura o provedor de gás padrão para transações
                                              // gerais.
        return new DefaultGasProvider(); // Retorna uma instância de 'DefaultGasProvider', que fornece estimativas
                                         // básicas de gás para transações.
                                         // A menção de "ou sua implementação customizada" sugere que em cenários mais
                                         // complexos, uma lógica de estimativa de gás personalizada poderia ser usada.
    }
}