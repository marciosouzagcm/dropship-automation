package conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe responsável por estabelecer conexão com o banco de dados MySQL.
 * Esta classe segue um padrão simples para obter e fechar conexões,
 * utilizando informações de configuração hardcoded. Em aplicações mais
 * robustas, essas configurações seriam externalizadas (e.g., arquivos de
 * propriedades) e o gerenciamento de conexões seria feito por um pool
 * de conexões para melhor performance e escalabilidade.
 */
public class Conexao {

    // Configurações de conexão com o banco de dados MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/dbvoting";
    private static final String USER = "root";
    private static final String PASSWORD = "seguRa1$";

    /**
     * Método estático para obter uma conexão com o banco de dados MySQL.
     * Este método utiliza o DriverManager da API JDBC para estabelecer a conexão
     * com o banco de dados especificado na URL, com o usuário e senha fornecidos.
     *
     * @return Connection Uma conexão estabelecida com o banco de dados.
     * @throws SQLException Se ocorrer algum erro ao tentar estabelecer a conexão.
     */
    public static Connection getConexao() throws SQLException {
        // DriverManager.getConnection() tenta estabelecer uma conexão com o banco de dados
        // usando a URL, usuário e senha fornecidos. Se as credenciais estiverem corretas
        // e o servidor de banco de dados estiver acessível, uma conexão será retornada.
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Método estático para fechar uma conexão com o banco de dados.
     * É crucial fechar as conexões após o uso para liberar recursos do banco de dados
     * e evitar problemas de esgotamento de conexões.
     *
     * @param conn A conexão a ser fechada. Se a conexão for nula, este método não fará nada.
     */
    public static void fecharConexao(Connection conn) {
        try {
            // Verifica se a conexão não é nula antes de tentar fechá-la.
            if (conn != null) {
                // O método close() da interface Connection fecha a conexão com o banco de dados,
                // liberando todos os recursos associados a ela.
                conn.close();
            }
        } catch (SQLException e) {
            // Em caso de erro ao fechar a conexão, uma mensagem de erro é impressa no console.
            // Em aplicações de produção, um tratamento de erro mais robusto (como logging) seria necessário.
            System.out.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }

    /**
     * Método principal (main) para testar a funcionalidade de conexão.
     * Este método tenta estabelecer uma conexão com o banco de dados e,
     * em caso de sucesso, imprime uma mensagem e fecha a conexão.
     * Ele também inclui tratamento para `ClassNotFoundException` (caso o
     * driver JDBC do MySQL não seja encontrado) e `SQLException` (para erros
     * durante a conexão).
     *
     * @param args Argumentos da linha de comando (não utilizados neste exemplo).
     */
    public static void main(String[] args) {
        try {
            // 1. Verificar se o driver MySQL está no classpath
            // Imprime o classpath atual para ajudar na depuração de problemas de driver.
            System.out.println("Classpath: " + System.getProperty("java.class.path"));

            // 2. Carregar o driver JDBC do MySQL
            // Class.forName() carrega a classe do driver JDBC do MySQL na memória.
            // Isso registra o driver com o DriverManager, permitindo que ele reconheça
            // URLs do tipo "jdbc:mysql://...".
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Driver MySQL carregado com sucesso!");

            // 3. Testar a conexão com o banco de dados
            // Chama o método getConexao() para tentar estabelecer uma conexão.
            Connection conector = Conexao.getConexao();

            // 4. Verificar se a conexão foi estabelecida com sucesso
            if (conector != null) {
                System.out.println("Conexão estabelecida com sucesso!");
                // Fecha a conexão após o teste.
                Conexao.fecharConexao(conector);
            } else {
                System.out.println("Falha ao estabelecer conexão.");
            }
        } catch (ClassNotFoundException e) {
            // Esta exceção ocorre se a classe do driver MySQL não for encontrada no classpath.
            System.out.println("Driver não encontrado: " + e.getMessage());
            System.out.println("Certifique-se de que o driver MySQL (arquivo .jar) está incluído nas dependências do seu projeto.");
        } catch (SQLException e) {
            // Esta exceção ocorre se houver algum erro durante a tentativa de conexão
            // com o banco de dados (e.g., URL incorreta, usuário/senha inválidos,
            // servidor de banco de dados inacessível).
            System.out.println("Erro ao conectar ao banco de dados: " + e.getMessage());
        }
    }
}