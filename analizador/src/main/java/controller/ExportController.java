package controller;

import core.export.CsvWriter;
import core.export.ExportCoordinator;
import view.MainWindow;
import view.components.EditorPanel;
import view.components.ErrorsTablePanel;
import view.components.LexemeCountTablePanel;
import view.components.TokensTablePanel;
import view.table.ErrorTableModel;
import view.table.LexemeCountTableModel;
import view.table.TokenTableModel;
import model.report.ErrorRow;
import model.report.TokenRow;
import model.report.LexemeCountRow;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controlador encargado de exportar los reportes exigidos por la práctica.
 * <p>
 * Reglas (según el enunciado de la práctica):
 * <ul>
 * <li>Si el último análisis tiene <b>errores</b>, se exporta <b>solo</b> el
 * reporte de errores.</li>
 * <li>Si el último análisis está <b>limpio</b>, se exportan <b>Tokens</b> y
 * <b>Recuento de lexemas</b>.</li>
 * <li>El texto de entrada se guarda desde el flujo de “Guardar”, pero se expone
 * un método auxiliar
 * para quien desee usarlo.</li>
 * </ul>
 * <p>
 * Implementación fiel al esquema: usa {@link CsvWriter} para tokens y errores a
 * partir de los TableModel
 * de las vistas (porque las tablas exponen filas de tipo {@code TokenRow} y
 * {@code ErrorRow}), y usa
 * {@link ExportCoordinator} para el recuento (recibe
 * {@code List<LexemeCountRow>}) y el guardado del
 * texto de entrada.
 */
public class ExportController {

    private final MainWindow mainWindow;
    private final EditorPanel editorPanel;
    private final ErrorsTablePanel errorsPanel;
    private final TokensTablePanel tokensPanel;
    private final LexemeCountTablePanel countPanel;

    // Utilidades de exportación disponibles en el proyecto
    private final CsvWriter csvWriter = new CsvWriter();
    private final ExportCoordinator coordinator = new ExportCoordinator();

    public ExportController(MainWindow mainWindow,
            EditorPanel editorPanel,
            ErrorsTablePanel errorsPanel,
            TokensTablePanel tokensPanel,
            LexemeCountTablePanel countPanel) {
        this.mainWindow = Objects.requireNonNull(mainWindow, "mainWindow");
        this.editorPanel = Objects.requireNonNull(editorPanel, "editorPanel");
        this.errorsPanel = Objects.requireNonNull(errorsPanel, "errorsPanel");
        this.tokensPanel = Objects.requireNonNull(tokensPanel, "tokensPanel");
        this.countPanel = Objects.requireNonNull(countPanel, "countPanel");
    }

    /**
     * Abre un diálogo para elegir carpeta y exporta los reportes correspondientes
     * al estado
     * actual (con errores o limpio). Usa nombres de archivo fijos: <br>
     * - <code>errores.csv</code> (si hay errores) <br>
     * - <code>tokens.csv</code> y <code>recuento.csv</code> (si no hay errores)
     */
    public void exportReportsInteractive() {
        Path dir = chooseDirectory(mainWindow);
        if (dir == null)
            return; // cancelado

        try {
            List<Path> out = exportReportsToDirectory(dir);
            if (out.isEmpty()) {
                JOptionPane.showMessageDialog(mainWindow,
                        "No hay datos para exportar. Ejecute un análisis primero.",
                        "Exportar reportes",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            StringBuilder sb = new StringBuilder("Exportación exitosa:\n");
            for (Path p : out)
                sb.append("• ").append(p.toAbsolutePath()).append('\n');
            JOptionPane.showMessageDialog(mainWindow, sb.toString(),
                    "Exportar reportes", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showIoError(ex);
        }
    }

    /**
     * Exporta a la carpeta indicada siguiendo las reglas de la práctica. Devuelve
     * la lista
     * de archivos generados. Si no hay ningún dato en tablas, devuelve lista vacía.
     */
    public List<Path> exportReportsToDirectory(Path directory) throws IOException {
        Objects.requireNonNull(directory, "directory");
        ensureDirectory(directory);

        List<Path> generated = new ArrayList<>();

        boolean hasErrors = hasErrors();
        if (hasErrors) {
            // Exportar solo errores
            Path errorsPath = directory.resolve("errores.csv");
            exportErrorsCsv(errorsPath);
            generated.add(errorsPath);
        } else {
            // Exportar tokens y recuento (si hay datos)
            Path tokensPath = directory.resolve("tokens.csv");
            Path countPath = directory.resolve("recuento.csv");

            boolean any = false;
            if (getTokenModel().getRowCount() > 0) {
                exportTokensCsv(tokensPath);
                generated.add(tokensPath);
                any = true;
            }
            if (getCountModel().getRowCount() > 0) {
                exportLexemeCountCsv(countPath);
                generated.add(countPath);
                any = true;
            }
            if (!any) {
                // Ningún dato: devolver vacío
                return List.of();
            }
        }
        return generated;
    }

    /**
     * Exporta el <b>texto de entrada</b> (útil si querés habilitar una acción
     * “Exportar texto”).
     */
    public void exportInputTextInteractive() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar texto de entrada");
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setSelectedFile(new java.io.File("entrada.txt"));
        if (fc.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
            try {
                coordinator.exportInputText(fc.getSelectedFile().toPath(), editorPanel.getEditorText());
                JOptionPane.showMessageDialog(mainWindow, "Texto guardado correctamente.",
                        "Exportar texto", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                showIoError(ex);
            }
        }
    }

    /*
     * =============================================================================
     * ========
     * Exportaciones concretas (CSV)
     * =============================================================================
     * ========
     */

    /** Exporta errores.csv leyendo directamente el {@link ErrorTableModel}. */
    public void exportErrorsCsv(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        ErrorTableModel model = getErrorModel();
        int n = model.getRowCount();
        if (n <= 0)
            return;

        String[] headers = { "SimboloOCadena", "Linea", "Columna", "Mensaje" };
        String[][] rows = new String[n][headers.length];
        for (int i = 0; i < n; i++) {
            ErrorRow r = model.getRow(i);
            rows[i][0] = nullToEmpty(r.simboloOCadena());
            rows[i][1] = String.valueOf(r.posicion().linea());
            rows[i][2] = String.valueOf(r.posicion().columna());
            rows[i][3] = nullToEmpty(r.mensaje());
        }
        csvWriter.write(file, headers, rows);
    }

    /** Exporta tokens.csv leyendo directamente el {@link TokenTableModel}. */
    public void exportTokensCsv(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        TokenTableModel model = getTokenModel();
        int n = model.getRowCount();
        if (n <= 0)
            return;

        String[] headers = { "NombreToken", "Lexema", "Linea", "Columna" };
        String[][] rows = new String[n][headers.length];
        for (int i = 0; i < n; i++) {
            TokenRow r = model.getRow(i);
            rows[i][0] = r.nombreToken().toString();
            rows[i][1] = nullToEmpty(r.lexema());
            rows[i][2] = String.valueOf(r.posicion().linea());
            rows[i][3] = String.valueOf(r.posicion().columna());
        }
        csvWriter.write(file, headers, rows);
    }

    /** Exporta recuento.csv apoyándose en {@link ExportCoordinator}. */
    public void exportLexemeCountCsv(Path file) throws IOException {
        Objects.requireNonNull(file, "file");
        LexemeCountTableModel model = getCountModel();
        int n = model.getRowCount();
        if (n <= 0)
            return;

        List<LexemeCountRow> rows = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            rows.add(model.getRow(i));
        }
        coordinator.exportLexemeCount(file, rows);
    }

    private boolean hasErrors() {
        return getErrorModel().getRowCount() > 0;
    }

    private ErrorTableModel getErrorModel() {
        TableModel tm = errorsPanel.getTable().getModel();
        if (!(tm instanceof ErrorTableModel etm)) {
            throw new IllegalStateException("La tabla de errores no usa ErrorTableModel");
        }
        return etm;
    }

    private TokenTableModel getTokenModel() {
        TableModel tm = tokensPanel.getTable().getModel();
        if (!(tm instanceof TokenTableModel ttm)) {
            throw new IllegalStateException("La tabla de tokens no usa TokenTableModel");
        }
        return ttm;
    }

    private LexemeCountTableModel getCountModel() {
        TableModel tm = countPanel.getTable().getModel();
        if (!(tm instanceof LexemeCountTableModel lcm)) {
            throw new IllegalStateException("La tabla de recuento no usa LexemeCountTableModel");
        }
        return lcm;
    }

    private static Path chooseDirectory(Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar reportes a carpeta");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fc.showSaveDialog(parent);
        if (option == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile().toPath();
        }
        return null;
    }

    private static void ensureDirectory(Path dir) throws IOException {
        if (Files.exists(dir)) {
            if (!Files.isDirectory(dir)) {
                throw new IOException("La ruta seleccionada no es una carpeta: " + dir);
            }
        } else {
            Files.createDirectories(dir);
        }
    }

    private static void showIoError(IOException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null,
                "Error de E/S: " + ex.getMessage(),
                "Exportación", JOptionPane.ERROR_MESSAGE);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
