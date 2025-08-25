package view.components;

import java.awt.event.MouseAdapter;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author aaron
 */
public class ErrorsTablePanel extends javax.swing.JPanel {

    /**
     * Índices de columna esperados por el reporte de errores.
     */
    public static final int COL_SIMBOLO = 0;
    public static final int COL_FILA = 1;
    public static final int COL_COLUMNA = 2;

    private IntConsumer onRowDoubleClick;

    /**
     * Creates new form ErrorsTablePanel
     */
    public ErrorsTablePanel() {
        initComponents();
        postInitConfigure();
    }

    /**
     * Método de configuración posterior al initComponents(). Aquí se ajustan
     * propiedades de la tabla SIN tocar el código generado.
     */
    private void postInitConfigure() {
        setBorder(BorderFactory.createTitledBorder("Errores léxicos"));

        tblErrores.setFillsViewportHeight(true);
        tblErrores.setAutoCreateRowSorter(true);                   // ordenar por columnas
        tblErrores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblErrores.setRowSelectionAllowed(true);
        tblErrores.getTableHeader().setReorderingAllowed(false);   // no permitir reordenar columnas
        tblErrores.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tblErrores.setRowHeight(22);

        // Tabla no editable (la edición no aplica al reporte)
        tblErrores.setDefaultEditor(Object.class, null);

        // Ajuste de renderers para columnas numéricas (centradas) y anchos
        configureColumnModelSafely();

        // Soporte para doble clic → notificar al Controller con índice del *modelo*
        tblErrores.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && onRowDoubleClick != null) {
                    int viewRow = tblErrores.getSelectedRow();
                    if (viewRow >= 0) {
                        int modelRow = tblErrores.convertRowIndexToModel(viewRow);
                        onRowDoubleClick.accept(modelRow);
                    }
                }
            }
        });
    }

    /**
     * Reaplica renderers y anchos de columnas respetando el orden exigido por
     * la práctica: [ "Símbolo/Cadena", "Fila", "Columna" ]. Llamar cada vez que
     * se asigne un nuevo TableModel.
     */
    private void configureColumnModelSafely() {
        TableColumnModel cm = tblErrores.getColumnModel();
        if (cm.getColumnCount() < 3) {
            return; // evitar errores si aún no hay 3 columnas
        }
        // Renderers para alinear números
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        // Columna 0: Símbolo/Cadena
        TableColumn c0 = cm.getColumn(COL_SIMBOLO);
        c0.setMinWidth(150);
        c0.setPreferredWidth(380);

        // Columna 1: Fila (numérica)
        TableColumn c1 = cm.getColumn(COL_FILA);
        c1.setMinWidth(60);
        c1.setPreferredWidth(80);
        c1.setCellRenderer(center);

        // Columna 2: Columna (numérica)
        TableColumn c2 = cm.getColumn(COL_COLUMNA);
        c2.setMinWidth(70);
        c2.setPreferredWidth(90);
        c2.setCellRenderer(center);
    }

    /**
     * Permite al Controller asignar el modelo real (por ejemplo,
     * ErrorTableModel) en runtime. Tras asignarlo, se reconfiguran los anchos y
     * renderers.
     */
    public void setTableModel(TableModel model) {
        tblErrores.setModel(model);
        configureColumnModelSafely();
    }

    /**
     * Devuelve la JTable interna por si el Controller quiere enlazar acciones.
     */
    public JTable getTable() {
        return tblErrores;
    }

    /**
     * Limpia la selección actual (útil después de recargar datos).
     */
    public void clearSelection() {
        tblErrores.clearSelection();
    }

    /**
     * Retorna el índice de fila seleccionada en el *modelo* (no en la vista).
     * Si no hay selección, retorna -1.
     */
    public int getSelectedModelRow() {
        int viewRow = tblErrores.getSelectedRow();
        if (viewRow < 0) {
            return -1;
        }
        return tblErrores.convertRowIndexToModel(viewRow);
    }

    /**
     * Desplaza el viewport hacia la fila indicada (índice de *modelo*) y la
     * selecciona.
     */
    public void scrollToModelRow(int modelRow) {
        if (modelRow < 0) {
            return;
        }
        int viewRow = modelRow;
        RowSorter<? extends TableModel> sorter = tblErrores.getRowSorter();
        if (sorter != null) {
            try {
                viewRow = tblErrores.convertRowIndexToView(modelRow);
            } catch (IndexOutOfBoundsException ignored) {
                return; // modelo/vista desincronizados
            }
        }
        if (viewRow < 0 || viewRow >= tblErrores.getRowCount()) {
            return;
        }
        Rectangle rect = tblErrores.getCellRect(viewRow, COL_SIMBOLO, true);
        tblErrores.scrollRectToVisible(rect);
        tblErrores.setRowSelectionInterval(viewRow, viewRow);
    }

    /**
     * Permite al Controller registrar una acción a ejecutar en doble clic sobre
     * una fila. El índice entregado corresponde al *modelo* (ya convertido).
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
        tblErrores = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        tblErrores.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Símbolo/Cadena", "Fila", "Columna"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblErrores);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblErrores;
    // End of variables declaration//GEN-END:variables
}
