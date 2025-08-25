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
public class TokensTablePanel extends javax.swing.JPanel {
    
     /** Índices de columna esperados por el reporte de tokens. */
    public static final int COL_NOMBRE_TOKEN = 0;
    public static final int COL_LEXEMA       = 1;
    public static final int COL_FILA         = 2;
    public static final int COL_COLUMNA      = 3;

    private IntConsumer onRowDoubleClick;

    /**
     * Creates new form TokensTablePanel
     */
    public TokensTablePanel() {
        initComponents();
        postInitConfigure();
    }
    
    /**
     * Método de configuración posterior al initComponents().
     * Aquí se ajustan propiedades de la tabla.
     */
    private void postInitConfigure() {
        // Título del panel para acoplarse al lenguaje de la práctica
        setBorder(BorderFactory.createTitledBorder("Tokens"));

        // Propiedades generales de la JTable para buena UX
        tblTokens.setFillsViewportHeight(true);
        tblTokens.setAutoCreateRowSorter(true);                     // ordenar por columnas
        tblTokens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTokens.setRowSelectionAllowed(true);
        tblTokens.getTableHeader().setReorderingAllowed(false);     // no permitir reordenar columnas
        tblTokens.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        tblTokens.setRowHeight(22);

        // Tabla no editable (la edición no aplica al reporte)
        tblTokens.setDefaultEditor(Object.class, null);

        // Ajuste de renderers para columnas numéricas (centradas) y anchos
        configureColumnModelSafely();

        // Soporte para doble clic → notificar al Controller con índice del *modelo*
        tblTokens.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && onRowDoubleClick != null) {
                    int viewRow = tblTokens.getSelectedRow();
                    if (viewRow >= 0) {
                        int modelRow = tblTokens.convertRowIndexToModel(viewRow);
                        onRowDoubleClick.accept(modelRow);
                    }
                }
            }
        });
    }

    /**
     * Reaplica renderers y anchos de columnas respetando el orden exigido por la práctica:
     *  [ "Nombre del Token", "Lexema", "Fila", "Columna" ].
     * Llamar cada vez que se asigne un nuevo TableModel.
     */
    private void configureColumnModelSafely() {
        TableColumnModel cm = tblTokens.getColumnModel();
        if (cm.getColumnCount() < 4) return; // evitar errores si aún no hay 4 columnas

        // Renderers para alinear números
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);

        // Columna 0: Nombre del Token
        TableColumn c0 = cm.getColumn(COL_NOMBRE_TOKEN);
        c0.setMinWidth(120);
        c0.setPreferredWidth(180);

        // Columna 1: Lexema
        TableColumn c1 = cm.getColumn(COL_LEXEMA);
        c1.setMinWidth(150);
        c1.setPreferredWidth(400);

        // Columna 2: Fila (numérica)
        TableColumn c2 = cm.getColumn(COL_FILA);
        c2.setMinWidth(60);
        c2.setPreferredWidth(80);
        c2.setCellRenderer(center);

        // Columna 3: Columna (numérica)
        TableColumn c3 = cm.getColumn(COL_COLUMNA);
        c3.setMinWidth(70);
        c3.setPreferredWidth(90);
        c3.setCellRenderer(center);
    }

    /**
     * Permite al Controller asignar el modelo real (por ejemplo, TokenTableModel) en runtime.
     * Tras asignarlo, se reconfiguran los anchos y renderers.
     */
    public void setTableModel(TableModel model) {
        tblTokens.setModel(model);
        configureColumnModelSafely();
    }

    /** Devuelve la JTable interna por si el Controller quiere enlazar acciones. */
    public JTable getTable() { return tblTokens; }

    /** Limpia la selección actual (útil después de recargar datos). */
    public void clearSelection() { tblTokens.clearSelection(); }

    /**
     * Retorna el índice de fila seleccionada en el *modelo* (no en la vista).
     * Si no hay selección, retorna -1.
     */
    public int getSelectedModelRow() {
        int viewRow = tblTokens.getSelectedRow();
        if (viewRow < 0) return -1;
        return tblTokens.convertRowIndexToModel(viewRow);
    }

    /**
     * Desplaza el viewport hacia la fila indicada (índice de *modelo*) y la selecciona.
     */
    public void scrollToModelRow(int modelRow) {
        if (modelRow < 0) return;
        int viewRow = modelRow;
        RowSorter<? extends TableModel> sorter = tblTokens.getRowSorter();
        if (sorter != null) {
            try {
                viewRow = tblTokens.convertRowIndexToView(modelRow);
            } catch (IndexOutOfBoundsException ignored) {
                return; // modelo/vista desincronizados
            }
        }
        if (viewRow < 0 || viewRow >= tblTokens.getRowCount()) return;
        Rectangle rect = tblTokens.getCellRect(viewRow, COL_NOMBRE_TOKEN, true);
        tblTokens.scrollRectToVisible(rect);
        tblTokens.setRowSelectionInterval(viewRow, viewRow);
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
        tblTokens = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        tblTokens.setBorder(javax.swing.BorderFactory.createTitledBorder("Tokens reconocidos"));
        tblTokens.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Nombre del Token", "Lexema", "Fila", "Columna"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblTokens);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblTokens;
    // End of variables declaration//GEN-END:variables
}
