package view.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.table.AbstractTableModel;

import model.report.LexemeCountRow;

/**
 * TableModel de solo lectura para el reporte de recuento de lexemas.
 * Columnas: "Lexema", "Tipo de Token", "Cantidad".
 */
public class LexemeCountTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
            "Lexema",
            "Tipo de Token",
            "Cantidad"
    };

    private List<LexemeCountRow> rows = new ArrayList<>();

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 1 -> String.class;
            case 2 -> Integer.class;
            default -> Object.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LexemeCountRow row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.lexema();
            case 1 -> row.tipo().toString();
            case 2 -> row.cantidad();
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /** Reemplaza todas las filas y notifica a la tabla. */
    public void setRows(List<LexemeCountRow> rows) {
        this.rows = new ArrayList<>(Objects.requireNonNull(rows, "rows"));
        fireTableDataChanged();
    }

    /** Obtiene la fila indicada. */
    public LexemeCountRow getRow(int rowIndex) {
        return rows.get(rowIndex);
    }

    /** Limpia todas las filas. */
    public void clear() {
        rows.clear();
        fireTableDataChanged();
    }
}
