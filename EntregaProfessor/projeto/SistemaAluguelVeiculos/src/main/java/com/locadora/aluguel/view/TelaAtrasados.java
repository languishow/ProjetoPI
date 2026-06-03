package com.locadora.aluguel.view;

import com.locadora.aluguel.dao.AluguelDAO;
import com.locadora.aluguel.model.Aluguel;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TelaAtrasados extends JInternalFrame {

    private JTable            tabela;
    private DefaultTableModel modeloTabela;
    private JButton           btnAtualizar;
    private JButton           btnMarcarEntregue;
    private JLabel            lblContador;
    private JLabel            lblUltimaAtualizacao;
    private Timer             timerAuto;

    private ArrayList<Aluguel> listaAtual = new ArrayList<>();

    private final AluguelDAO       dao     = new AluguelDAO();
    private final SimpleDateFormat sdf     = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm:ss");

    private static final int COL_DIAS = 4;

    public TelaAtrasados(JDesktopPane desktop) {
        super("Veículos com Entrega Atrasada", true, true, true, true);
        initComponents();
        carregar();
        iniciarTimer();
        setSize(720, 420);
        setLocation(100, 100);
    }

    private void initComponents() {
        setLayout(new BorderLayout(4, 4));

        // ── Barra superior ──────────────────────────────────────────────────────
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topo.setBorder(BorderFactory.createEtchedBorder());

        btnAtualizar      = new JButton("Atualizar");
        btnMarcarEntregue = new JButton("Marcar como Entregue");
        btnMarcarEntregue.setEnabled(false);

        lblContador = new JLabel("Nenhum atraso encontrado.");
        lblContador.setFont(new Font("SansSerif", Font.BOLD, 12));

        topo.add(btnAtualizar);
        topo.add(btnMarcarEntregue);
        topo.add(Box.createHorizontalStrut(16));
        topo.add(lblContador);

        // ── Tabela ──────────────────────────────────────────────────────────────
        modeloTabela = new DefaultTableModel(
                new String[]{"Placa", "Modelo", "Cliente", "Entrega Prevista", "Dias de Atraso"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == COL_DIAS ? Integer.class : String.class;
            }
        };

        tabela = new JTable(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(22);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(130);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(110);

        AtrasadoRenderer renderer = new AtrasadoRenderer();
        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // ── Rodapé ──────────────────────────────────────────────────────────────
        JPanel legenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        legenda.add(criarLegenda(new Color(255, 220, 100), "Até 7 dias de atraso"));
        legenda.add(criarLegenda(new Color(220, 60,  60),  "Mais de 7 dias de atraso"));

        lblUltimaAtualizacao = new JLabel("Última atualização: --:--:--");
        lblUltimaAtualizacao.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblUltimaAtualizacao.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.add(legenda,              BorderLayout.WEST);
        rodape.add(lblUltimaAtualizacao, BorderLayout.EAST);

        add(topo,                    BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);
        add(rodape,                  BorderLayout.SOUTH);

        // ── Listeners ───────────────────────────────────────────────────────────
        btnAtualizar.addActionListener(e -> carregar());
        btnMarcarEntregue.addActionListener(e -> marcarEntregue());

        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                btnMarcarEntregue.setEnabled(tabela.getSelectedRow() >= 0);
            }
        });

        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                if (timerAuto != null) timerAuto.stop();
            }
        });
    }

    private void iniciarTimer() {
        timerAuto = new Timer(60_000, e -> carregar());
        timerAuto.setInitialDelay(60_000);
        timerAuto.start();
    }

    private void carregar() {
        modeloTabela.setRowCount(0);
        listaAtual.clear();
        try {
            listaAtual = dao.buscarAtrasados();
            long hoje = System.currentTimeMillis();

            for (Aluguel a : listaAtual) {
                long diffMs    = hoje - a.getDataentrega().getTime();
                int  diasAtraso = (int) (diffMs / (1000L * 60 * 60 * 24));

                modeloTabela.addRow(new Object[]{
                    a.getVeiculo().getPlaca(),
                    a.getVeiculo().getModelo(),
                    a.getCliente().getNomeCliente(),
                    a.getDataentrega() != null ? sdf.format(a.getDataentrega()) : "",
                    diasAtraso
                });
            }

            if (listaAtual.isEmpty()) {
                lblContador.setForeground(new Color(0, 130, 0));
                lblContador.setText("Nenhum veículo em atraso.");
            } else {
                lblContador.setForeground(new Color(180, 0, 0));
                lblContador.setText(listaAtual.size() + " veículo(s) com entrega atrasada.");
            }

            lblUltimaAtualizacao.setText("Última atualização: " + sdfHora.format(new java.util.Date()));
            btnMarcarEntregue.setEnabled(false);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao carregar atrasos:\n" + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void marcarEntregue() {
        int row = tabela.getSelectedRow();
        if (row < 0 || row >= listaAtual.size()) return;

        Aluguel a    = listaAtual.get(row);
        String placa = a.getVeiculo().getPlaca();

        int resp = JOptionPane.showConfirmDialog(this,
            "Confirma a entrega do veículo " + placa + "?",
            "Confirmar Entrega", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resp != JOptionPane.YES_OPTION) return;

        try {
            dao.marcarEntregue(a.getIdaluguel());
            carregar();
            JOptionPane.showMessageDialog(this,
                "Entrega registrada com sucesso!",
                "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao registrar entrega:\n" + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Renderer colorido por linha ─────────────────────────────────────────────
    private class AtrasadoRenderer extends DefaultTableCellRenderer {
        private static final Color COR_LEVE  = new Color(255, 220, 100);
        private static final Color COR_GRAVE = new Color(220, 60,  60);
        private static final Color COR_SEL   = new Color(100, 149, 237);

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focus, int row, int col) {

            Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, col);
            if (!selected) {
                Object diasObj = t.getModel().getValueAt(row, COL_DIAS);
                int dias = (diasObj instanceof Integer) ? (Integer) diasObj : 0;
                c.setBackground(dias > 7 ? COR_GRAVE : COR_LEVE);
                c.setForeground(dias > 7 ? Color.WHITE : Color.BLACK);
            } else {
                c.setBackground(COR_SEL);
                c.setForeground(Color.WHITE);
            }
            return c;
        }
    }

    // ── Helper legenda ──────────────────────────────────────────────────────────
    private JPanel criarLegenda(Color cor, String texto) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JLabel icone = new JLabel("   ");
        icone.setOpaque(true);
        icone.setBackground(cor);
        icone.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        p.add(icone);
        p.add(new JLabel(texto));
        return p;
    }
}
