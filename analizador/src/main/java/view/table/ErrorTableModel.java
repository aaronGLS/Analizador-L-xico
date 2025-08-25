package view.table;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import model.report.ErrorRow;

/**
 * TableModel para mostrar los errores léxicos.
 * Columnas: Símbolo/Cadena, Fila, Columna.
 */
public class ErrorTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private List<ErrorRow> rows = List.of();

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> "Símbolo/Cadena";
            case 1 -> "Fila";
            case 2 -> "Columna";
            default -> "";
        };
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> String.class;
            case 1, 2 -> Integer.class;
            default -> Object.class;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        ErrorRow row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.simboloOCadena();
            case 1 -> row.posicion().linea();
            case 2 -> row.posicion().columna();
            default -> null;
        };
    }

    public void setRows(List<ErrorRow> rows) {
        this.rows = List.copyOf(rows);
        fireTableDataChanged();
    }

    public ErrorRow getRow(int index) {
        return rows.get(index);
    }

    public void clear() {
        if (!rows.isEmpty()) {
            rows = List.of();
            fireTableDataChanged();
        }
    }
}
