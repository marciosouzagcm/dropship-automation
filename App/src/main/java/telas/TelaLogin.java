package telas;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import conexao.Conexao;
import service.UsuarioService;

@Component
public class TelaLogin extends JFrame implements ActionListener {

    private JPanel panel;
    private JLabel userLabel;
    private JLabel passwordLabel;
    private JTextField userTextField;
    private JPasswordField passwordField;
    private JButton loginButton;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

    public TelaLogin() {
        initComponents();
    }

    private void initComponents() {
        setTitle("System-Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(10, 10, 10, 10);

        userLabel = new JLabel("Usuário:");
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.EAST;
        panel.add(userLabel, constraints);

        userTextField = new JTextField(20);
        constraints.gridx = 1;
        panel.add(userTextField, constraints);

        passwordLabel = new JLabel("Senha:");
        constraints.gridy = 1;
        constraints.gridx = 0;
        panel.add(passwordLabel, constraints);

        passwordField = new JPasswordField(20);
        constraints.gridx = 1;
        panel.add(passwordField, constraints);

        loginButton = new JButton("Login");
        constraints.gridy = 2;
        constraints.gridx = 1;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(loginButton, constraints);

        loginButton.addActionListener(this);

        add(panel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            logar();
        }
    }

    private void logar() {
        String usuario = userTextField.getText();
        String senha = new String(passwordField.getPassword());

        if (validarLogin(usuario, senha)) {
            JOptionPane.showMessageDialog(this, "Login bem-sucedido!");
            // Se o login for bem-sucedido, você pode fazer algo aqui, como abrir outra
            // janela
        } else {
            JOptionPane.showMessageDialog(this, "Usuário ou senha incorretos!");
        }
    }

    private boolean validarLogin(String usuario, String senha) {
        try {
            UserDetails userDetails = usuarioService.loadUserByUsername(usuario);
            return passwordEncoder.matches(senha, userDetails.getPassword());
        } catch (UsernameNotFoundException e) {
            JOptionPane.showMessageDialog(null, "Erro: " + e.getMessage());
        }
        return false;
    }

    public static void main(String[] args) {
        new TelaLogin();
    }
}