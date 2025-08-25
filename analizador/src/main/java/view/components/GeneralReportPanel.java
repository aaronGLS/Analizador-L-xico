package view.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;

/**
 * Panel pasivo para el "Reporte general" del análisis léxico.
 *
 * Muestra: - Cantidad de errores - % de tokens válidos - Tokens no utilizados
 * (lista)
 *
 * No realiza cálculos ni I/O; la lógica vive en Controller/Services.
 */
public class GeneralReportPanel extends javax.swing.JPanel {

    /**
     * Handler opcional para doble clic en la lista de "no utilizados".
     */
    private IntConsumer onNotUsedDoubleClick;

    /**
     * Formateador de porcentaje (dos decimales, estilo 97.50 %).
     */
    private final NumberFormat percentFormat;

    /**
     * Creates new form GeneralReportPanel
     */
    public GeneralReportPanel() {
        // Configurar formateador de porcentaje antes por si se usa de inmediato
        percentFormat = NumberFormat.getNumberInstance(new Locale("es", "GT"));
        percentFormat.setMinimumFractionDigits(2);
        percentFormat.setMaximumFractionDigits(2);
        initComponents();
        postInitConfigure();
    }

    /**
     * Ajustes posteriores al initComponents().
     */
    private void postInitConfigure() {
        // Alineación y tipografía de los encabezados/valores (si el diseñador no lo dejó así)
        lblErroresTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblValidPctTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblUnusedTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblErroresValue.setHorizontalAlignment(SwingConstants.CENTER);
        lblValidPctValue.setHorizontalAlignment(SwingConstants.CENTER);
        lblUnusedCountValue.setHorizontalAlignment(SwingConstants.CENTER);

        // Lista: selección simple, sin edición, con scroll ya provisto por jScrollPane1
        jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList1.setVisibleRowCount(-1); // que la altura la controle el scroll
        jList1.setFixedCellHeight(22); // coherente con altura de filas en tablas
        jList1.setPrototypeCellValue("XXXXXXXXXXXXXX"); // ayuda a calcular ancho uniforme

        // Doble clic en la lista → notificar al Controller con el índice seleccionado
        jList1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && onNotUsedDoubleClick != null) {
                    int idx = jList1.getSelectedIndex();
                    if (idx >= 0) {
                        onNotUsedDoubleClick.accept(idx);
                    }
                }
            }
        });
    }

    /* ========================= API para Controller (vista pasiva) ========================= */
    /**
     * Asigna el número total de errores.
     */
    public void setErrorsCount(int n) {
        lblErroresValue.setText(String.valueOf(n));
    }

    /**
     * Asigna el porcentaje de tokens válidos.
     *
     * @param percentValue valor en rango 0–100 (p. ej., 97.5) -> "97.50 %"
     */
    public void setValidPercent(double percentValue) {
        lblValidPctValue.setText(percentFormat.format(percentValue) + " %");
    }

    /**
     * Asigna la cantidad de tokens no utilizados.
     */
    public void setNotUsedCount(int n) {
        lblUnusedCountValue.setText(String.valueOf(n));
    }

    /**
     * Carga la lista de tokens no utilizados. El Controller puede formatear
     * cada ítem (p. ej., "[OPERADOR] +").
     */
    public void setNotUsedItems(List<String> items) {
        DefaultListModel<String> model = new DefaultListModel<>();
        if (items != null) {
            for (String s : items) {
                model.addElement(s);
            }
        }
        jList1.setModel(model);
        if (model.size() > 0) {
            jList1.setSelectedIndex(0);
        }
    }

    /**
     * Limpia la selección de la lista.
     */
    public void clearNotUsedSelection() {
        jList1.clearSelection();
    }

    /**
     * Devuelve el índice seleccionado en la lista o -1 si no hay selección.
     */
    public int getSelectedNotUsedIndex() {
        return jList1.getSelectedIndex();
    }

    /**
     * Devuelve el valor seleccionado en la lista o null si no hay selección.
     */
    public String getSelectedNotUsedValue() {
        return jList1.getSelectedValue();
    }

    /**
     * Desplaza el viewport para hacer visible el elemento indicado y lo
     * selecciona.
     */
    public void scrollToNotUsedIndex(int index) {
        if (index < 0) {
            return;
        }
        int size = jList1.getModel().getSize();
        if (index >= size) {
            return;
        }
        jList1.ensureIndexIsVisible(index);
        jList1.setSelectedIndex(index);
    }

    /**
     * Registra handler para doble clic sobre un elemento de la lista.
     */
    public void setOnNotUsedDoubleClick(IntConsumer handler) {
        this.onNotUsedDoubleClick = handler;
    }

    /**
     * Permite al Controller acceder a la JList, si necesitara enlazar acciones
     * extra.
     */
    public JList<String> getNotUsedList() {
        return jList1;
    }

    /**
     * Permite cambiar el título del panel central si el Controller lo desea.
     */
    public void setNotUsedListTitle(String title) {
        // El borde del JList es un TitledBorder según el diseñador
        if (jList1.getBorder() instanceof javax.swing.border.TitledBorder tb) {
            tb.setTitle(title);
            jList1.repaint();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelSuperior = new javax.swing.JPanel();
        panelErrores = new javax.swing.JPanel();
        lblErroresTitle = new javax.swing.JLabel();
        lblErroresValue = new javax.swing.JLabel();
        panelPorcentajeValido = new javax.swing.JPanel();
        lblValidPctTitle = new javax.swing.JLabel();
        lblValidPctValue = new javax.swing.JLabel();
        panelTokensNoUtilizados = new javax.swing.JPanel();
        lblUnusedTitle = new javax.swing.JLabel();
        lblUnusedCountValue = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Reporte general", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 3, 18))); // NOI18N
        setLayout(new java.awt.BorderLayout());

        panelSuperior.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8));
        panelSuperior.setLayout(new java.awt.GridLayout(1, 3, 8, 4));

        panelErrores.setLayout(new java.awt.GridBagLayout());

        lblErroresTitle.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        lblErroresTitle.setText("Errores:");
        panelErrores.add(lblErroresTitle, new java.awt.GridBagConstraints());

        lblErroresValue.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblErroresValue.setText("-");
        panelErrores.add(lblErroresValue, new java.awt.GridBagConstraints());

        panelSuperior.add(panelErrores);

        panelPorcentajeValido.setLayout(new java.awt.GridBagLayout());

        lblValidPctTitle.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        lblValidPctTitle.setText("% válidos:");
        panelPorcentajeValido.add(lblValidPctTitle, new java.awt.GridBagConstraints());

        lblValidPctValue.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblValidPctValue.setText("-");
        panelPorcentajeValido.add(lblValidPctValue, new java.awt.GridBagConstraints());

        panelSuperior.add(panelPorcentajeValido);

        panelTokensNoUtilizados.setLayout(new java.awt.GridBagLayout());

        lblUnusedTitle.setFont(new java.awt.Font("Segoe UI", 3, 18)); // NOI18N
        lblUnusedTitle.setText("Tokens no utilizados:");
        panelTokensNoUtilizados.add(lblUnusedTitle, new java.awt.GridBagConstraints());

        lblUnusedCountValue.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblUnusedCountValue.setText("-");
        panelTokensNoUtilizados.add(lblUnusedCountValue, new java.awt.GridBagConstraints());

        panelSuperior.add(panelTokensNoUtilizados);

        add(panelSuperior, java.awt.BorderLayout.PAGE_START);

        jList1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Detalle de tokens no utilizados", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 18))); // NOI18N
        jList1.setModel(new javax.swing.AbstractListModel<String>() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public String getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(jList1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblErroresTitle;
    private javax.swing.JLabel lblErroresValue;
    private javax.swing.JLabel lblUnusedCountValue;
    private javax.swing.JLabel lblUnusedTitle;
    private javax.swing.JLabel lblValidPctTitle;
    private javax.swing.JLabel lblValidPctValue;
    private javax.swing.JPanel panelErrores;
    private javax.swing.JPanel panelPorcentajeValido;
    private javax.swing.JPanel panelSuperior;
    private javax.swing.JPanel panelTokensNoUtilizados;
    // End of variables declaration//GEN-END:variables
}
