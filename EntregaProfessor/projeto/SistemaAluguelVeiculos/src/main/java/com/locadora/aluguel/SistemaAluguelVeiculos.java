package com.locadora.aluguel;

import com.locadora.aluguel.view.TelaLogin;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class SistemaAluguelVeiculos {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // mantém o look padrão
        }

        SwingUtilities.invokeLater(() -> new TelaLogin().setVisible(true));
    }
}
