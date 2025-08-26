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

    public SearchController(EditorPanel editorPanel,
            SearchPanel searchPanel,
            SearchService searchService) {
        this.editorPanel = Objects.requireNonNull(editorPanel, "editorPanel");
        this.searchPanel = Objects.requireNonNull(searchPanel, "searchPanel");
        this.searchService = Objects.requireNonNull(searchService, "searchService");
    }

    /**
     * Ejecuta la búsqueda con la {@code query} dada. Si la consulta está vacía,
     * limpia el panel de búsqueda.
     */
    public void search(String query) {
        String q = (query == null) ? "" : query.trim();
        String text = editorPanel.getEditorText();

        if (q.isEmpty()) {
            clearResults(text);
            return;
        }

        try {
            boolean ignoreCase = searchPanel.isIgnoreCaseSelected();
            boolean wholeWord = searchPanel.isWholeWordSelected();
            SearchResult result = searchService.findAll(text, q, !ignoreCase, wholeWord);
            List<MatchRange> ranges = result.matches();
            // Render en el panel de búsqueda (área nueva), no tocar el editor
            searchPanel.render(text, ranges);

            if (ranges == null || ranges.isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "No se encontraron coincidencias.",
                        "Buscar", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Ocurrió un error al buscar: " + ex.getMessage(),
                    "Buscar", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Limpia los resultados de búsqueda mostrando el texto sin resaltados
     * en el {@link SearchPanel}.
     */
    public void clear() {
        String text = editorPanel.getEditorText();
        clearResults(text);
    }

    // --------------------------------------------------------------
    // Helpers
    // --------------------------------------------------------------

    private void clearResults(String text) {
        // Renderizar sin rangos (sin resaltados)
        searchPanel.render(text, java.util.List.of());
    }
}
