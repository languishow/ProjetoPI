package com.locadora.aluguel.view;

import com.locadora.aluguel.dao.VeiculoDAO;
import com.locadora.aluguel.model.Veiculo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.sql.SQLException;
import java.util.Calendar;

public class TelaVeiculo extends JInternalFrame {

    private JTextField txtNumero, txtPlaca, txtFabricante, txtModelo;
    private JTextField txtAno, txtPortas, txtAcessorios;
    private JButton btnNovo, btnSalvar, btnAlterar, btnExcluir, btnLimpar;
    private JComboBox<String> cmbFiltro;
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private final VeiculoDAO dao = new VeiculoDAO();

    public TelaVeiculo(JDesktopPane desktop) {
        super("Cadastro de Veículos", true, true, true, true);
        initComponents();
        carregarTabela();
        setSize(820, 530);
        setLocation(40, 40);
    }

    private void initComponents() {
        setLayout(new BorderLayout(4, 4));

        // ── Formulário ──────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Dados do Veículo"));
        GridBagConstraints g = new GridBagConstraints();
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 6, 4, 6);

        txtNumero     = new JTextField(12);
        txtPlaca      = new JTextField(10);
        txtFabricante = new JTextField(20);
        txtModelo     = new JTextField(20);
        txtAno        = new JTextField(6);
        txtPortas     = new JTextField(4);
        txtAcessorios = new JTextField(50);

        aplicarMascaraPlaca();
        aplicarFiltroDigitos(txtAno,    4);
        aplicarFiltroDigitos(txtPortas, 1);

        // linha 0 — Número | Placa
        lbl(form, "Número *:", g, 0, 0, 0.0); fld(form, txtNumero,     g, 1, 0, 0.2);
        lbl(form, "Placa *:",  g, 2, 0, 0.0); fld(form, txtPlaca,      g, 3, 0, 0.15);

        // linha 1 — Fabricante | Modelo
        lbl(form, "Fabricante:", g, 0, 1, 0.0); fld(form, txtFabricante, g, 1, 1, 0.35);
        lbl(form, "Modelo:",     g, 2, 1, 0.0); fld(form, txtModelo,     g, 3, 1, 0.35);

        // linha 2 — Ano | Portas
        lbl(form, "Ano *:",   g, 0, 2, 0.0); fld(form, txtAno,    g, 1, 2, 0.1);
        lbl(form, "Portas:", g, 2, 2, 0.0); fld(form, txtPortas, g, 3, 2, 0.05);

        // linha 3 — Acessórios (span 3 cols)
        lbl(form, "Acessórios:", g, 0, 3, 0.0);
        g.gridx = 1; g.gridy = 3; g.gridwidth = 3; g.weightx = 1.0;
        form.add(txtAcessorios, g);
        g.gridwidth = 1;

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
                new String[]{"Número", "Placa", "Fabricante", "Modelo", "Ano", "Portas", "Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(70);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(80);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(6).setPreferredWidth(90);

        // ── Filtro ──────────────────────────────────────────────────────────────
        JPanel filtroPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filtroPanel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        cmbFiltro = new JComboBox<>(new String[]{"Todos", "Disponíveis", "Alugados"});
        filtroPanel.add(new JLabel("Filtrar por status:"));
        filtroPanel.add(cmbFiltro);

        JPanel tableArea = new JPanel(new BorderLayout());
        tableArea.add(filtroPanel, BorderLayout.NORTH);
        tableArea.add(new JScrollPane(tabela), BorderLayout.CENTER);

        add(topo, BorderLayout.NORTH);
        add(tableArea, BorderLayout.CENTER);

        setEstadoNovo();
        configurarAcoes();
    }

    // ── Máscara de placa (AAA-9999 antigo | AAA9A99 Mercosul) ─────────────────

    private void aplicarMascaraPlaca() {
        ((AbstractDocument) txtPlaca.getDocument()).setDocumentFilter(new DocumentFilter() {

            @Override
            public void insertString(FilterBypass fb, int off, String str, AttributeSet a)
                    throws BadLocationException {
                String newRaw = toRaw(getText(fb)) + toRaw(str);
                aplicar(fb, newRaw);
            }

            @Override
            public void replace(FilterBypass fb, int off, int len, String str, AttributeSet a)
                    throws BadLocationException {
                String displayed   = getText(fb);
                String currentRaw  = toRaw(displayed);
                int    r0          = rawAntes(displayed, off);
                int    r1          = rawAntes(displayed, off + len);
                String newRaw      = currentRaw.substring(0, r0) + toRaw(str) + currentRaw.substring(r1);
                aplicar(fb, newRaw);
            }

            @Override
            public void remove(FilterBypass fb, int off, int len) throws BadLocationException {
                String displayed  = getText(fb);
                String currentRaw = toRaw(displayed);
                int    r0         = rawAntes(displayed, off);
                int    r1         = rawAntes(displayed, off + len);
                // backspace sobre separador '-': remove o caractere bruto anterior
                if (r0 == r1 && r0 > 0) r0--;
                String newRaw = currentRaw.substring(0, r0) + currentRaw.substring(r1);
                aplicar(fb, newRaw);
            }

            private void aplicar(FilterBypass fb, String raw) throws BadLocationException {
                if (raw.length() > 7) raw = raw.substring(0, 7);
                String formatted = formatar(raw);
                fb.replace(0, fb.getDocument().getLength(), formatted, null);
                final String f = formatted;
                SwingUtilities.invokeLater(() -> {
                    if (f.equals(txtPlaca.getText())) txtPlaca.setCaretPosition(f.length());
                });
            }

            private String getText(FilterBypass fb) throws BadLocationException {
                return fb.getDocument().getText(0, fb.getDocument().getLength());
            }

            // Remove tudo que não é letra/dígito e converte para maiúsculas
            private String toRaw(String s) {
                return s == null ? "" : s.toUpperCase().replaceAll("[^A-Z0-9]", "");
            }

            // Quantos caracteres brutos (letras/dígitos) existem antes da posição de exibição
            private int rawAntes(String displayed, int pos) {
                int count = 0;
                int limit = Math.min(pos, displayed.length());
                for (int i = 0; i < limit; i++) {
                    if (Character.isLetterOrDigit(displayed.charAt(i))) count++;
                }
                return count;
            }

            // Formata raw para exibição:
            //   Mercosul (pos 4 é letra): AAA9A99 — sem separador
            //   Antigo ou ambíguo:        AAA-9NNN — hífen após pos 2
            private String formatar(String raw) {
                if (raw.length() < 4) return raw;
                if (raw.length() >= 5 && Character.isLetter(raw.charAt(4))) {
                    return raw; // Mercosul
                }
                return raw.substring(0, 3) + "-" + raw.substring(3); // antigo
            }
        });
    }

    // ── Filtro numérico para Ano e Portas ──────────────────────────────────────

    private void aplicarFiltroDigitos(JTextField campo, int maxLen) {
        ((AbstractDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int off, String str, AttributeSet a)
                    throws BadLocationException {
                if (str == null) return;
                String digits = str.replaceAll("\\D", "");
                if (fb.getDocument().getLength() + digits.length() <= maxLen) {
                    super.insertString(fb, off, digits, a);
                }
            }
            @Override
            public void replace(FilterBypass fb, int off, int len, String str, AttributeSet a)
                    throws BadLocationException {
                String digits = str == null ? "" : str.replaceAll("\\D", "");
                int newLen = fb.getDocument().getLength() - len + digits.length();
                if (newLen <= maxLen) super.replace(fb, off, len, digits, a);
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
        btnNovo.addActionListener(e   -> limpar());
        btnLimpar.addActionListener(e -> limpar());
        cmbFiltro.addActionListener(e -> carregarTabela());

        btnSalvar.addActionListener(e -> {
            if (!validar()) return;
            try {
                dao.inserir(construir());
                carregarTabela();
                limpar();
                JOptionPane.showMessageDialog(this, "Veículo salvo com sucesso.");
            } catch (SQLException ex) {
                erro("salvar", ex);
            }
        });

        btnAlterar.addActionListener(e -> {
            if (txtNumero.getText().trim().isEmpty()) {
                aviso("Selecione um veículo na tabela."); return;
            }
            if (!validar()) return;
            try {
                dao.atualizar(construir());
                carregarTabela();
                limpar();
                JOptionPane.showMessageDialog(this, "Veículo alterado com sucesso.");
            } catch (SQLException ex) {
                erro("alterar", ex);
            }
        });

        btnExcluir.addActionListener(e -> {
            if (tabela.getSelectedRow() < 0) { aviso("Selecione um veículo na tabela."); return; }
            String numero = val(tabela.getSelectedRow(), 0);
            try {
                if (!dao.veiculoDisponivel(numero)) {
                    JOptionPane.showMessageDialog(this,
                        "Não é possível excluir este veículo pois ele possui aluguéis cadastrados no sistema.",
                        "Operação bloqueada", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (SQLException ex) { erro("verificar", ex); return; }

            if (JOptionPane.showConfirmDialog(this, "Confirma exclusão do veículo?",
                    "Excluir", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    dao.excluir(numero);
                    carregarTabela();
                    limpar();
                } catch (SQLException ex) {
                    erro("excluir", ex);
                }
            }
        });

        tabela.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting() || tabela.getSelectedRow() < 0) return;
            int r = tabela.getSelectedRow();
            txtNumero.setText(val(r, 0));
            txtPlaca.setText(val(r, 1));
            txtFabricante.setText(val(r, 2));
            txtModelo.setText(val(r, 3));
            txtAno.setText(val(r, 4));
            txtPortas.setText(val(r, 5));
            try {
                Veiculo v = dao.buscarPorNumero(val(r, 0));
                txtAcessorios.setText(v != null ? v.getAcessorios() : "");
            } catch (SQLException ex) { /* ignora */ }
            txtNumero.setEditable(false);
            setEstadoEdicao();
        });
    }

    // ── Dados ──────────────────────────────────────────────────────────────────

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        String filtro = cmbFiltro != null ? (String) cmbFiltro.getSelectedItem() : "Todos";
        try {
            for (Veiculo v : dao.buscarTodos()) {
                String status;
                try { status = dao.veiculoDisponivel(v.getNumero()) ? "Disponível" : "Alugado"; }
                catch (SQLException e) { status = "?"; }
                if ("Todos".equals(filtro)
                        || ("Disponíveis".equals(filtro) && "Disponível".equals(status))
                        || ("Alugados".equals(filtro) && "Alugado".equals(status))) {
                    modeloTabela.addRow(new Object[]{
                        v.getNumero(), v.getPlaca(), v.getFabricante(),
                        v.getModelo(), v.getAnoModelo(), v.getQtdPortas(), status
                    });
                }
            }
        } catch (SQLException ex) {
            erro("carregar", ex);
        }
    }

    private boolean validar() {
        if (txtNumero.getText().trim().isEmpty()) {
            aviso("Número é obrigatório."); txtNumero.requestFocus(); return false;
        }

        // Placa
        String placa = txtPlaca.getText().trim();
        if (placa.isEmpty()) {
            aviso("Placa é obrigatória."); txtPlaca.requestFocus(); return false;
        }
        boolean placaAntiga    = placa.matches("[A-Z]{3}-[0-9]{4}");
        boolean placaMercosul  = placa.matches("[A-Z]{3}[0-9][A-Z][0-9]{2}");
        if (!placaAntiga && !placaMercosul) {
            aviso("Placa inválida. Use o formato AAA-9999 (antigo) ou AAA9A99 (Mercosul).");
            txtPlaca.requestFocus();
            return false;
        }

        // Ano
        if (txtAno.getText().trim().isEmpty()) {
            aviso("Ano é obrigatório."); txtAno.requestFocus(); return false;
        }
        int anoAtual = Calendar.getInstance().get(Calendar.YEAR);
        try {
            int ano = Integer.parseInt(txtAno.getText().trim());
            if (ano < 1950 || ano > anoAtual) {
                aviso("Ano inválido. Informe um ano entre 1950 e " + anoAtual + ".");
                txtAno.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            aviso("Ano inválido. Informe um ano entre 1950 e " + anoAtual + ".");
            txtAno.requestFocus();
            return false;
        }

        // Portas
        if (!txtPortas.getText().trim().isEmpty()) {
            try {
                int portas = Integer.parseInt(txtPortas.getText().trim());
                if (portas < 2 || portas > 5) {
                    aviso("Quantidade de portas inválida. Informe entre 2 e 5.");
                    txtPortas.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                aviso("Quantidade de portas inválida. Informe entre 2 e 5.");
                txtPortas.requestFocus();
                return false;
            }
        }

        return true;
    }

    private Veiculo construir() {
        return new Veiculo(
            txtNumero.getText().trim(),
            txtPlaca.getText().trim(),
            txtFabricante.getText().trim(),
            txtModelo.getText().trim(),
            Integer.parseInt(txtAno.getText().trim()),
            txtPortas.getText().trim().isEmpty() ? 0 : Integer.parseInt(txtPortas.getText().trim()),
            txtAcessorios.getText().trim()
        );
    }

    private void limpar() {
        txtNumero.setText(""); txtPlaca.setText(""); txtFabricante.setText("");
        txtModelo.setText(""); txtAno.setText(""); txtPortas.setText("");
        txtAcessorios.setText("");
        txtNumero.setEditable(true);
        tabela.clearSelection();
        txtNumero.requestFocus();
        setEstadoNovo();
    }

    // ── helpers ────────────────────────────────────────────────────────────────
    private String val(int r, int c) {
        Object o = modeloTabela.getValueAt(r, c); return o == null ? "" : o.toString();
    }
    private void lbl(JPanel p, String t, GridBagConstraints g, int x, int y, double wx) {
        g.gridx = x; g.gridy = y; g.weightx = wx; p.add(new JLabel(t), g);
    }
    private void fld(JPanel p, JTextField f, GridBagConstraints g, int x, int y, double wx) {
        g.gridx = x; g.gridy = y; g.weightx = wx; p.add(f, g);
    }
    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }
    private void erro(String op, SQLException ex) {
        JOptionPane.showMessageDialog(this, "Erro ao " + op + ":\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
