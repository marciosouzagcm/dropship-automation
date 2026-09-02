package telas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import conexao.Conexao;
import entity.GastosDia;

/**
 * Classe que manipula as operações de acesso a dados relacionadas aos gastos do
 * dia.
 */
public class GastosDiaDAO {

    /**
     * Método para cadastrar um novo gasto no banco de dados.
     * 
     * @param gastos Objeto contendo os dados dos gastos do dia.
     */
    public void cadastrarGastosDia(GastosDia gastos) {
        // Consulta SQL para inserir os dados dos gastos na tabela gastos_dia
        String sql = "INSERT INTO gastosdia(data, aluguelParcelamentoVeiculo, combustivel, pedagio, multa, alimentacao, manutencaoVeiculo, investimento, consorcio) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.getConexao(); PreparedStatement ps = conn.prepareStatement(sql)) {
            // Definindo parâmetros para o PreparedStatement com os dados dos gastos
            ps.setDate(1, java.sql.Date.valueOf(gastos.getData()));
            ps.setFloat(2, gastos.getAluguelParcelamentoVeiculo());
            ps.setFloat(3, gastos.getCombustivel());
            ps.setFloat(4, gastos.getPedagio());
            ps.setFloat(5, gastos.getMulta());
            ps.setFloat(6, gastos.getAlimentacao());
            ps.setFloat(7, gastos.getManutencaoVeiculo());
            ps.setFloat(8, gastos.getInvestimento());
            ps.setFloat(9, gastos.getConsorcio());

            // Executando a instrução SQL
            ps.executeUpdate();

        } catch (SQLException e) {
            // Lidando com exceções SQL
            e.printStackTrace();
        }
    }
}
