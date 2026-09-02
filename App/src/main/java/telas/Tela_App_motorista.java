package telas;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import entity.Motorista;

/**
 * Classe Tela_App_motorista
 * Esta classe cria uma interface gráfica para capturar dados de motoristas e
 * enviá-los para um banco de dados.
 * Extende JFrame para criar uma janela de aplicação.
 */
public class Tela_App_motorista extends JFrame {

    private JTextField nomeField;
    private JTextField placaVeiculoField;
    private JButton submitButton;

    /**
     * Construtor da classe Tela_App_motorista
     * Configura o JFrame e inicializa os componentes da interface gráfica.
     */
    public Tela_App_motorista() {
        // Configurações do JFrame
        setTitle("Cadastro de Motorista"); // Título da janela
        setSize(400, 200); // Define o tamanho da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ação ao fechar a janela
        setLocationRelativeTo(null); // Centraliza a janela na tela

        // Layout do JFrame
        setLayout(new GridLayout(3, 2)); // Define o layout de grade (3 linhas e 2 colunas)

        // Inicialização dos componentes do formulário
        JLabel nomeLabel = new JLabel("Nome:");
        nomeField = new JTextField();

        JLabel placaVeiculoLabel = new JLabel("Placa do Veículo (XXX-XXXX):");
        placaVeiculoField = new JTextField();

        submitButton = new JButton("Enviar"); // Botão para enviar os dados

        // Adiciona os componentes ao JFrame
        add(nomeLabel);
        add(nomeField);
        add(placaVeiculoLabel);
        add(placaVeiculoField);
        add(submitButton);

        // Adiciona ação ao botão
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cadastrarMotorista();
            }
        });
    }

    /**
     * Método para capturar os dados do formulário e cadastrar no banco de dados.
     */
    private void cadastrarMotorista() {
        try {
            // Cria um novo objeto Motorista e preenche com os dados do formulário
            Motorista motorista = new Motorista();
            motorista.setNome(nomeField.getText());
            motorista.setPlacaVeiculo(placaVeiculoField.getText());

            // Chama o DAO para salvar o objeto no banco de dados
            MotoristaDAO dao = new MotoristaDAO();
            dao.cadastrarMotorista(motorista);

            // Exibe mensagem de sucesso
            JOptionPane.showMessageDialog(this, "Dados cadastrados com sucesso!");
        } catch (IllegalArgumentException ex) {
            // Exibe mensagem de erro se houver problemas na validação dos dados
            JOptionPane.showMessageDialog(this, "Erro nos dados inseridos: " + ex.getMessage(), "Erro",
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
        Tela_App_motorista tela = new Tela_App_motorista();
        tela.setVisible(true);
    }
}
