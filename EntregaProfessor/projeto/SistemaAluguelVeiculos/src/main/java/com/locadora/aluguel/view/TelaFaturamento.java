package com.locadora.aluguel.view;

import com.locadora.aluguel.dao.AluguelDAO;
import com.locadora.aluguel.model.Aluguel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TelaFaturamento extends JInternalFrame {

    private JTextField        txtDataInicial, txtDataFinal;
    private JButton           btnConsultar;
    private JTable            tabela;
    private DefaultTableModel modeloTabela;
    private JLabel            lblTotal;
    private JLabel            lblLegenda;

    private final AluguelDAO       dao = new AluguelDAO();
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public TelaFaturamento(JDesktopPane desktop) {
        super("Relatório de Faturamento", true, true, true, true);
        sdf.setLenient(false);
        initComponents();
        setSize(760, 480);
        setLocation(80, 80);
    }

    private void initComponents() {
        setLayout(new BorderLayout(4, 4));

        // ── Filtro ──────────────────────────────────────────────────────────────
        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filtro.setBorder(BorderFactory.createTitledBorder("Período"));

        txtDataInicial = new JTextField(10);
        txtDataFinal   = new JTextField(10);
        btnConsultar   = new JButton("Consultar");

        aplicarMascaraData(txtDataInicial);
        aplicarMascaraData(txtDataFinal);

        filtro.add(new JLabel("Data Inicial:"));
        filtro.add(txtDataInicial);
        filtro.add(new JLabel("Data Final:"));
        filtro.add(txtDataFinal);
        filtro.add(btnConsultar);
        filtro.add(new JLabel("(dd/MM/yyyy)"));

        // ── Tabela ──────────────────────────────────────────────────────────────
        modeloTabela = new DefaultTableModel(
                new String[]{"ID", "Veículo", "Cliente", "Data Aluguel", "Data Entrega", "Valor (R$)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modeloTabela) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    Object val = getModel().getValueAt(row, 5);
                    if (val != null) {
                        try {
                            double v = Double.parseDouble(val.toString().replace(",", "."));
                            c.setBackground(v == 0.0 ? Color.YELLOW : Color.WHITE);
                        } catch (NumberFormatException ignored) {
                            c.setBackground(Color.WHITE);
                        }
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        };
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(100);

        // ── Rodapé ──────────────────────────────────────────────────────────────
        lblTotal = new JLabel("  Total no período: R$ 0,00");
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 13));

        lblLegenda = new JLabel("  Aluguel sem valor informado", criarIconeAmarelo(), SwingConstants.LEFT);
        lblLegenda.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblLegenda.setVisible(false);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 4));
        rodape.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(2, 4, 2, 4)));
        rodape.add(lblTotal);
        rodape.add(lblLegenda);

        add(filtro,                    BorderLayout.NORTH);
        add(new JScrollPane(tabela),   BorderLayout.CENTER);
        add(rodape,                    BorderLayout.SOUTH);

        btnConsultar.addActionListener(e -> consultar());
        getRootPane().setDefaultButton(btnConsultar);
    }

    // ── Máscara DD/MM/AAAA ──────────────────────────────────────────────────────
    private void aplicarMascaraData(JTextField campo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int off, String str, AttributeSet a)
                    throws BadLocationException {
                replace(fb, off, 0, str, a);
            }

            @Override
            public void replace(FilterBypass fb, int off, int len, String str, AttributeSet a)
                    throws BadLocationException {
                String digits = str == null ? "" : str.replaceAll("[^0-9]", "");
                String atual  = fb.getDocument().getText(0, fb.getDocument().getLength());
                String apenasDigitos = atual.replace("/", "");
                // Calcula dígitos antes do offset (excluindo barras)
                String antes = atual.substring(0, off).replace("/", "");
                int pos = antes.length();

                StringBuilder sb = new StringBuilder(apenasDigitos);
                sb.delete(pos, pos + len - (atual.substring(off, off + len).replace("/", "").length()));
                for (char c : digits.toCharArray()) {
                    if (sb.length() >= 8) break;
                    sb.insert(pos++, c);
                }

                String raw = sb.toString();
                String mascarado = mascara(raw);
                fb.replace(0, fb.getDocument().getLength(), mascarado, a);

                // Posiciona o cursor após o último dígito inserido
                int novoCursor = Math.min(mascarado.length(), posCursor(raw, pos));
                SwingUtilities.invokeLater(() -> campo.setCaretPosition(
                        Math.min(novoCursor, campo.getText().length())));
            }

            @Override
            public void remove(FilterBypass fb, int off, int len) throws BadLocationException {
                String atual = fb.getDocument().getText(0, fb.getDocument().getLength());
                String raw   = atual.replace("/", "");
                String antes = atual.substring(0, off).replace("/", "");
                int pos = antes.length();
                int fim = atual.substring(0, off + len).replace("/", "").length();
                if (pos == fim && off > 0) pos = Math.max(0, pos - 1);
                StringBuilder sb = new StringBuilder(raw);
                if (pos < fim) sb.delete(pos, fim);
                else if (pos > 0) { sb.deleteCharAt(pos - 1); pos--; }
                String mascarado = mascara(sb.toString());
                fb.replace(0, fb.getDocument().getLength(), mascarado, null);
                int novoCursor = posCursor(sb.toString(), pos);
                SwingUtilities.invokeLater(() -> campo.setCaretPosition(
                        Math.min(novoCursor, campo.getText().length())));
            }

            private String mascara(String d) {
                StringBuilder s = new StringBuilder();
                for (int i = 0; i < d.length() && i < 8; i++) {
                    if (i == 2 || i == 4) s.append('/');
                    s.append(d.charAt(i));
                }
                return s.toString();
            }

            private int posCursor(String raw, int digPos) {
                int count = 0, i = 0;
                String m = mascara(raw);
                while (i < m.length() && count < digPos) {
                    if (m.charAt(i) != '/') count++;
                    i++;
                }
                return i;
            }
        });
    }

    // ── Ícone quadrado amarelo para a legenda ───────────────────────────────────
    private Icon criarIconeAmarelo() {
        return new Icon() {
            @Override public int getIconWidth()  { return 14; }
            @Override public int getIconHeight() { return 14; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(Color.YELLOW);
                g.fillRect(x, y, 14, 14);
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, 13, 13);
            }
        };
    }

    private void consultar() {
        Date inicio = parseDate(txtDataInicial.getText());
        Date fim    = parseDate(txtDataFinal.getText());

        if (inicio == null) {
            aviso("Data inicial inválida. Use dd/MM/yyyy.");
            txtDataInicial.requestFocus();
            return;
        }
        if (fim == null) {
            aviso("Data final inválida. Use dd/MM/yyyy.");
            txtDataFinal.requestFocus();
            return;
        }
        if (fim.before(inicio)) {
            aviso("A data final deve ser igual ou posterior à data inicial.");
            return;
        }

        modeloTabela.setRowCount(0);
        try {
            ArrayList<Aluguel> lista = dao.buscarPorPeriodo(inicio, fim);
            boolean temAmarelo = false;

            for (Aluguel a : lista) {
                double valor = a.getValorPago();
                modeloTabela.addRow(new Object[]{
                    a.getIdaluguel(),
                    a.getVeiculo().toString(),
                    a.getCliente().toString(),
                    a.getDataAluguel()  != null ? sdf.format(a.getDataAluguel())  : "",
                    a.getDataentrega()  != null ? sdf.format(a.getDataentrega())  : "",
                    String.format("%.2f", valor)
                });
                if (valor == 0.0) temAmarelo = true;
            }

            double total = dao.calcularFaturamento(inicio, fim);
            lblTotal.setText(String.format("  Total no período: R$ %,.2f  (%d registro(s))",
                    total, lista.size()));
            lblLegenda.setVisible(temAmarelo);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Erro ao consultar faturamento:\n" + ex.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Date parseDate(String text) {
        try { return new Date(sdf.parse(text.trim()).getTime()); }
        catch (ParseException | NullPointerException e) { return null; }
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
}
