package telas;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import entity.GastosDia;

/**
 * Classe Tela_App_gastos_dia
 * Esta classe cria uma interface gráfica para capturar dados de gastos diários
 * e
 * enviá-los para um banco de dados.
 * Extende JFrame para criar uma janela de aplicação.
 */
public class Tela_App_gastos_dia extends JFrame {

    private JTextField dataField;
    private JTextField aluguelParcelamentoVeiculoField;
    private JTextField combustivelField;
    private JTextField pedagioField;
    private JTextField multaField;
    private JTextField alimentacaoField;
    private JTextField manutencaoVeiculoField;
    private JTextField investimentoField;
    private JTextField consorcioField;
    private JButton submitButton;

    /**
     * Construtor da classe Tela_App_gastos_dia
     * Configura o JFrame e inicializa os componentes da interface gráfica.
     */
    public Tela_App_gastos_dia() {
        // Configurações do JFrame
        setTitle("Gastos do Dia"); // Título da janela
        setSize(400, 400); // Define o tamanho da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ação ao fechar a janela
        setLocationRelativeTo(null); // Centraliza a janela na tela

        // Layout do JFrame
        setLayout(new GridLayout(10, 2)); // Define o layout de grade (10 linhas e 2 colunas)

        // Inicialização dos componentes do formulário
        JLabel dataLabel = new JLabel("Data:");
        dataField = new JTextField();

        JLabel aluguelParcelamentoVeiculoLabel = new JLabel("Aluguel/Parcelamento Veículo:");
        aluguelParcelamentoVeiculoField = new JTextField();

        JLabel combustivelLabel = new JLabel("Combustível:");
        combustivelField = new JTextField();

        JLabel pedagioLabel = new JLabel("Pedágio:");
        pedagioField = new JTextField();

        JLabel multaLabel = new JLabel("Multa:");
        multaField = new JTextField();

        JLabel alimentacaoLabel = new JLabel("Alimentação:");
        alimentacaoField = new JTextField();

        JLabel manutencaoVeiculoLabel = new JLabel("Manutenção Veículo:");
        manutencaoVeiculoField = new JTextField();

        JLabel investimentoLabel = new JLabel("Investimento:");
        investimentoField = new JTextField();

        JLabel consorcioLabel = new JLabel("Consórcio:");
        consorcioField = new JTextField();

        submitButton = new JButton("Enviar"); // Botão para enviar os dados

        // Adiciona os componentes ao JFrame
        add(dataLabel);
        add(dataField);
        add(aluguelParcelamentoVeiculoLabel);
        add(aluguelParcelamentoVeiculoField);
        add(combustivelLabel);
        add(combustivelField);
        add(pedagioLabel);
        add(pedagioField);
        add(multaLabel);
        add(multaField);
        add(alimentacaoLabel);
        add(alimentacaoField);
        add(manutencaoVeiculoLabel);
        add(manutencaoVeiculoField);
        add(investimentoLabel);
        add(investimentoField);
        add(consorcioLabel);
        add(consorcioField);
        add(submitButton);

        // Adiciona ação ao botão
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cadastrarGastosDia();
            }
        });
    }

    /**
     * Método para capturar os dados do formulário e cadastrar no banco de dados.
     */
    private void cadastrarGastosDia() {
        try {
            // Cria um novo objeto GastosDia e preenche com os dados do formulário
            GastosDia gastos = new GastosDia();
            gastos.setData(LocalDate.parse(dataField.getText()));
            gastos.setAluguelParcelamentoVeiculo(Float.parseFloat(aluguelParcelamentoVeiculoField.getText()));
            gastos.setCombustivel(Float.parseFloat(combustivelField.getText()));
            gastos.setPedagio(Float.parseFloat(pedagioField.getText()));
            gastos.setMulta(Float.parseFloat(multaField.getText()));
            gastos.setAlimentacao(Float.parseFloat(alimentacaoField.getText()));
            gastos.setManutencaoVeiculo(Float.parseFloat(manutencaoVeiculoField.getText()));
            gastos.setInvestimento(Float.parseFloat(investimentoField.getText()));
            gastos.setConsorcio(Float.parseFloat(consorcioField.getText()));

            // Chama o DAO para salvar o objeto no banco de dados
            GastosDiaDAO dao = new GastosDiaDAO();
            dao.cadastrarGastosDia(gastos);

            // Exibe mensagem de sucesso
            JOptionPane.showMessageDialog(this, "Dados cadastrados com sucesso!");
        } catch (NumberFormatException ex) {
            // Exibe mensagem de erro se houver problemas na conversão de valores numéricos
            JOptionPane.showMessageDialog(this, "Erro nos valores inseridos. Verifique os dados e tente novamente.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException ex) {
            // Exibe mensagem de erro se houver problemas na conversão da data
            JOptionPane.showMessageDialog(this, "Erro na data inserida. Use o formato AAAA-MM-DD.", "Erro",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            // Exibe mensagem de erro para qualquer outro problema
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar dados: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Método principal para iniciar a aplicação.
     * 
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        // Cria e exibe a tela
        Tela_App_gastos_dia tela = new Tela_App_gastos_dia();
        tela.setVisible(true);
    }
}
