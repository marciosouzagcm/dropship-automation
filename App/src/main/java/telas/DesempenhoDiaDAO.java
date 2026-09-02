package telas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import conexao.Conexao;
import entity.DesempenhoDia;

/**
 * Classe que manipula as operações de acesso a dados relacionadas ao desempenho
 * do dia.
 */
public class DesempenhoDiaDAO {

    /**
     * Método para cadastrar um novo desempenho no banco de dados.
     * 
     * @param desempenho Objeto contendo os dados do desempenho do dia.
     */
    public void cadastrarDesempenhoDia(DesempenhoDia desempenho) {
        // Consulta SQL para inserir os dados do desempenho na tabela desempenho_dia
        String sql = "INSERT INTO desempenhodia(data, corridasDiaUber, corridasDia99, corridasDiaIndrive, onlineUber, online99, onlineIndrive, totalKmDia) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConexao(); PreparedStatement ps = conn.prepareStatement(sql)) {
            // Definindo parâmetros para o PreparedStatement com os dados do desempenho
            ps.setDate(1, java.sql.Date.valueOf(desempenho.getData()));
            ps.setInt(2, desempenho.getCorridasDiaUber());
            ps.setInt(3, desempenho.getCorridasDia99());
            ps.setInt(4, desempenho.getCorridasDiaIndrive());
            ps.setString(5, desempenho.getOnlineUber());
            ps.setString(6, desempenho.getOnline99());
            ps.setString(7, desempenho.getOnlineIndrive());
            ps.setFloat(8, desempenho.getTotalKmDia());

            // Executando a instrução SQL
            ps.executeUpdate();

        } catch (SQLException e) {
            // Lidando com exceções SQL
            e.printStackTrace();
        }
    }
}
