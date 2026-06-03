package com.locadora.aluguel.view;

import com.locadora.aluguel.dao.UsuarioDAO;
import com.locadora.aluguel.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;

public class TelaLogin extends JFrame {

    private JTextField txtLogin;
    private JPasswordField txtSenha;
    private JButton btnEntrar;

    public TelaLogin() {
        initComponents();
        configurarAcoes();
    }

    private void initComponents() {
        setTitle("Login - Sistema de Aluguel de Veículos");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill      = GridBagConstraints.HORIZONTAL;
        gbc.insets    = new Insets(6, 6, 6, 6);

        JLabel lblTitulo = new JLabel("Sistema de Aluguel de Veículos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        painel.add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        painel.add(new JLabel("Login:"), gbc);

        txtLogin = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 1;
        painel.add(txtLogin, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        painel.add(new JLabel("Senha:"), gbc);

        txtSenha = new JPasswordField(15);
        gbc.gridx = 1; gbc.gridy = 2;
        painel.add(txtSenha, gbc);

        btnEntrar = new JButton("Entrar");
        btnEntrar.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill  = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(14, 6, 6, 6);
        painel.add(btnEntrar, gbc);

        add(painel);
        getRootPane().setDefaultButton(btnEntrar);
    }

    private void configurarAcoes() {
        btnEntrar.addActionListener(e -> autenticar());

        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) autenticar();
            }
        };
        txtLogin.addKeyListener(enterListener);
        txtSenha.addKeyListener(enterListener);
    }

    private void autenticar() {
        String login = txtLogin.getText().trim();
        String senha = new String(txtSenha.getPassword());

        if (login.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha login e senha.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            UsuarioDAO dao = new UsuarioDAO();
            Usuario usuario = dao.autenticar(login, senha);

            if (usuario != null) {
                new TelaPrincipal(usuario).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Login ou senha inválidos.", "Acesso negado", JOptionPane.ERROR_MESSAGE);
                txtSenha.setText("");
                txtSenha.requestFocus();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao conectar ao banco:\n" + ex.getMessage(),
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
