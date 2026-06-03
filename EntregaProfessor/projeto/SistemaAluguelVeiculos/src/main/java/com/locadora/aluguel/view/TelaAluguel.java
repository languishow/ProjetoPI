package com.locadora.aluguel.view;

import com.locadora.aluguel.dao.AluguelDAO;
import com.locadora.aluguel.dao.ClienteDAO;
import com.locadora.aluguel.dao.VeiculoDAO;
import com.locadora.aluguel.model.Aluguel;
import com.locadora.aluguel.model.Cliente;
import com.locadora.aluguel.model.Veiculo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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

public class TelaAluguel extends JInternalFrame {

    private JComboBox<Veiculo>  cmbVeiculo;
    private JComboBox<Cliente>  cmbCliente;
    private JTextField          txtDataAluguel, txtDataEntrega, txtValor, txtObservacao;
    private JCheckBox           chkEntregue;
    private JButton             btnNovo, btnSalvar, btnAlterar, btnExcluir, btnLimpar;
    private JTable              tabela;
    private DefaultTableModel   modeloTabela;

    private final AluguelDAO daoAluguel = new AluguelDAO();
    private final ClienteDAO daoCliente = new ClienteDAO();
    private final VeiculoDAO daoVeiculo = new VeiculoDAO();

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private int idAluguelSelecionado = -1;

    public TelaAluguel(JDesktopPane desktop) {
        super("Cadastro de Aluguéis", true, true, true, true);
        sdf.setLenient(false);
        initComponents();
        carregarCombos(true);
        carregarTabela();
        setSize(860, 560);
        setLocation(60, 60);
    }

    private void initComponents() {
        setLayout(new BorderLayout(4, 4));

        // ── Formulário ──────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Dados do Aluguel"));
        GridBagConstraints g = new GridBagConstraints();
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 6, 4, 6);

        cmbVeiculo     = new JComboBox<>();
        cmbCliente     = new JComboBox<>();
        txtDataAluguel = new JTextField(10);
        txtDataEntrega = new JTextField(10);
        txtValor       = new JTextField(10);
        txtObservacao  = new JTextField(40);
        chkEntregue    = new JCheckBox("Entregue?");

        aplicarMascaraData(txtDataAluguel);
        aplicarMascaraData(txtDataEntrega);
        aplicarMascaraValor();

        // linha 0 — Veículo
        lbl(form, "Veículo *:", g, 0, 0, 0.0);
        g.gridx = 1; g.gridy = 0; g.gridwidth = 3; g.weightx = 1.0;
        form.add(cmbVeiculo, g); g.gridwidth = 1;

        // linha 1 — Cliente
        lbl(form, "Cliente *:", g, 0, 1, 0.0);
        g.gridx = 1; g.gridy = 1; g.gridwidth = 3; g.weightx = 1.0;
        form.add(cmbCliente, g); g.gridwidth = 1;

        // linha 2 — Data Aluguel | Data Entrega
        lbl(form, "Data Aluguel *:", g, 0, 2, 0.0);
        g.gridx = 1; g.gridy = 2; g.weightx = 0.2; form.add(txtDataAluguel, g);
        lbl(form, "Data Entrega *:", g, 2, 2, 0.0);
        g.gridx = 3; g.gridy = 2; g.weightx = 0.2; form.add(txtDataEntrega, g);

        // linha 3 — Valor | Entregue
        lbl(form, "Valor (R$) *:", g, 0, 3, 0.0);
        g.gridx = 1; g.gridy = 3; g.weightx = 0.2; form.add(txtValor, g);
        g.gridx = 2; g.gridy = 3; g.gridwidth = 2; g.weightx = 0.0;
        form.add(chkEntregue, g); g.gridwidth = 1;

        // linha 4 — Observação
        lbl(form, "Observação:", g, 0, 4, 0.0);
        g.gridx = 1; g.gridy = 4; g.gridwidth = 3; g.weightx = 1.0;
        form.add(txtObservacao, g); g.gridwidth = 1;

        txtDataAluguel.setToolTipText("dd/MM/yyyy");
        txtDataEntrega.setToolTipText("dd/MM/yyyy");
        txtValor.setToolTipText("Ex: 1.500,00");

        // ── Botões ──────────────────────────────────────────────────────────────
        btnNovo    = new JButton("Novo");
        btnSalvar  = new JButton("Salvar");
        btnAlterar = new JButton("Alterar");
        btnExcluir = new JButton("Excluir");
        btnLimpar  = new JButton("Limpar");

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 6));
        botoes.add(btnNovo); botoes.add(btnSalvar); botoes.add(btnAlterar);
        botoes.add(btnExcluir); botoes.add(btnLimpar);

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(form,   BorderLayout.CENTER);
        topo.add(botoes, BorderLayout.SOUTH);

        // ── Tabela ──────────────────────────────────────────────────────────────
        modeloTabela = new DefaultTableModel(
                new String[]{"ID", "Veículo", "Cliente", "Data Aluguel", "Data Entrega", "Entregue", "Valor (R$)"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(160);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(100);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(65);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(90);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        setEstadoNovo();
        configurarAcoes();
    }

    // ── Máscara DD/MM/AAAA ─────────────────────────────────────────────────────

    private void aplicarMascaraData(JTextField campo) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void insertString(FilterBypass fb, int off, String str, AttributeSet a)
                    throws BadLocationException {
                String digits = digitsOf(fb) + digitsOf(str);
                aplicar(fb, digits);
            }

            @Override
            public void replace(FilterBypass fb, int off, int len, String str, AttributeSet a)
                    throws BadLocationException {
                String all = digitsOf(fb);
                int d0 = Math.min(digitsAntesPos(fb, off),       all.length());
                int d1 = Math.min(digitsAntesPos(fb, off + len), all.length());
                String combined = all.substring(0, d0) + digitsOf(str) + all.substring(d1);
                aplicar(fb, combined);
            }

            @Override
            public void remove(FilterBypass fb, int off, int len) throws BadLocationException {
                String all = digitsOf(fb);
                int d0 = Math.min(digitsAntesPos(fb, off),       all.length());
                int d1 = Math.min(digitsAntesPos(fb, off + len), all.length());
                if (d0 == d1 && d0 > 0) d0--;
                aplicar(fb, all.substring(0, d0) + all.substring(d1));
            }

            private String digitsOf(FilterBypass fb) throws BadLocationException {
                return fb.getDocument().getText(0, fb.getDocument().getLength()).replaceAll("\\D", "");
            }

            private String digitsOf(String s) {
                return s == null ? "" : s.replaceAll("\\D", "");
            }

            private int digitsAntesPos(FilterBypass fb, int pos) throws BadLocationException {
                if (pos <= 0) return 0;
                int end = Math.min(pos, fb.getDocument().getLength());
                return (int) fb.getDocument().getText(0, end).chars()
                        .filter(c -> c >= '0' && c <= '9').count();
            }

            private void aplicar(FilterBypass fb, String digits) throws BadLocationException {
                if (digits.length() > 8) digits = digits.substring(0, 8);
                String formatted = mascara(digits);
                fb.replace(0, fb.getDocument().getLength(), formatted, null);
                final String f = formatted;
                SwingUtilities.invokeLater(() -> {
                    if (f.equals(campo.getText())) campo.setCaretPosition(f.length());
                });
            }

            // DD/MM/AAAA: barras após posições 1 e 3 no stream de dígitos
            private String mascara(String d) {
                if (d.isEmpty()) return "";
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < d.length(); i++) {
                    if (i == 2 || i == 4) sb.append('/');
                    sb.append(d.charAt(i));
                }
                return sb.toString();
            }
        });
    }

    // ── Máscara de valor (formato brasileiro) ──────────────────────────────────

    private void aplicarMascaraValor() {
        ((AbstractDocument) txtValor.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void insertString(FilterBypass fb, int off, String str, AttributeSet a)
                    throws BadLocationException {
                if (str == null) return;
                String displayed  = getText(fb);
                String currentRaw = getRaw(displayed);
                int r0 = Math.min(rawPos(displayed, off), currentRaw.length());
                String newRaw = currentRaw.substring(0, r0) + filtrar(str) + currentRaw.substring(r0);
                if (isValido(newRaw)) aplicar(fb, newRaw);
            }

            @Override
            public void replace(FilterBypass fb, int off, int len, String str, AttributeSet a)
                    throws BadLocationException {
                String displayed  = getText(fb);
                String currentRaw = getRaw(displayed);
                int r0 = Math.min(rawPos(displayed, off),       currentRaw.length());
                int r1 = Math.min(rawPos(displayed, off + len), currentRaw.length());
                String newRaw = currentRaw.substring(0, r0) + filtrar(str) + currentRaw.substring(r1);
                if (isValido(newRaw)) aplicar(fb, newRaw);
            }

            @Override
            public void remove(FilterBypass fb, int off, int len) throws BadLocationException {
                String displayed  = getText(fb);
                String currentRaw = getRaw(displayed);
                int r0 = Math.min(rawPos(displayed, off),       currentRaw.length());
                int r1 = Math.min(rawPos(displayed, off + len), currentRaw.length());
                if (r0 == r1 && r0 > 0) r0--;
                String newRaw = currentRaw.substring(0, r0) + currentRaw.substring(r1);
                aplicar(fb, newRaw);
            }

            private void aplicar(FilterBypass fb, String raw) throws BadLocationException {
                String formatted = formatar(raw);
                fb.replace(0, fb.getDocument().getLength(), formatted, null);
                final String f = formatted;
                SwingUtilities.invokeLater(() -> {
                    if (f.equals(txtValor.getText())) txtValor.setCaretPosition(f.length());
                });
            }

            private String getText(FilterBypass fb) throws BadLocationException {
                return fb.getDocument().getText(0, fb.getDocument().getLength());
            }

            // Remove pontos (separadores de milhar): "1.500,00" → "1500,00"
            private String getRaw(String s) { return s.replace(".", ""); }

            // Aceita só dígitos e vírgula
            private String filtrar(String s) {
                return s == null ? "" : s.replaceAll("[^0-9,]", "");
            }

            // Posição no display → posição no raw (pula pontos)
            private int rawPos(String displayed, int pos) {
                int raw = 0;
                int limit = Math.min(pos, displayed.length());
                for (int i = 0; i < limit; i++) {
                    if (displayed.charAt(i) != '.') raw++;
                }
                return raw;
            }

            private boolean isValido(String raw) {
                if (raw.startsWith(",")) return false;
                int commas = (int) raw.chars().filter(c -> c == ',').count();
                if (commas > 1) return false;
                int ci = raw.indexOf(',');
                return ci < 0 || raw.length() - ci - 1 <= 2;
            }

            // "1500,50" → "1.500,50" | "150" → "150" | "150,00" → "150,00"
            private String formatar(String raw) {
                if (raw == null || raw.isEmpty()) return "";
                int ci = raw.indexOf(',');
                String intPart = ci >= 0 ? raw.substring(0, ci) : raw;
                String decPart = ci >= 0 ? "," + raw.substring(ci + 1) : "";
                if (intPart.length() <= 3) return intPart + decPart;
                StringBuilder sb = new StringBuilder();
                int start = intPart.length() % 3;
                if (start > 0) sb.append(intPart, 0, start);
                for (int i = start; i < intPart.length(); i += 3) {
                    if (sb.length() > 0) sb.append('.');
                    sb.append(intPart, i, i + 3);
                }
                return sb + decPart;
            }
        });
    }

    // ── Estado dos botões ──────────────────────────────────────────────────────

    private void setEstadoNovo() {
        btnSalvar.setEnabled(true);
        btnAlterar.setEnabled(false);
        btnExcluir.setEnabled(false);
    }

    private void setEstadoEdicao() {
        btnSalvar.setEnabled(false);
        btnAlterar.setEnabled(true);
        btnExcluir.setEnabled(true);
    }

    // ── Ações ──────────────────────────────────────────────────────────────────

    private void configurarAcoes() {
        btnNovo.addActionListener(e   -> { limpar(); carregarCombos(true); });
        btnLimpar.addActionListener(e -> { limpar(); carregarCombos(true); });

        btnSalvar.addActionListener(e -> {
            if (!validar()) return;
            try {
                daoAluguel.inserir(construir());
                carregarTabela();
                limpar();
                carregarCombos(true);
                JOptionPane.showMessageDialog(this, "Aluguel registrado com sucesso.");
            } catch (SQLException ex) {
                erro("salvar", ex);
            }
        });

        btnAlterar.addActionListener(e -> {
            if (idAluguelSelecionado < 0) { aviso("Selecione um aluguel na tabela."); return; }
            if (!validar()) return;
            try {
                Aluguel a = construir();
                a.setIdaluguel(idAluguelSelecionado);
                daoAluguel.atualizar(a);
                carregarTabela();
                limpar();
                carregarCombos(true);
                JOptionPane.showMessageDialog(this, "Aluguel alterado com sucesso.");
            } catch (SQLException ex) {
                erro("alterar", ex);
            }
        });

        btnExcluir.addActionListener(e -> {
            if (idAluguelSelecionado < 0) { aviso("Selecione um aluguel na tabela."); return; }
            if (JOptionPane.showConfirmDialog(this, "Confirma exclusão do aluguel?",
                    "Excluir", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    daoAluguel.excluir(idAluguelSelecionado);
                    carregarTabela();
                    limpar();
                    carregarCombos(true);
                } catch (SQLException ex) {
                    erro("excluir", ex);
                }
            }
        });

        tabela.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting() || tabela.getSelectedRow() < 0) return;
            int r = tabela.getSelectedRow();
            idAluguelSelecionado = Integer.parseInt(val(r, 0));

            // Carrega todos os veículos para permitir selecionar o já alugado
            carregarCombos(false);

            selecionarCombo(cmbVeiculo, val(r, 1));
            selecionarCombo(cmbCliente, val(r, 2));

            txtDataAluguel.setText(val(r, 3));          // "DD/MM/AAAA" → máscara processa
            txtDataEntrega.setText(val(r, 4));
            chkEntregue.setSelected("S".equals(val(r, 5)));
            txtValor.setText(val(r, 6));                // "1.500,00" BR → máscara processa

            setEstadoEdicao();
        });
    }

    // ── Carga de dados ─────────────────────────────────────────────────────────

    private void carregarCombos(boolean apenasDisponiveis) {
        cmbVeiculo.removeAllItems();
        cmbCliente.removeAllItems();
        try {
            for (Veiculo v : daoVeiculo.buscarTodos()) {
                if (!apenasDisponiveis || daoVeiculo.veiculoDisponivel(v.getNumero())) {
                    cmbVeiculo.addItem(v);
                }
            }
            for (Cliente c : daoCliente.buscarTodos()) {
                cmbCliente.addItem(c);
            }
        } catch (SQLException ex) {
            erro("carregar combos", ex);
        }
    }

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        try {
            for (Aluguel a : daoAluguel.buscarTodos()) {
                modeloTabela.addRow(new Object[]{
                    a.getIdaluguel(),
                    a.getVeiculo().toString(),
                    a.getCliente().toString(),
                    a.getDataAluguel() != null ? sdf.format(a.getDataAluguel()) : "",
                    a.getDataentrega() != null ? sdf.format(a.getDataentrega()) : "",
                    String.valueOf(a.getEntregue()),
                    formatarValor(a.getValorPago())     // "1.500,00" — compatível com a máscara
                });
            }
        } catch (SQLException ex) {
            erro("carregar", ex);
        }
    }

    // ── Validação e construção ──────────────────────────────────────────────────

    private boolean validar() {
        if (cmbVeiculo.getSelectedItem() == null) {
            aviso("Selecione um veículo."); return false;
        }
        if (cmbCliente.getSelectedItem() == null) {
            aviso("Selecione um cliente."); return false;
        }
        Date dataAluguel = parseDate(txtDataAluguel.getText());
        if (dataAluguel == null) {
            aviso("Data de aluguel inválida. Use dd/MM/yyyy.");
            txtDataAluguel.requestFocus(); return false;
        }
        Date dataEntrega = parseDate(txtDataEntrega.getText());
        if (dataEntrega == null) {
            aviso("Data de entrega inválida. Use dd/MM/yyyy.");
            txtDataEntrega.requestFocus(); return false;
        }
        if (dataEntrega.before(dataAluguel)) {
            aviso("A data de entrega não pode ser anterior à data do aluguel.");
            txtDataEntrega.requestFocus(); return false;
        }
        String valorStr = txtValor.getText().trim();
        if (valorStr.isEmpty()) {
            aviso("Informe o valor do aluguel."); txtValor.requestFocus(); return false;
        }
        try {
            double v = parseValor(valorStr);
            if (v <= 0.0) { aviso("Informe o valor do aluguel."); txtValor.requestFocus(); return false; }
        } catch (NumberFormatException e) {
            aviso("Informe o valor do aluguel."); txtValor.requestFocus(); return false;
        }
        return true;
    }

    private Aluguel construir() {
        Veiculo v = (Veiculo) cmbVeiculo.getSelectedItem();
        Cliente c = (Cliente) cmbCliente.getSelectedItem();
        double valor = 0.0;
        try { valor = parseValor(txtValor.getText()); }
        catch (NumberFormatException ignored) {}
        return new Aluguel(
            0, v, c,
            parseDate(txtDataAluguel.getText()),
            parseDate(txtDataEntrega.getText()),
            chkEntregue.isSelected() ? 'S' : 'N',
            txtObservacao.getText().trim(),
            valor
        );
    }

    private void limpar() {
        txtDataAluguel.setText("");
        txtDataEntrega.setText("");
        txtValor.setText("");
        txtObservacao.setText("");
        chkEntregue.setSelected(false);
        idAluguelSelecionado = -1;
        tabela.clearSelection();
        setEstadoNovo();
    }

    // ── Utilitários ────────────────────────────────────────────────────────────

    // double 1500.0 → "1.500,00"  |  150.0 → "150,00"
    private String formatarValor(double valor) {
        try {
            // Locale.US garante ponto como separador decimal em String.format
            String s    = String.format(java.util.Locale.US, "%.2f", valor);
            int    dot  = s.indexOf('.');
            if (dot < 0) return "0,00";
            String intP = s.substring(0, dot);
            String decP = s.substring(dot + 1);
            if (intP.isEmpty()) return "0," + decP;
            if (intP.length() <= 3) return intP + "," + decP;
            StringBuilder sb = new StringBuilder();
            int start = intP.length() % 3;
            if (start > 0) sb.append(intP, 0, start);
            for (int i = start; i < intP.length(); i += 3) {
                if (sb.length() > 0) sb.append('.');
                sb.append(intP, i, i + 3);
            }
            return sb + "," + decP;
        } catch (Exception e) {
            return "0,00";
        }
    }

    // "1.500,00" → 1500.0
    private double parseValor(String text) {
        return Double.parseDouble(text.trim().replace(".", "").replace(",", "."));
    }

    private <T> void selecionarCombo(JComboBox<T> combo, String texto) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).toString().equals(texto)) {
                combo.setSelectedIndex(i); return;
            }
        }
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).toString().contains(texto) ||
                    texto.contains(combo.getItemAt(i).toString())) {
                combo.setSelectedIndex(i); return;
            }
        }
    }

    private Date parseDate(String text) {
        try { return new Date(sdf.parse(text.trim()).getTime()); }
        catch (ParseException | NullPointerException e) { return null; }
    }

    private String val(int r, int c) {
        Object o = modeloTabela.getValueAt(r, c); return o == null ? "" : o.toString();
    }
    private void lbl(JPanel p, String t, GridBagConstraints g, int x, int y, double wx) {
        g.gridx = x; g.gridy = y; g.weightx = wx; p.add(new JLabel(t), g);
    }
    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
    private void erro(String op, SQLException ex) {
        JOptionPane.showMessageDialog(this, "Erro ao " + op + ":\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
