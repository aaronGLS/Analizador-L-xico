package controller;

import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import core.io.ConfigLoader;
import core.io.ConfigSaver;
import core.io.TextLoader;
import core.io.TextSaver;
import model.config.Config;
import model.document.DocumentModel;
import service.AnalyzeService;
import service.GradingService;
import service.HighlightService;
import service.NotUsedCalculator;
import service.ReportBuilder;
import service.SearchService;
import view.MainWindow;
import view.components.EditorPanel;
import view.components.ReportsPanel;
import view.components.SearchPanel;

/**
 * Controlador raíz que integra y orquesta todos los demás controladores,
 * servicios, modelos y la ventana principal. Punto único de inicio.
 */
public final class MainController {

    // Modelo principal
    private final DocumentModel documentModel = new DocumentModel();

    // Configuración dinámica
    private final Config config = new Config();

    // I/O config
    private final ConfigLoader configLoader = new ConfigLoader();
    private final ConfigSaver configSaver = new ConfigSaver();

    // I/O texto
    private final TextLoader textLoader = new TextLoader();
    private final TextSaver textSaver = new TextSaver();

    // Servicios
    private final AnalyzeService analyzeService = new AnalyzeService(config);
    private final ReportBuilder reportBuilder = new ReportBuilder();
    private final GradingService gradingService = new GradingService();
    private final NotUsedCalculator notUsedCalculator = new NotUsedCalculator();
    private final SearchService searchService = new SearchService();
    private final HighlightService highlightService = new HighlightService(config);

    // Vista principal
    private final MainWindow mainWindow = new MainWindow();
    private final EditorPanel editorPanel = mainWindow.getEditorPanel();
    private final ReportsPanel reportsPanel = mainWindow.getReportsPanel();
    private final SearchPanel searchPanel = mainWindow.getSearchPanel();

    // Controladores específicos
    private final FileController fileController = new FileController(
            mainWindow, editorPanel, documentModel, textLoader, textSaver);
    private final AnalyzeController analyzeController = new AnalyzeController(
            editorPanel, reportsPanel, documentModel, config, analyzeService, reportBuilder, gradingService,
            notUsedCalculator);
    private final ExportController exportController = new ExportController(
            mainWindow, editorPanel, reportsPanel.getErrorsPanel(), reportsPanel.getTokensPanel(),
            reportsPanel.getLexemeCountPanel());
    private final SearchController searchController = new SearchController(
            editorPanel, searchPanel, searchService);
    private final ConfigController configController = new ConfigController(
            mainWindow, config, configLoader, configSaver);

    // Ruta de configuración (se intenta la usada por ConfigController; fallback si
    // no existe)
    private Path configPath = Paths.get("resources", "config.json");

    /** Inicia la aplicación (cargar config + mostrar UI). */
    public void start() {
        EventQueue.invokeLater(() -> {
            loadInitialConfig();
            wireViewHandlers();
            wireMenuActions();
            installEditorListeners();
            analyzeController.setOnStateChanged(state -> {
                // Habilitar exportación solo si hay datos (tokens o errores)
                // Simple: habilitar siempre tras primer análisis
                mainWindow.getMiExportarReportes().setEnabled(true);
                highlightEditor();
            });
            configController.setOnConfigChanged(() -> {
                // Reaplicar resaltado y reanalizar si desea el usuario
                highlightEditor();
                // Opcional: reanalizar automáticamente
                analyzeController.analyze();
            });
            mainWindow.getMiExportarReportes().setEnabled(false);
            mainWindow.setVisible(true);
        });
    }

    /* ===================== Carga de configuración ===================== */
    private void loadInitialConfig() {
        // Intentar ruta primaria; si no existe probar en src/main/java/resources
        try {
            if (!Files.exists(configPath)) {
                Path alt = Paths.get("src", "main", "java", "resources", "config.json");
                if (Files.exists(alt))
                    configPath = alt;
            }
            Config loaded = configLoader.load(configPath);
            copyConfigIntoLive(config, loaded);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "No se pudo cargar config.json: " + ex.getMessage() + "\nSe continuará con configuración vacía.",
                    "Configuración", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static void copyConfigIntoLive(Config dst, Config src) {
        if (dst == null || src == null)
            return;
        dst.setPalabrasReservadas(src.getPalabrasReservadas());
        dst.setOperadores(src.getOperadores());
        dst.setPuntuacion(src.getPuntuacion());
        dst.setAgrupacion(src.getAgrupacion());
        dst.setComentarios(src.getComentarios());
    }

    /* ===================== Wiring de handlers de vista ===================== */
    private void wireViewHandlers() {
        editorPanel.setOnFind(() -> mainWindow.showSearchPanel());
        editorPanel.setOnSave(fileController::saveInteractive);
        editorPanel.setOnAnalyze(analyzeController::analyze);

        // SearchPanel bindings (modo simple)
        searchPanel.setOnSearch(() -> searchController.search(searchPanel.getQueryText()));
        searchPanel.setOnClose(mainWindow::hideSearchPanel);
        // Next/Prev reutilizan la misma búsqueda por simplicidad
        searchPanel.setOnNext(() -> searchController.search(searchPanel.getQueryText()));
        searchPanel.setOnPrev(() -> searchController.search(searchPanel.getQueryText()));
    }

    private void wireMenuActions() {
        // Archivo
        mainWindow.getMiNuevo().addActionListener(e -> newDocument());
        mainWindow.getMiAbrir().addActionListener(e -> {
            fileController.openInteractive();
            highlightEditor();
        });
        mainWindow.getMiGuardar().addActionListener(e -> fileController.saveInteractive());
        mainWindow.getMiGuardarComo().addActionListener(e -> fileController.saveAsInteractive());
        mainWindow.getMiExportarReportes().addActionListener(e -> exportController.exportReportsInteractive());

        // Edición / Búsqueda
        mainWindow.getMiBuscar().addActionListener(e -> {
            mainWindow.showSearchPanel();
            searchPanel.focusQuery();
        });
        mainWindow.getMiLimpiarResaltados().addActionListener(e -> searchController.clear());

        // Ver
        mainWindow.getMiToggleSearch().addActionListener(e -> mainWindow.toggleSearchPanel());
        mainWindow.getMiToggleReports().addActionListener(e -> mainWindow.toggleReports());

        // Análisis
        mainWindow.getMiAnalizar().addActionListener(e -> analyzeController.analyze());
        mainWindow.getMiLimpiarResultados().addActionListener(e -> clearReports());

        // Configuración
        mainWindow.getMiConfigurarLenguaje().addActionListener(e -> configController.openDialogAndSave());

        // Ayuda
        mainWindow.getMiAcercaDe().addActionListener(showAbout());
    }

    private ActionListener showAbout() {
        return e -> JOptionPane.showMessageDialog(mainWindow,
                "Analizador léxico – Ejemplo de integración completa\nAutor: (Tu nombre)",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }

    /*
     * ===================== Listeners de editor (estado y resaltado)
     * =====================
     */
    private void installEditorListeners() {
        // Dirty + stats
        editorPanel.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onChange();
            }

            private void onChange() {
                fileController.markDirtyFromEditorChange();
                updateStats();
            }
        });
        // Caret -> posición línea/columna
        CaretListener caretListener = e -> updateCaretPosition();
        editorPanel.getEditorPane().addCaretListener(caretListener);
        updateStats();
        updateCaretPosition();
    }

    private void updateStats() {
        String text = editorPanel.getEditorText();
        int chars = text.length();
        editorPanel.setStatusStatsText("Caracteres: " + chars);
    }

    private void updateCaretPosition() {
        int pos = editorPanel.getEditorPane().getCaretPosition();
        String text = editorPanel.getEditorText();
        int line = 1, col = 1;
        for (int i = 0, c = 0; i < text.length() && i < pos; i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                line++;
                c = 0;
            } else {
                c++;
            }
            col = c + 1;
        }
        editorPanel.setStatusPositionText("Línea: " + line + " Columna: " + col);
    }

    /* ===================== Acciones de alto nivel ===================== */
    private void newDocument() {
        documentModel.clearAnalysis();
        documentModel.setFilePath(null);
        documentModel.setText("");
        documentModel.setDirty(false);
        editorPanel.setEditorText("");
        clearReports();
    }

    private void clearReports() {
        reportsPanel.getErrorsPanel().clearSelection();
        // Modelos se limpian a través de AnalyzeController si se implementa método
        // público.
        // Aquí simplemente limpiamos resaltado y panel general.
        editorPanel.resetAttributes();
    }

    private void highlightEditor() {
        SwingUtilities.invokeLater(() -> {
            try {
                editorPanel.resetAttributes();
                var spans = highlightService.highlight(editorPanel.getEditorText());
                for (var s : spans) {
                    applyColor(s.start(), s.end() - s.start(), s.color());
                }
            } catch (Exception ignore) {
                /* silencioso */ }
        });
    }

    private void applyColor(int start, int length, java.awt.Color color) {
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, color);
        editorPanel.applyAttributes(start, length, attrs);
    }

    /* ===================== Utilidades ===================== */
    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public static void main(String[] args) {
        new MainController().start();
    }
}
