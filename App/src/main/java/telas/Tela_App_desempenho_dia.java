package telas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import entity.DesempenhoDia;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Classe Tela_App_desempenho_dia
 * Esta classe cria uma interface gráfica para capturar dados de desempenho diário e
 * enviá-los para um banco de dados.
 * Extende JFrame para criar uma janela de aplicação.
 */
public class Tela_App_desempenho_dia extends JFrame {

    private JTextField dataField;
    private JTextField corridasDiaUberField;
    private JTextField corridasDia99Field;
    private JTextField corridasDiaIndriveField;
    private JTextField onlineUberField;
    private JTextField online99Field;
    private JTextField onlineIndriveField;
    private JTextField totalKmDiaField;
    private JButton submitButton;

    /**
     * Construtor da classe Tela_App_desempenho_dia
     * Configura o JFrame e inicializa os componentes da interface gráfica.
     */
    public Tela_App_desempenho_dia() {
        // Configurações do JFrame
        setTitle("Desempenho do Dia"); // Título da janela
        setSize(400, 400); // Define o tamanho da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ação ao fechar a janela
        setLocationRelativeTo(null); // Centraliza a janela na tela

        // Layout do JFrame
        setLayout(new GridLayout(9, 2)); // Define o layout de grade (9 linhas e 2 colunas)

        // Inicialização dos componentes do formulário
        JLabel dataLabel = new JLabel("Data:");
        dataField = new JTextField();

        JLabel corridasDiaUberLabel = new JLabel("Corridas no Uber:");
        corridasDiaUberField = new JTextField();

        JLabel corridasDia99Label = new JLabel("Corridas no 99:");
        corridasDia99Field = new JTextField();

        JLabel corridasDiaIndriveLabel = new JLabel("Corridas no InDrive:");
        corridasDiaIndriveField = new JTextField();

        JLabel onlineUberLabel = new JLabel("Tempo Online no Uber (HH:mm):");
        onlineUberField = new JTextField();

        JLabel online99Label = new JLabel("Tempo Online no 99 (HH:mm):");
        online99Field = new JTextField();

        JLabel onlineIndriveLabel = new JLabel("Tempo Online no InDrive (HH:mm):");
        onlineIndriveField = new JTextField();

        JLabel totalKmDiaLabel = new JLabel("Total de Km Percorridos:");
        totalKmDiaField = new JTextField();

        submitButton = new JButton("Enviar"); // Botão para enviar os dados

        // Adiciona os componentes ao JFrame
        add(dataLabel);
        add(dataField);
        add(corridasDiaUberLabel);
        add(corridasDiaUberField);
        add(corridasDia99Label);
        add(corridasDia99Field);
        add(corridasDiaIndriveLabel);
        add(corridasDiaIndriveField);
        add(onlineUberLabel);
        add(onlineUberField);
        add(online99Label);
        add(online99Field);
        add(onlineIndriveLabel);
        add(onlineIndriveField);
        add(totalKmDiaLabel);
        add(totalKmDiaField);
        add(submitButton);

        // Adiciona ação ao botão
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cadastrarDesempenhoDia();
            }
        });
    }

    /**
     * Método para capturar os dados do formulário e cadastrar no banco de dados.
     */
    private void cadastrarDesempenhoDia() {
        try {
            // Cria um novo objeto DesempenhoDia e preenche com os dados do formulário
            DesempenhoDia desempenho = new DesempenhoDia();
            desempenho.setData(LocalDate.parse(dataField.getText()));
            desempenho.setCorridasDiaUber(Integer.parseInt(corridasDiaUberField.getText()));
            desempenho.setCorridasDia99(Integer.parseInt(corridasDia99Field.getText()));
            desempenho.setCorridasDiaIndrive(Integer.parseInt(corridasDiaIndriveField.getText()));
            desempenho.setOnlineUber(onlineUberField.getText());
            desempenho.setOnline99(online99Field.getText());
            desempenho.setOnlineIndrive(onlineIndriveField.getText());
            desempenho.setTotalKmDia(Float.parseFloat(totalKmDiaField.getText()));

            // Chama o DAO para salvar o objeto no banco de dados
            DesempenhoDiaDAO dao = new DesempenhoDiaDAO();
            dao.cadastrarDesempenhoDia(desempenho);

            // Exibe mensagem de sucesso
            JOptionPane.showMessageDialog(this, "Dados cadastrados com sucesso!");
        } catch (NumberFormatException ex) {
            // Exibe mensagem de erro se houver problemas na conversão de valores numéricos
            JOptionPane.showMessageDialog(this, "Erro nos valores inseridos. Verifique os dados e tente novamente.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (DateTimeParseException ex) {
            // Exibe mensagem de erro se houver problemas na conversão da data
            JOptionPane.showMessageDialog(this, "Erro na data inserida. Use o formato AAAA-MM-DD.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            // Exibe mensagem de erro para qualquer outro problema
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar dados: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Método principal para iniciar a aplicação.
     * @param args Argumentos da linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        // Cria e exibe a tela
        Tela_App_desempenho_dia tela = new Tela_App_desempenho_dia();
        tela.setVisible(true);
    }
}
