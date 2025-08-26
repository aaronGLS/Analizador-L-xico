package controller;

import service.AnalyzeService;
import service.ReportBuilder;
import service.GradingService;
import service.NotUsedCalculator; // (inyectado por requisito aunque GradingService tenga uno interno)

import model.config.Config;
import model.document.DocumentModel;
import model.report.GeneralReport;

import view.components.EditorPanel;
import view.components.ReportsPanel;
import view.components.ErrorsTablePanel;
import view.components.TokensTablePanel;
import view.components.LexemeCountTablePanel;
import view.components.GeneralReportPanel;
import view.table.ErrorTableModel;
import view.table.TokenTableModel;
import view.table.LexemeCountTableModel;
import javax.swing.*;
import java.awt.Cursor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Controlador que orquesta la operación "Analizar":
 * - Obtiene el texto desde {@link EditorPanel}.
 * - Invoca {@link AnalyzeService} para producir tokens y errores.
 * - Con {@link ReportBuilder} construye filas para las tablas.
 * - Con {@link GradingService} genera el reporte general (incluye % válidos y
 * no utilizados).
 * - Actualiza {@link ReportsPanel} aplicando las reglas de visibilidad:
 * WITH_ERRORS -> mostrar solo Errores + General.
 * CLEAN -> mostrar Tokens + Recuento + General.
 * - Expone estado para habilitar/deshabilitar exportaciones en otros
 * controllers.
 */
public final class AnalyzeController {

    public enum AnalysisState {
        WITH_ERRORS, CLEAN
    }

    private final EditorPanel editorPanel;
    private final ReportsPanel reportsPanel;
    private final Config config; // Configuración viva (se modifica en ConfigDialog)
    private final DocumentModel documentModel;
    private final AnalyzeService analyzeService;
    private final ReportBuilder reportBuilder;
    private final GradingService gradingService;
    @SuppressWarnings("unused")
    private final NotUsedCalculator notUsedCalculator; // redundante pero se inyecta para cumplir requisito

    private final ErrorsTablePanel errorsPanel;
    private final TokensTablePanel tokensPanel;
    private final LexemeCountTablePanel lexemeCountPanel;
    private final GeneralReportPanel generalPanel;
    private final ErrorTableModel errorTableModel = new ErrorTableModel();
    private final TokenTableModel tokenTableModel = new TokenTableModel();
    private final LexemeCountTableModel lexemeCountTableModel = new LexemeCountTableModel();

    private AnalysisState lastState = AnalysisState.CLEAN; // por defecto (sin errores al inicio)
    private Consumer<AnalysisState> onStateChanged;

    private volatile boolean analyzing = false; // evita ejecuciones reentrantes

    public AnalyzeController(EditorPanel editorPanel,
            ReportsPanel reportsPanel,
            DocumentModel documentModel,
            Config config,
            AnalyzeService analyzeService,
            ReportBuilder reportBuilder,
            GradingService gradingService,
            NotUsedCalculator notUsedCalculator) {
        this.editorPanel = Objects.requireNonNull(editorPanel, "editorPanel");
        this.reportsPanel = Objects.requireNonNull(reportsPanel, "reportsPanel");
        this.documentModel = Objects.requireNonNull(documentModel, "documentModel");
        this.config = Objects.requireNonNull(config, "config");
        this.analyzeService = Objects.requireNonNull(analyzeService, "analyzeService");
        this.reportBuilder = Objects.requireNonNull(reportBuilder, "reportBuilder");
        this.gradingService = Objects.requireNonNull(gradingService, "gradingService");
        this.notUsedCalculator = Objects.requireNonNull(notUsedCalculator, "notUsedCalculator");

        // Obtener subvistas reales desde ReportsPanel
        this.errorsPanel = reportsPanel.getErrorsPanel();
        this.tokensPanel = reportsPanel.getTokensPanel();
        this.lexemeCountPanel = reportsPanel.getLexemeCountPanel();
        this.generalPanel = reportsPanel.getGeneralPanel();

        // Vincular modelos a las tablas (vacíos inicialmente)
        this.errorsPanel.setTableModel(errorTableModel);
        this.tokensPanel.setTableModel(tokenTableModel);
        this.lexemeCountPanel.setTableModel(lexemeCountTableModel);
    }

    /**
     * Ejecuta el análisis léxico en background y actualiza los reportes al
     * terminar.
     */
    public void analyze() {
        if (analyzing)
            return; // prevenir reentrada
        analyzing = true;
        final String text = editorPanel.getEditorText();

        // (Opcional) feedback inmediato
        setAnalyzeBusy(true);

        SwingWorker<WorkerResult, Void> worker = new SwingWorker<>() {
            @Override
            protected WorkerResult doInBackground() {
                try {
                    // Texto vacío => tratar como resultado vacío sin errores
                    var serviceResult = (text == null || text.isBlank())
                            ? new AnalyzeService.Result(List.of(), List.of())
                            : analyzeService.analyzeText(text);

                    var builderResult = reportBuilder.build(serviceResult.tokens(), serviceResult.errors());
                    var general = gradingService.build(config, serviceResult.tokens(), serviceResult.errors());
                    return new WorkerResult(serviceResult.errors(), serviceResult.tokens(), builderResult, general);
                } catch (Exception ex) {
                    return new WorkerResult(ex);
                }
            }

            @Override
            protected void done() {
                try {
                    WorkerResult result = get();
                    if (result.failure != null) {
                        // Mostrar error y mantener estado consistente (limpiar modelos pero no cambiar
                        // lastState)
                        showErrorDialog("Error durante el análisis: " + result.failure.getMessage());
                        clearDataModels();
                        updateGeneralPanelForEmpty();
                        documentModel.clearAnalysis();
                        // No notificar cambio de estado (se mantiene previous)
                        return;
                    }
                    applyWorkerResult(result);
                    documentModel.setTokens(result.tokens);
                    documentModel.setErrors(result.errors);
                    documentModel.setGeneralReport(result.generalReport);
                } catch (Exception e) {
                    showErrorDialog("Fallo inesperado al obtener resultado: " + e.getMessage());
                } finally {
                    analyzing = false;
                    setAnalyzeBusy(false);
                }
            }
        };
        worker.execute();
    }

    /** Devuelve true si el último análisis no tuvo errores. */
    public boolean isClean() {
        return lastState == AnalysisState.CLEAN;
    }

    /** Devuelve true si el último análisis tuvo errores. */
    public boolean hasErrors() {
        return lastState == AnalysisState.WITH_ERRORS;
    }

    /** Devuelve el estado crudo. */
    public AnalysisState getLastState() {
        return lastState;
    }

    /** Permite suscribir un listener a cambios de estado (WITH_ERRORS vs CLEAN). */
    public void setOnStateChanged(Consumer<AnalysisState> listener) {
        this.onStateChanged = listener;
    }

    /** Limpia el modelo de errores. */
    public void clearErrorTableModel() {
        errorTableModel.clear();
    }

    /** Limpia el modelo de tokens. */
    public void clearTokenTableModel() {
        tokenTableModel.clear();
    }

    /** Limpia el modelo de recuento de lexemas. */
    public void clearLexemeCountTableModel() {
        lexemeCountTableModel.clear();
    }

    /** Restablece el panel de reporte general a valores vacíos. */
    public void resetGeneralReportPanel() {
        updateGeneralPanelForEmpty();
    }

    /** Restablece el estado interno a CLEAN sin notificar. */
    public void resetStateToClean() {
        this.lastState = AnalysisState.CLEAN;
    }

    private void applyWorkerResult(WorkerResult r) {
        boolean hasErrors = !r.errors.isEmpty();
        // Actualizar tablas según reglas
        if (hasErrors) {
            // Errores
            errorTableModel.setRows(r.builderResult.errores());
            // Limpiar otras
            tokenTableModel.clear();
            lexemeCountTableModel.clear();
            reportsPanel.setDataTabsEnabled(false); // deshabilita Tokens & Recuento
            reportsPanel.selectErrorsTab();
            updateState(AnalysisState.WITH_ERRORS);
        } else {
            // Tokens + Recuento
            tokenTableModel.setRows(r.builderResult.tokens());
            lexemeCountTableModel.setRows(r.builderResult.recuento());
            errorTableModel.clear();
            reportsPanel.setDataTabsEnabled(true);
            reportsPanel.selectTokensTab();
            updateState(AnalysisState.CLEAN);
        }

        // Reporte General (siempre)
        updateGeneralPanel(r.generalReport);
    }

    private void updateState(AnalysisState newState) {
        if (this.lastState != newState) {
            this.lastState = newState;
            if (onStateChanged != null) {
                onStateChanged.accept(newState);
            }
        }
    }

    private void updateGeneralPanel(GeneralReport gr) {
        if (gr == null) {
            updateGeneralPanelForEmpty();
            return;
        }
        generalPanel.setErrorsCount(gr.errorCount());
        generalPanel.setValidPercent(gr.percentValid());
        // Formatear lista de no utilizados
        List<String> items = formatNotUsed(gr);
        generalPanel.setNotUsedCount(items.size());
        generalPanel.setNotUsedItems(items);
    }

    private void updateGeneralPanelForEmpty() {
        generalPanel.setErrorsCount(0);
        generalPanel.setValidPercent(100.0);
        generalPanel.setNotUsedCount(0);
        generalPanel.setNotUsedItems(List.of());
    }

    private List<String> formatNotUsed(GeneralReport gr) {
        List<String> items = new ArrayList<>();
        gr.reservadasNoUsadas().forEach(s -> items.add("[RESERVADA] " + s));
        gr.operadoresNoUsados().forEach(s -> items.add("[OPERADOR] " + s));
        gr.puntuacionNoUsada().forEach(s -> items.add("[PUNTUACION] " + s));
        gr.agrupacionNoUsada().forEach(s -> items.add("[AGRUPACION] " + s));
        return items;
    }

    private void clearDataModels() {
        errorTableModel.clear();
        tokenTableModel.clear();
        lexemeCountTableModel.clear();
    }

    private void setAnalyzeBusy(boolean busy) {
        // Simple gestión: cambiar título de pestaña General o cursor (sin bloquear EDT)
        try {
            JTabbedPane tabs = reportsPanel.getTabbedPane();
            if (busy) {
                tabs.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            } else {
                tabs.setCursor(Cursor.getDefaultCursor());
            }
        } catch (Exception ignored) {
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(reportsPanel), message, "Error de análisis",
                JOptionPane.ERROR_MESSAGE);
    }

    private static final class WorkerResult {
        final List<model.lexical.LexError> errors;
        final List<model.lexical.Token> tokens;
        final ReportBuilder.Result builderResult;
        final GeneralReport generalReport;
        final Exception failure;

        WorkerResult(List<model.lexical.LexError> errors,
                List<model.lexical.Token> tokens,
                ReportBuilder.Result builderResult,
                GeneralReport generalReport) {
            this.errors = errors;
            this.tokens = tokens;
            this.builderResult = builderResult;
            this.generalReport = generalReport;
            this.failure = null;
        }

        WorkerResult(Exception failure) {
            this.errors = List.of();
            this.tokens = List.of();
            this.builderResult = null; // no se puede instanciar directamente (constructor privado)
            this.generalReport = null;
            this.failure = failure;
        }
    }
}
