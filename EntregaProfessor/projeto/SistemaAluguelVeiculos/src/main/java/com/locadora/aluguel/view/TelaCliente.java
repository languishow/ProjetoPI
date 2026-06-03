package com.locadora.aluguel.view;

import com.locadora.aluguel.dao.ClienteDAO;
import com.locadora.aluguel.model.Cliente;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class TelaCliente extends JInternalFrame {

    private JTextField txtCpf, txtNome, txtEndereco, txtTelefone, txtEmail;
    private JComboBox<String> cmbUf;
    private JTextField ufEditor;
    private JButton btnNovo, btnSalvar, btnAlterar, btnExcluir, btnLimpar;
    private JTable tabela;
    private DefaultTableModel modeloTabela;
    private final ClienteDAO dao = new ClienteDAO();

    private static final String[] UFS = {
        "", "AC","AL","AP","AM","BA","CE","DF","ES","GO",
        "MA","MT","MS","MG","PA","PB","PR","PE","PI",
        "RJ","RN","RS","RO","RR","SC","SP","SE","TO"
    };
    private static final List<String> UF_LIST = Arrays.asList(UFS);

    public TelaCliente(JDesktopPane desktop) {
        super("Cadastro de Clientes", true, true, true, true);
        initComponents();
        carregarTabela();
        setSize(780, 520);
        setLocation(20, 20);
    }

    private void initComponents() {
        setLayout(new BorderLayout(4, 4));

        // ── Formulário ──────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Dados do Cliente"));
        GridBagConstraints g = new GridBagConstraints();
        g.fill   = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 6, 4, 6);

        txtCpf      = new JTextField(15);
        txtNome     = new JTextField(30);
        txtEndereco = new JTextField(40);
        cmbUf       = new JComboBox<>(UFS);
        txtTelefone = new JTextField(15);
        txtEmail    = new JTextField(30);

        configurarAutoCompleteUf();
        aplicarMascaraTelefone();

        // linha 0 — CPF | UF
        label(form, "CPF *:", g, 0, 0, 0.0); campo(form, txtCpf, g, 1, 0, 0.3);
        label(form, "UF:", g, 2, 0, 0.0);
        g.gridx = 3; g.gridy = 0; g.weightx = 0.05; form.add(cmbUf, g);

        // linha 1 — Nome (span 3 colunas)
        label(form, "Nome *:", g, 0, 1, 0.0);
        g.gridx = 1; g.gridy = 1; g.gridwidth = 3; g.weightx = 1.0;
        form.add(txtNome, g);
        g.gridwidth = 1;

        // linha 2 — Endereço (span 3 colunas)
        label(form, "Endereço:", g, 0, 2, 0.0);
        g.gridx = 1; g.gridy = 2; g.gridwidth = 3; g.weightx = 1.0;
        form.add(txtEndereco, g);
        g.gridwidth = 1;

        // linha 3 — Telefone | Email
        label(form, "Telefone:", g, 0, 3, 0.0); campo(form, txtTelefone, g, 1, 3, 0.25);
        label(form, "Email:",    g, 2, 3, 0.0); campo(form, txtEmail,    g, 3, 3, 0.7);

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
                new String[]{"CPF", "Nome", "Endereço", "UF", "Telefone", "E-mail"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = new JTable(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getColumnModel().getColumn(0).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(35);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(110);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(150);

        add(topo, BorderLayout.NORTH);
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        setEstadoNovo();
        configurarAcoes();
    }

    // ── Autocompletar UF ───────────────────────────────────────────────────────

    private void configurarAutoCompleteUf() {
        cmbUf.setEditable(true);
        ufEditor = (JTextField) cmbUf.getEditor().getEditorComponent();

        // Garante que o texto digitado seja sempre maiúsculo
        ((AbstractDocument) ufEditor.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int off, String s, AttributeSet a)
                    throws BadLocationException {
                super.insertString(fb, off, s != null ? s.toUpperCase() : s, a);
            }
            @Override
            public void replace(FilterBypass fb, int off, int len, String s, AttributeSet a)
                    throws BadLocationException {
                super.replace(fb, off, len, s != null ? s.toUpperCase() : s, a);
            }
        });

        // Ao inserir caractere, completa com a primeira UF que começa com o texto
        // e seleciona o sufixo sugerido para que o próximo keystroke o substitua
        ufEditor.getDocument().addDocumentListener(new DocumentListener() {
            private boolean completing = false;

            @Override public void changedUpdate(DocumentEvent e) {}
            @Override public void removeUpdate(DocumentEvent e) {}

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (completing) return;
                SwingUtilities.invokeLater(() -> {
                    completing = true;
                    try {
                        String typed = ufEditor.getText();
                        if (typed.isEmpty()) return;
                        for (String uf : UFS) {
                            if (!uf.isEmpty() && uf.startsWith(typed) && !uf.equals(typed)) {
                                // Preenche com a UF e seleciona o sufixo
                                ufEditor.setText(uf);
                                ufEditor.setCaretPosition(uf.length());
                                ufEditor.moveCaretPosition(typed.length());
                                return;
                            }
                        }
                    } finally {
                        completing = false;
                    }
                });
            }
        });

        // Ao sair do campo, valida o conteúdo
        ufEditor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(TelaCliente.this::validarUf);
            }
        });
    }

    private void validarUf() {
        String typed = ufEditor.getText().trim();
        if (typed.isEmpty() || UF_LIST.contains(typed)) {
            cmbUf.setSelectedItem(typed);
            return;
        }
        aviso("UF inválida. Selecione um estado válido.");
        cmbUf.setSelectedIndex(0);
        cmbUf.requestFocus();
    }

    // ── Máscara de telefone ────────────────────────────────────────────────────

    private void aplicarMascaraTelefone() {
        ((AbstractDocument) txtTelefone.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int off, String str, AttributeSet a)
                    throws BadLocationException {
                String digits = digitsOf(fb) + digitsOf(str);
                apply(fb, digits);
            }

            @Override
            public void replace(FilterBypass fb, int off, int len, String str, AttributeSet a)
                    throws BadLocationException {
                String all      = digitsOf(fb);
                int    d0       = digitsBeforePos(fb, off);
                int    d1       = digitsBeforePos(fb, off + len);
                String combined = all.substring(0, d0) + digitsOf(str) + all.substring(d1);
                apply(fb, combined);
            }

            @Override
            public void remove(FilterBypass fb, int off, int len) throws BadLocationException {
                String all = digitsOf(fb);
                int    d0  = digitsBeforePos(fb, off);
                int    d1  = digitsBeforePos(fb, off + len);
                // backspace sobre separador: remove o dígito anterior
                if (d0 == d1 && d0 > 0) d0--;
                apply(fb, all.substring(0, d0) + all.substring(d1));
            }

            private String digitsOf(FilterBypass fb) throws BadLocationException {
                return fb.getDocument().getText(0, fb.getDocument().getLength()).replaceAll("\\D", "");
            }

            private String digitsOf(String s) {
                return s == null ? "" : s.replaceAll("\\D", "");
            }

            private int digitsBeforePos(FilterBypass fb, int pos) throws BadLocationException {
                if (pos <= 0) return 0;
                int end = Math.min(pos, fb.getDocument().getLength());
                return (int) fb.getDocument().getText(0, end).chars()
                        .filter(c -> c >= '0' && c <= '9').count();
            }

            private void apply(FilterBypass fb, String digits) throws BadLocationException {
                if (digits.length() > 11) digits = digits.substring(0, 11);
                fb.replace(0, fb.getDocument().getLength(), mascara(digits), null);
            }

            // (XX) XXXXX-XXXX
            private String mascara(String d) {
                if (d.isEmpty()) return "";
                StringBuilder sb = new StringBuilder("(");
                for (int i = 0; i < d.length(); i++) {
                    if (i == 2) sb.append(") ");
                    if (i == 7) sb.append("-");
                    sb.append(d.charAt(i));
                }
                return sb.toString();
            }
        });
    }

    // ── Ações dos botões e tabela ──────────────────────────────────────────────

    private void configurarAcoes() {
        btnNovo.addActionListener(e   -> limpar());
        btnLimpar.addActionListener(e -> limpar());

        btnSalvar.addActionListener(e -> {
            if (!validar()) return;
            try {
                dao.inserir(construir());
                carregarTabela();
                limpar();
                JOptionPane.showMessageDialog(this, "Cliente salvo com sucesso.");
            } catch (SQLException ex) {
                erro("salvar", ex);
            }
        });

        btnAlterar.addActionListener(e -> {
            if (txtCpf.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!validar()) return;
            try {
                dao.atualizar(construir());
                carregarTabela();
                limpar();
                JOptionPane.showMessageDialog(this, "Cliente alterado com sucesso.");
            } catch (SQLException ex) {
                erro("alterar", ex);
            }
        });

        btnExcluir.addActionListener(e -> {
            if (tabela.getSelectedRow() < 0) {
                JOptionPane.showMessageDialog(this, "Selecione um cliente na tabela.", "Atenção", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "Confirma exclusão do cliente?",
                    "Excluir", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    dao.excluir(modeloTabela.getValueAt(tabela.getSelectedRow(), 0).toString());
                    carregarTabela();
                    limpar();
                } catch (SQLException ex) {
                    String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";
                    if (msg.contains("foreign key") || msg.contains("constraint")) {
                        JOptionPane.showMessageDialog(this,
                            "Não é possível excluir este cliente pois ele possui aluguéis cadastrados no sistema.",
                            "Exclusão bloqueada", JOptionPane.WARNING_MESSAGE);
                    } else {
                        erro("excluir", ex);
                    }
                }
            }
        });

        tabela.getSelectionModel().addListSelectionListener(ev -> {
            if (ev.getValueIsAdjusting() || tabela.getSelectedRow() < 0) return;
            int r = tabela.getSelectedRow();
            txtCpf.setText(val(r, 0));
            txtNome.setText(val(r, 1));
            txtEndereco.setText(val(r, 2));
            cmbUf.setSelectedItem(val(r, 3));
            txtTelefone.setText(val(r, 4));
            txtEmail.setText(val(r, 5));
            txtCpf.setEditable(false);
            setEstadoEdicao();
        });
    }

    // ── Dados ──────────────────────────────────────────────────────────────────

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        try {
            for (Cliente c : dao.buscarTodos()) {
                modeloTabela.addRow(new Object[]{
                    c.getCpf(), c.getNomeCliente(), c.getEndereco(),
                    c.getUf(), c.getTelefone(), c.getEmail()
                });
            }
        } catch (SQLException ex) {
            erro("carregar", ex);
        }
    }

    private boolean validar() {
        if (txtCpf.getText().trim().isEmpty()) {
            aviso("CPF é obrigatório."); txtCpf.requestFocus(); return false;
        }
        if (txtNome.getText().trim().isEmpty()) {
            aviso("Nome é obrigatório."); txtNome.requestFocus(); return false;
        }
        // Garante que a UF digitada seja válida mesmo sem sair do campo antes
        String uf = ufEditor.getText().trim();
        if (!uf.isEmpty() && !UF_LIST.contains(uf)) {
            aviso("UF inválida. Selecione um estado válido.");
            cmbUf.requestFocus();
            return false;
        }
        String email = txtEmail.getText().trim();
        if (!email.isEmpty()) {
            int at = email.indexOf('@');
            if (at < 1 || !email.substring(at + 1).contains(".")) {
                aviso("E-mail inválido. Use o formato: exemplo@email.com");
                txtEmail.requestFocus();
                return false;
            }
        }
        return true;
    }

    private Cliente construir() {
        String uf = ufEditor.getText().trim();
        return new Cliente(
            txtCpf.getText().trim(),
            txtNome.getText().trim(),
            txtEndereco.getText().trim(),
            uf,
            txtTelefone.getText().trim(),
            txtEmail.getText().trim()
        );
    }

    private void limpar() {
        txtCpf.setText(""); txtNome.setText(""); txtEndereco.setText("");
        cmbUf.setSelectedIndex(0);
        txtTelefone.setText(""); txtEmail.setText("");
        txtCpf.setEditable(true);
        tabela.clearSelection();
        txtCpf.requestFocus();
        setEstadoNovo();
    }

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

    // ── helpers ────────────────────────────────────────────────────────────────
    private String val(int r, int c) {
        Object o = modeloTabela.getValueAt(r, c);
        return o == null ? "" : o.toString();
    }
    private void label(JPanel p, String t, GridBagConstraints g, int x, int y, double wx) {
        g.gridx = x; g.gridy = y; g.weightx = wx; p.add(new JLabel(t), g);
    }
    private void campo(JPanel p, JTextField f, GridBagConstraints g, int x, int y, double wx) {
        g.gridx = x; g.gridy = y; g.weightx = wx; p.add(f, g);
    }
    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validação", JOptionPane.WARNING_MESSAGE);
    }
    private void erro(String op, SQLException ex) {
        JOptionPane.showMessageDialog(this, "Erro ao " + op + ":\n" + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
