package view.components;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.IntConsumer;

/**
 *
 * @author aaron
 */
public class LexemeCountTablePanel extends javax.swing.JPanel {
    
     /** Índices de columna esperados por el reporte de recuento. */
    public static final int COL_LEXEMA   = 0;
    public static final int COL_TIPO     = 1;
    public static final int COL_CANTIDAD = 2;

    /** Handler opcional para doble clic en una fila (índice en el *modelo*). */
    private IntConsumer onRowDoubleClick;

    /**
     * Creates new form LexemeCountTablePanel
     */
    public LexemeCountTablePanel() {
        initComponents();
         postInitConfigure();
    }
    
    /**
     * Método de configuración posterior al initComponents().
     * Aquí se ajustan propiedades de la tabla.
     */
    private void postInitConfigure() {
        // Propiedades generales de la JTable para buena UX
        tblLexemeCount.setFillsViewportHeight(true);
        tblLexemeCount.setAutoCreateRowSorter(true);                  // ordenar por columnas
        tblLexemeCount.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblLexemeCount.setRowSelectionAllowed(true);
        tblLexemeCount.setColumnSelectionAllowed(false);
        tblLexemeCount.getTableHeader().setReorderingAllowed(false);  // no permitir reordenar columnas
        tblLexemeCount.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tblLexemeCount.setRowHeight(22);

        // Tabla no editable (la edición no aplica al reporte)
        tblLexemeCount.setDefaultEditor(Object.class, null);

        // Ajuste de renderers para la columna numérica (centrada) y anchos
        configureColumnModelSafely();

        // Soporte para doble clic → notificar al Controller con índice del *modelo*
        tblLexemeCount.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && onRowDoubleClick != null) {
                    int viewRow = tblLexemeCount.getSelectedRow();
                    if (viewRow >= 0) {
                        int modelRow = tblLexemeCount.convertRowIndexToModel(viewRow);
                        onRowDoubleClick.accept(modelRow);
                    }
                }
            }
        });
    }

    /**
     * Reaplica renderers y anchos de columnas respetando el orden exigido por la práctica:
     *  [ "Lexema", "Tipo de Token", "Cantidad" ].
     * Llamar cada vez que se asigne un nuevo TableModel.
     */
    private void configureColumnModelSafely() {
        TableColumnModel cm = tblLexemeCount.getColumnModel();
        if (cm.getColumnCount() < 3) return; // evitar errores si aún no hay 3 columnas

        // Renderer para centrar números en "Cantidad"
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        // Columna 0: Lexema
        TableColumn c0 = cm.getColumn(COL_LEXEMA);
        c0.setMinWidth(180);
        c0.setPreferredWidth(420);

        // Columna 1: Tipo de Token
        TableColumn c1 = cm.getColumn(COL_TIPO);
        c1.setMinWidth(160);
        c1.setPreferredWidth(240);

        // Columna 2: Cantidad (numérica)
        TableColumn c2 = cm.getColumn(COL_CANTIDAD);
        c2.setMinWidth(80);
        c2.setPreferredWidth(100);
        c2.setCellRenderer(center);
    }

    /**
     * Permite al Controller asignar el modelo real (por ejemplo, LexemeCountTableModel) en runtime.
     * Tras asignarlo, se reconfiguran los anchos y renderers.
     */
    public void setTableModel(TableModel model) {
        tblLexemeCount.setModel(model);
        configureColumnModelSafely();
    }

    /** Devuelve la JTable interna por si el Controller quiere enlazar acciones. */
    public JTable getTable() { return tblLexemeCount; }

    /** Limpia la selección actual (útil después de recargar datos). */
    public void clearSelection() { tblLexemeCount.clearSelection(); }

    /**
     * Retorna el índice de fila seleccionada en el *modelo* (no en la vista).
     * Si no hay selección, retorna -1.
     */
    public int getSelectedModelRow() {
        int viewRow = tblLexemeCount.getSelectedRow();
        if (viewRow < 0) return -1;
        return tblLexemeCount.convertRowIndexToModel(viewRow);
    }

    /**
     * Desplaza el viewport hacia la fila indicada (índice de *modelo*) y la selecciona.
     */
    public void scrollToModelRow(int modelRow) {
        if (modelRow < 0) return;
        int viewRow = modelRow;
        RowSorter<? extends TableModel> sorter = tblLexemeCount.getRowSorter();
        if (sorter != null) {
            try {
                viewRow = tblLexemeCount.convertRowIndexToView(modelRow);
            } catch (IndexOutOfBoundsException ignored) {
                return; // modelo/vista desincronizados
            }
        }
        if (viewRow < 0 || viewRow >= tblLexemeCount.getRowCount()) return;
        Rectangle rect = tblLexemeCount.getCellRect(viewRow, COL_LEXEMA, true);
        tblLexemeCount.scrollRectToVisible(rect);
        tblLexemeCount.setRowSelectionInterval(viewRow, viewRow);
    }

    /**
     * Permite al Controller registrar una acción a ejecutar en doble clic sobre una fila.
     * El índice entregado corresponde al *modelo* (ya convertido).
     */
    public void setOnRowDoubleClick(IntConsumer handler) {
        this.onRowDoubleClick = handler;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblLexemeCount = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Recuento de lexemas", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 3, 18))); // NOI18N
        setLayout(new java.awt.BorderLayout());

        tblLexemeCount.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Lexema", "Tipo de Token", "Cantidad"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblLexemeCount);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblLexemeCount;
    // End of variables declaration//GEN-END:variables
}
