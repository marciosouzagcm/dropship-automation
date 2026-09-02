package telas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import conexao.Conexao;
import entity.Motorista;

/**
 * Classe que manipula as operações de acesso a dados relacionadas aos
 * motoristas.
 */
public class MotoristaDAO {

    /**
     * Método para cadastrar um novo motorista no banco de dados.
     * 
     * @param motorista Objeto contendo os dados do motorista.
     */
    public void cadastrarMotorista(Motorista motorista) {
        // Consulta SQL para inserir os dados do motorista na tabela motorista
        String sql = "INSERT INTO motorista(nome, placaVeiculo) VALUES (?, ?)";

        try (Connection conn = Conexao.getConexao(); PreparedStatement ps = conn.prepareStatement(sql)) {
            // Definindo parâmetros para o PreparedStatement com os dados do motorista
            ps.setString(1, motorista.getNome());
            ps.setString(2, motorista.getPlacaVeiculo());

            // Executando a instrução SQL
            ps.executeUpdate();

        } catch (SQLException e) {
            // Lidando com exceções SQL
            e.printStackTrace();
        }
    }
}
