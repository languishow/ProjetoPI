package com.locadora.aluguel.view;

import com.locadora.aluguel.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TelaPrincipal extends JFrame {

    private JDesktopPane desktopPane;
    private final Usuario usuarioLogado;

    public TelaPrincipal(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
        initComponents();
    }

    private void initComponents() {
        setTitle("Sistema de Aluguel de Veículos  —  Usuário: " + usuarioLogado.getNome());
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        desktopPane = new JDesktopPane();
        desktopPane.setBackground(new Color(60, 90, 130));
        add(desktopPane, BorderLayout.CENTER);

        setJMenuBar(criarMenuBar());

        JLabel lblRodape = new JLabel("  " + usuarioLogado.getCargo() + " | " + usuarioLogado.getNome());
        lblRodape.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblRodape.setBorder(BorderFactory.createEtchedBorder());
        add(lblRodape, BorderLayout.SOUTH);
    }

    private JMenuBar criarMenuBar() {
        Font fonteMenu    = new Font("Arial", Font.PLAIN, 16);
        Font fonteSubitem = new Font("Arial", Font.PLAIN, 13);

        JMenuBar menuBar = new JMenuBar();

        // ── Cadastros ──
        JMenu menuCadastros = new JMenu("Cadastros");
        menuCadastros.setFont(fonteMenu);
        menuCadastros.setMnemonic(KeyEvent.VK_C);

        JMenuItem itemClientes = new JMenuItem("Clientes");
        JMenuItem itemVeiculos = new JMenuItem("Veículos");
        JMenuItem itemAlugueis = new JMenuItem("Aluguéis");
        itemClientes.setFont(fonteSubitem);
        itemVeiculos.setFont(fonteSubitem);
        itemAlugueis.setFont(fonteSubitem);

        itemClientes.addActionListener(e -> abrirInterna(new TelaCliente(desktopPane)));
        itemVeiculos.addActionListener(e -> abrirInterna(new TelaVeiculo(desktopPane)));
        itemAlugueis.addActionListener(e -> abrirInterna(new TelaAluguel(desktopPane)));

        menuCadastros.add(itemClientes);
        menuCadastros.add(itemVeiculos);
        menuCadastros.add(itemAlugueis);

        // ── Relatórios ──
        JMenu menuRelatorios = new JMenu("Relatórios");
        menuRelatorios.setFont(fonteMenu);
        menuRelatorios.setMnemonic(KeyEvent.VK_R);

        JMenuItem itemFaturamento = new JMenuItem("Faturamento");
        JMenuItem itemAtrasados   = new JMenuItem("Veículos Atrasados");
        itemFaturamento.setFont(fonteSubitem);
        itemAtrasados.setFont(fonteSubitem);

        itemFaturamento.addActionListener(e -> abrirInterna(new TelaFaturamento(desktopPane)));
        itemAtrasados.addActionListener(e -> abrirInterna(new TelaAtrasados(desktopPane)));

        menuRelatorios.add(itemFaturamento);
        menuRelatorios.add(itemAtrasados);

        // ── Sistema ──
        JMenu menuSistema = new JMenu("Sistema");
        menuSistema.setFont(fonteMenu);
        menuSistema.setMnemonic(KeyEvent.VK_S);

        JMenuItem itemSair = new JMenuItem("Sair");
        itemSair.setFont(fonteSubitem);
        itemSair.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Deseja sair do sistema?",
                    "Confirmar saída", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new TelaLogin().setVisible(true);
            }
        });

        menuSistema.add(itemSair);

        menuBar.add(menuCadastros);
        menuBar.add(menuRelatorios);
        menuBar.add(menuSistema);

        return menuBar;
    }

    private void abrirInterna(JInternalFrame tela) {
        for (JInternalFrame f : desktopPane.getAllFrames()) {
            if (f.getClass() == tela.getClass()) {
                try {
                    f.setSelected(true);
                    f.setIcon(false);
                } catch (Exception ex) { /* ignora */ }
                return;
            }
        }
        desktopPane.add(tela);
        tela.setVisible(true);
    }
}
