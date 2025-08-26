package service;

import java.util.Objects;

import core.search.SearchEngine;
import model.config.Config;
import model.document.DocumentModel;
import model.search.SearchResult;

/**
 * Servicio de alto nivel para ejecutar búsquedas de patrones sobre el texto
 * del documento. Actúa como una fachada del {@link SearchEngine} y se encarga
 * de actualizar el {@link DocumentModel} con el resultado para que la capa de
 * interfaz pueda resaltarlo.
 */
public final class SearchService {

    private final SearchEngine engine = new SearchEngine();

    /**
     * Ejecuta la búsqueda sobre el texto del documento y almacena el resultado.
     *
     * @param doc             modelo del documento (no null)
     * @param pattern         patrón a buscar (no null ni vacío)
     * @param caseSensitive   true para respetar mayúsculas/minúsculas
     * @param wholeWord       true para coincidir solo palabras completas
     * @param includeComments true para incluir coincidencias dentro de comentarios
     * @param config          configuración dinámica (se usa si
     *                        includeComments=false)
     * @return resultado de la búsqueda
     */
    public SearchResult search(DocumentModel doc,
            String pattern,
            boolean caseSensitive,
            boolean wholeWord,
            boolean includeComments,
            Config config) {
        Objects.requireNonNull(doc, "doc no puede ser null");
        String text = Objects.requireNonNull(doc.getText(), "El texto del documento no puede ser null");

        SearchResult res = engine.search(text, pattern, caseSensitive, wholeWord, includeComments, config);
        doc.setSearchResult(res);
        return res;
    }

    /**
     * Búsqueda rápida directa sobre un texto (sin DocumentModel) usada por el
     * SearchController
     * simplificado de la práctica. Por defecto se busca con sensibilidad a
     * mayúsculas y
     * SIN excluir comentarios (no hay Config disponible aquí). Si se requiere más
     * control,
     * usar el método
     * {@link #search(DocumentModel, String, boolean, boolean, boolean, Config)}.
     */
    public SearchResult findAll(String text, String pattern, boolean caseSensitive, boolean wholeWord) {
        return engine.search(Objects.requireNonNull(text, "text"),
                Objects.requireNonNull(pattern, "pattern"),
                caseSensitive,
                wholeWord,
                true, // includeComments por defecto
                null // no config necesaria
        );
    }
}
