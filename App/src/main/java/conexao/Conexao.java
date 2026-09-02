package conexao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Classe responsável por estabelecer conexão com o banco de dados.
 */
public class Conexao {

    // Configurações de conexão
    private static final String URL = "jdbc:mysql://localhost:3306/motorista_app";
    private static final String USER = "admin";
    private static final String PASSWORD = "seguRa1$";

    /**
     * Método para obter uma conexão com o banco de dados.
     * 
     * @return Conexão estabelecida ou null em caso de erro.
     */
    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Método para fechar uma conexão.
     * 
     * @param conn Conexão a ser fechada.
     */
    public static void fecharConexao(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws SQLException {
        try {
            // Verificar se o driver MySQL está no classpath
            System.out.println("Classpath: " + System.getProperty("java.class.path"));
            // Carregar driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver não encontrado: " + e.getMessage());
            System.out.println("Certifique-se de que o driver MySQL está no classpath.");
            return;
        }

        Connection conector = Conexao.getConexao();

        if (conector != null) {
            System.out.println("Conexão estabelecida com sucesso!");
            Conexao.fecharConexao(conector);
        } else {
            System.out.println("Falha ao estabelecer conexão.");
        }
    }
}
