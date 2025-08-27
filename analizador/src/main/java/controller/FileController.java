package controller;

import model.document.DocumentModel;
import view.MainWindow;
import view.components.EditorPanel;
import core.io.TextLoader;
import core.io.TextSaver;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Controlador responsable de abrir y guardar el <b>texto de entrada</b>,
 * sincronizando siempre el {@link DocumentModel} con el {@link EditorPanel}.
 * <p>
 * Reglas (según la práctica y el esquema):
 * <ul>
 * <li>Permitir <b>cargar</b> un archivo de entrada y <b>editarlo</b> en el
 * componente de texto.</li>
 * <li>Permitir <b>guardar</b> el contenido actual del editor en disco.</li>
 * <li>Mantener el estado de <i>dirty</i> (modificado) en el
 * {@code DocumentModel}.</li>
 * <li>No realiza lógica de análisis ni de resaltado; solo I/O + sincronización
 * Modelo↔Vista.</li>
 * </ul>
 */
public class FileController {

    private final MainWindow mainWindow;
    private final EditorPanel editorPanel;
    private final DocumentModel documentModel;

    private final TextLoader textLoader;
    private final TextSaver textSaver;

    public FileController(MainWindow mainWindow,
            EditorPanel editorPanel,
            DocumentModel documentModel,
            TextLoader textLoader,
            TextSaver textSaver) {
        this.mainWindow = Objects.requireNonNull(mainWindow, "mainWindow");
        this.editorPanel = Objects.requireNonNull(editorPanel, "editorPanel");
        this.documentModel = Objects.requireNonNull(documentModel, "documentModel");
        this.textLoader = Objects.requireNonNull(textLoader, "textLoader");
        this.textSaver = Objects.requireNonNull(textSaver, "textSaver");
    }

    /**
     * Abre un diálogo de selección y <b>carga</b> el archivo de texto seleccionado
     * en el editor, actualizando el {@link DocumentModel}.
     */
    public void openInteractive() {
        Path path = chooseOpenFile(mainWindow);
        if (path == null)
            return; // usuario canceló
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return loadFromPath(path);
            }

            @Override
            protected void done() {
                try {
                    String text = get();
                    SwingUtilities.invokeLater(() -> {
                        editorPanel.setEditorText(text);
                        documentModel.setText(text);
                        documentModel.setFilePath(path);
                        documentModel.setDirty(false);
                        updateWindowTitle();
                        info("Archivo abierto", path.toAbsolutePath().toString());
                    });
                } catch (Exception e) {
                    Throwable cause = e.getCause() == null ? e : e.getCause();
                    IOException io = cause instanceof IOException ? (IOException) cause
                            : new IOException(cause);
                    SwingUtilities.invokeLater(() -> showIoError(io, "Error al abrir el archivo"));
                }
            }
        };
        worker.execute();
    }

    /**
     * Guarda el contenido del editor. Si el documento no tiene ruta aún,
     * abre un diálogo "Guardar como".
     */
    public void saveInteractive() {
        Path current = documentModel.getFilePath();
        if (current == null) {
            saveAsInteractive();
        } else {
            final String text = editorPanel.getEditorText();
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    saveToPath(current, text);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        SwingUtilities.invokeLater(() -> {
                            documentModel.setText(text);
                            documentModel.setFilePath(current);
                            documentModel.setDirty(false);
                            updateWindowTitle();
                            info("Archivo guardado", current.toAbsolutePath().toString());
                        });
                    } catch (Exception e) {
                        Throwable cause = e.getCause() == null ? e : e.getCause();
                        IOException io = cause instanceof IOException ? (IOException) cause
                                : new IOException(cause);
                        SwingUtilities.invokeLater(() -> showIoError(io, "Error al guardar el archivo"));
                    }
                }
            };
            worker.execute();
        }
    }

    /**
     * Abre un diálogo "Guardar como" para elegir la ruta y luego guarda allí
     * el contenido del editor, actualizando el {@link DocumentModel}.
     */
    public void saveAsInteractive() {
        Path target = chooseSaveFile(mainWindow);
        if (target == null)
            return; // cancelado
        String text = editorPanel.getEditorText();
        try {
            saveToPath(target, text);
            documentModel.setText(text);
            documentModel.setFilePath(target);
            documentModel.setDirty(false);
            updateWindowTitle();
            info("Archivo guardado", target.toAbsolutePath().toString());
        } catch (IOException e) {
            showIoError(e, "Error al guardar el archivo");
        }
    }

    /*
     * =====================================================================
     * Operaciones puras (sin diálogos): útiles para pruebas o flujos custom
     * =====================================================================
     */

    /** Carga desde {@code path} y devuelve el texto. */
    public String loadFromPath(Path path) throws IOException {
        Objects.requireNonNull(path, "path");
        return textLoader.load(path);
    }

    /** Guarda {@code text} en {@code path}. */
    public void saveToPath(Path path, String text) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(text, "text");
        ensureParentDirectory(path);
        textSaver.save(path, text);
    }

    /**
     * Marca el documento como modificado. Invocar desde el DocumentListener del
     * editor.
     */
    public void markDirtyFromEditorChange() {
        documentModel.setDirty(true);
    updateWindowTitle();
    }

    private static Path chooseOpenFile(Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Abrir archivo de entrada");
        int op = fc.showOpenDialog(parent);
        return op == JFileChooser.APPROVE_OPTION ? fc.getSelectedFile().toPath() : null;
    }

    private static Path chooseSaveFile(Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar texto de entrada");
        fc.setSelectedFile(new java.io.File("entrada.txt"));
        int op = fc.showSaveDialog(parent);
        return op == JFileChooser.APPROVE_OPTION ? fc.getSelectedFile().toPath() : null;
    }

    private static void ensureParentDirectory(Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private void updateWindowTitle() {
        try {
            Path path = documentModel.getFilePath();
            String base = "Analizador léxico"; // título base de la app
            String filePathStr = path == null ? null : path.toAbsolutePath().toString();
            mainWindow.updateWindowTitle(base, filePathStr, documentModel.isDirty());
        } catch (Exception ignore) {
            // Silencioso: no todos los tests/mock de vista implementarán updateWindowTitle.
        }
    }


    private static void info(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showIoError(IOException e, String title) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null, e.getMessage(), title, JOptionPane.ERROR_MESSAGE);
    }
}
