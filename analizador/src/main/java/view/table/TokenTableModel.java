package view.table;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

import model.report.TokenRow;

/**
 * TableModel para mostrar la lista de tokens reconocidos.
 * Las columnas siguen el orden:
 *  [ "Nombre del Token", "Lexema", "Fila", "Columna" ].
 */
public class TokenTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Nombre del Token", "Lexema", "Fila", "Columna"};
    private final Class<?>[] columnClasses = {String.class, String.class, Integer.class, Integer.class};
    private final List<TokenRow> rows = new ArrayList<>();

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnClasses[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TokenRow row = rows.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> row.nombreToken().toString();
            case 1 -> row.lexema();
            case 2 -> row.posicion().linea();
            case 3 -> row.posicion().columna();
            default -> null;
        };
    }

    /** Reemplaza las filas del modelo y notifica a la vista. */
    public void setRows(List<TokenRow> newRows) {
        rows.clear();
        if (newRows != null) {
            rows.addAll(newRows);
        }
        fireTableDataChanged();
    }

    /** Obtiene la fila TokenRow en el índice dado. */
    public TokenRow getRow(int index) {
        return rows.get(index);
    }

    /** Elimina todas las filas del modelo y notifica a la vista. */
    public void clear() {
        if (!rows.isEmpty()) {
            rows.clear();
            fireTableDataChanged();
        }
    }
}

