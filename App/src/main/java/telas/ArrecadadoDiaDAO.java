package telas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import conexao.Conexao;
import entity.ArrecadadoDia;

/**
 * Classe que manipula as operações de acesso a dados relacionadas à arrecadação
 * do dia.
 */
public class ArrecadadoDiaDAO {

    /**
     * Método para cadastrar uma nova arrecadação no banco de dados.
     * 
     * @param arrecadado Objeto contendo os dados da arrecadação do dia.
     */
    public void cadastrarArrecadadoDia(ArrecadadoDia arrecadado) {
        // Consulta SQL para inserir os dados da arrecadação na tabela arrecadado_dia
        String sql = "INSERT INTO arrecadadodia(data, arrecadadoUber, arrecadado99, arrecadadoIndrive, arrecadadoOutros) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConexao(); PreparedStatement ps = conn.prepareStatement(sql)) {
            // Definindo parâmetros para o PreparedStatement com os dados da arrecadação
            ps.setDate(1, java.sql.Date.valueOf(arrecadado.getData()));
            ps.setFloat(2, arrecadado.getArrecadadoUber());
            ps.setFloat(3, arrecadado.getArrecadado99());
            ps.setFloat(4, arrecadado.getArrecadadoIndrive());
            ps.setFloat(5, arrecadado.getArrecadadoOutros());

            // Executando a instrução SQL
            ps.executeUpdate();

        } catch (SQLException e) {
            // Lidando com exceções SQL
            e.printStackTrace();
        }
    }
}
