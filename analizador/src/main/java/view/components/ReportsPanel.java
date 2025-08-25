package view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Contenedor de reportes en pestañas. Vista pasiva: no calcula nada.
 *
 * Pestañas (orden fijo):
 *  0) Errores -> ErrorsTablePanel (siempre visible)
 *  1) Tokens  -> TokensTablePanel (solo si no hay errores)
 *  2) Recuento-> LexemeCountTablePanel (solo si no hay errores)
 *  3) General -> GeneralReportPanel (siempre visible)
 *
 * El Controller decide habilitar/deshabilitar pestañas y cargar datos.
 */
public class ReportsPanel extends javax.swing.JPanel {
    
    // Índices de pestaña para uso del Controller
    public static final int TAB_ERRORES  = 0;
    public static final int TAB_TOKENS   = 1;
    public static final int TAB_RECUENTO = 2;
    public static final int TAB_GENERAL  = 3;

    // Subvistas reales (se agregan dentro de los paneles placeholder generados por NetBeans)
    private ErrorsTablePanel errorsPanel;
    private TokensTablePanel tokensPanel;
    private LexemeCountTablePanel lexemeCountPanel;
    private GeneralReportPanel generalPanel;

    /**
     * Creates new form ReportsPanel
     */
    public ReportsPanel() {
        initComponents();
        postInitConfigure();
    }
    
    /**
     * Configuración posterior al initComponents(). No toca el código generado.
     * - Inserta las vistas reales en los paneles placeholder.
     * - Ajusta títulos/atajos/propiedades del JTabbedPane.
     * - Deja deshabilitadas Tokens y Recuento por defecto (hasta que no haya errores).
     */
    private void postInitConfigure() {
        // 1) Asegurar un layout adecuado en cada placeholder y montar las subvistas reales
        tabErrores.setLayout(new BorderLayout());
        tabTokens.setLayout(new BorderLayout());
        tabRecuento.setLayout(new BorderLayout());
        tabGeneral.setLayout(new BorderLayout());

        errorsPanel = new ErrorsTablePanel();
        tokensPanel = new TokensTablePanel();
        lexemeCountPanel = new LexemeCountTablePanel();
        generalPanel = new GeneralReportPanel();

        tabErrores.removeAll();
        tabErrores.add(errorsPanel, BorderLayout.CENTER);

        tabTokens.removeAll();
        tabTokens.add(tokensPanel, BorderLayout.CENTER);

        tabRecuento.removeAll();
        tabRecuento.add(lexemeCountPanel, BorderLayout.CENTER);

        tabGeneral.removeAll();
        tabGeneral.add(generalPanel, BorderLayout.CENTER);

        // 2) Propiedades del TabbedPane para mejor UX
        tabsReportes.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabsReportes.setMnemonicAt(TAB_ERRORES,  KeyEvent.VK_1); // Alt+1
        tabsReportes.setMnemonicAt(TAB_TOKENS,   KeyEvent.VK_2); // Alt+2
        tabsReportes.setMnemonicAt(TAB_RECUENTO, KeyEvent.VK_3); // Alt+3
        tabsReportes.setMnemonicAt(TAB_GENERAL,  KeyEvent.VK_4); // Alt+4

        // Normalizar capitalización del título de la pestaña "Tokens"
        tabsReportes.setTitleAt(TAB_TOKENS, "Tokens");

        // 3) Estado inicial recomendado por la práctica:
        //    - Errores y General habilitadas
        //    - Tokens y Recuento deshabilitadas hasta que #errores == 0
        tabsReportes.setEnabledAt(TAB_TOKENS, false);
        tabsReportes.setEnabledAt(TAB_RECUENTO, false);
        tabsReportes.setSelectedIndex(TAB_ERRORES);
    }

    /* ======================== API para el Controller ======================== */

    /** Devuelve el panel de Errores para que el Controller inyecte el TableModel, etc. */
    public ErrorsTablePanel getErrorsPanel() { return errorsPanel; }

    /** Devuelve el panel de Tokens. */
    public TokensTablePanel getTokensPanel() { return tokensPanel; }

    /** Devuelve el panel de Recuento de lexemas. */
    public LexemeCountTablePanel getLexemeCountPanel() { return lexemeCountPanel; }

    /** Devuelve el panel de Reporte general. */
    public GeneralReportPanel getGeneralPanel() { return generalPanel; }

    /** Devuelve el JTabbedPane por si el Controller necesita personalizar más. */
    public JTabbedPane getTabbedPane() { return tabsReportes; }

    /** Habilita/Deshabilita las pestañas de datos que dependen de NO tener errores. */
    public void setDataTabsEnabled(boolean enabled) {
        tabsReportes.setEnabledAt(TAB_TOKENS, enabled);
        tabsReportes.setEnabledAt(TAB_RECUENTO, enabled);
    }

    /** Selecciona la pestaña de Errores. */
    public void selectErrorsTab() { tabsReportes.setSelectedIndex(TAB_ERRORES); }

    /** Selecciona la pestaña de Tokens. */
    public void selectTokensTab() { tabsReportes.setSelectedIndex(TAB_TOKENS); }

    /** Selecciona la pestaña de Recuento. */
    public void selectRecuentoTab() { tabsReportes.setSelectedIndex(TAB_RECUENTO); }

    /** Selecciona la pestaña de General. */
    public void selectGeneralTab() { tabsReportes.setSelectedIndex(TAB_GENERAL); }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabsReportes = new javax.swing.JTabbedPane();
        tabErrores = new javax.swing.JPanel();
        tabTokens = new javax.swing.JPanel();
        tabRecuento = new javax.swing.JPanel();
        tabGeneral = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8));
        setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout tabErroresLayout = new javax.swing.GroupLayout(tabErrores);
        tabErrores.setLayout(tabErroresLayout);
        tabErroresLayout.setHorizontalGroup(
            tabErroresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 598, Short.MAX_VALUE)
        );
        tabErroresLayout.setVerticalGroup(
            tabErroresLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 385, Short.MAX_VALUE)
        );

        tabsReportes.addTab("Errores", null, tabErrores, "Tabla de errores léxicos");

        javax.swing.GroupLayout tabTokensLayout = new javax.swing.GroupLayout(tabTokens);
        tabTokens.setLayout(tabTokensLayout);
        tabTokensLayout.setHorizontalGroup(
            tabTokensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 598, Short.MAX_VALUE)
        );
        tabTokensLayout.setVerticalGroup(
            tabTokensLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 385, Short.MAX_VALUE)
        );

        tabsReportes.addTab("tokens", null, tabTokens, "Tokens reconocidos (solo si no hay errores)");

        javax.swing.GroupLayout tabRecuentoLayout = new javax.swing.GroupLayout(tabRecuento);
        tabRecuento.setLayout(tabRecuentoLayout);
        tabRecuentoLayout.setHorizontalGroup(
            tabRecuentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 598, Short.MAX_VALUE)
        );
        tabRecuentoLayout.setVerticalGroup(
            tabRecuentoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 385, Short.MAX_VALUE)
        );

        tabsReportes.addTab("Recuento", null, tabRecuento, "Conteo por lexema (solo si no hay errores)");
        tabRecuento.getAccessibleContext().setAccessibleName("");

        javax.swing.GroupLayout tabGeneralLayout = new javax.swing.GroupLayout(tabGeneral);
        tabGeneral.setLayout(tabGeneralLayout);
        tabGeneralLayout.setHorizontalGroup(
            tabGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 598, Short.MAX_VALUE)
        );
        tabGeneralLayout.setVerticalGroup(
            tabGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 385, Short.MAX_VALUE)
        );

        tabsReportes.addTab("General", null, tabGeneral, "Calificación y tokens no utilizados");

        add(tabsReportes, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel tabErrores;
    private javax.swing.JPanel tabGeneral;
    private javax.swing.JPanel tabRecuento;
    private javax.swing.JPanel tabTokens;
    private javax.swing.JTabbedPane tabsReportes;
    // End of variables declaration//GEN-END:variables
}
