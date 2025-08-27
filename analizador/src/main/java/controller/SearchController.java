package controller;

import view.components.EditorPanel;
import view.components.SearchPanel;
import model.search.SearchResult;
import service.SearchService;
import model.search.MatchRange;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

/**
 * Controlador para la funcionalidad de <b>búsqueda</b> exigida por la práctica:
 * al ejecutar una búsqueda, se muestra el <i>texto completo</i> en un <b>panel
 * aparte</b>
 * con las coincidencias <b>resaltadas</b> (sin modificar el editor principal).
 * <p>
 * Esta clase orquesta únicamente: toma el texto del {@link EditorPanel},
 * delega la detección de coincidencias al {@link SearchService} y ordena
 * al {@link SearchPanel} que <b>renderice</b> el texto con los rangos hallados.
 */
public class SearchController {

    private final EditorPanel editorPanel;
    private final SearchPanel searchPanel;
    private final SearchService searchService;
    private SwingWorker<SearchResult, Void> currentWorker;

    public SearchController(EditorPanel editorPanel,
            SearchPanel searchPanel,
            SearchService searchService) {
        this.editorPanel = Objects.requireNonNull(editorPanel, "editorPanel");
        this.searchPanel = Objects.requireNonNull(searchPanel, "searchPanel");
        this.searchService = Objects.requireNonNull(searchService, "searchService");
    }

    /**
     * Ejecuta la bǧsqueda con la {@code query} dada. Si la consulta está vacía,
     * limpia el panel de bǧsqueda. La búsqueda se ejecuta en background para no
     * bloquear el EDT; el render ocurre en el EDT.
     */
    public void search(String query) {
        String q = (query == null) ? "" : query.trim();
        String text = editorPanel.getEditorText();
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');

        // Cancelar cualquier búsqueda previa en curso
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }

        if (q.isEmpty()) {
            clearResults(normalized);
            return;
        }

        boolean ignoreCase = searchPanel.isIgnoreCaseSelected();
        boolean wholeWord = searchPanel.isWholeWordSelected();
        // Deshabilitar controles mientras se ejecuta la búsqueda
        searchPanel.setControlsEnabled(false);

        final String queryUsed = q;
        final String textUsed = normalized;

        currentWorker = new SwingWorker<>() {
            @Override
            protected SearchResult doInBackground() throws Exception {
                return searchService.findAll(textUsed, queryUsed, !ignoreCase, wholeWord);
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) return;
                    // Evitar resultados obsoletos si cambiaron texto o query
                    String curTextNorm = editorPanel.getEditorText().replace("\r\n", "\n").replace('\r', '\n');
                    String curQuery = (searchPanel.getQueryText() == null) ? "" : searchPanel.getQueryText().trim();
                    if (!curTextNorm.equals(textUsed) || !curQuery.equals(queryUsed)) {
                        return;
                    }
                    SearchResult result = get();
                    List<MatchRange> ranges = (result == null) ? java.util.List.of() : result.matches();
                    // Render en el panel de búsqueda (crea nueva), no tocar el editor
                    searchPanel.render(textUsed, ranges);

                    if (ranges == null || ranges.isEmpty()) {
                        JOptionPane.showMessageDialog(null,
                                "No se encontraron coincidencias.",
                                "Buscar", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    if (!isCancelled()) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null,
                                "Ocurri�� un error al buscar: " + ex.getMessage(),
                                "Buscar", JOptionPane.ERROR_MESSAGE);
                    }
                } finally {
                    searchPanel.setControlsEnabled(true);
                }
            }
        };
        currentWorker.execute();
    }

    /**
     * Limpia los resultados de búsqueda mostrando el texto sin resaltados
     * en el {@link SearchPanel}.
     */
    public void clear() {
        String text = editorPanel.getEditorText();
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
        searchPanel.setControlsEnabled(true);
        clearResults(normalized);
    }

    private void clearResults(String text) {
        // Renderizar sin rangos (sin resaltados)
        searchPanel.render(text, java.util.List.of());
    }
}

