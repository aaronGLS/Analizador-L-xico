package model.document;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import model.lexical.LexError;
import model.lexical.Token;
import model.report.GeneralReport;
import model.search.SearchResult;

/**
 * Modelo central que representa el estado del documento que edita el usuario.
 *
 * Mantiene el contenido actual y los artefactos derivados del análisis léxico
 * o de las búsquedas realizadas.  No contiene lógica de UI y procura exponer
 * únicamente getters y setters sencillos.
 */
public final class DocumentModel {

    private String text = "";            // texto completo en memoria
    private Path   filePath;              // ruta del archivo abierto (puede ser null)
    private List<Token> tokens = List.of();
    private List<LexError> errors = List.of();
    private SearchResult searchResult;    // última búsqueda realizada
    private GeneralReport generalReport;  // último reporte general generado

    /* ======================== texto y archivo ======================== */

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = Objects.requireNonNull(text, "text no puede ser null");
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath; // puede ser null para "documento nuevo"
    }

    /* ======================== tokens y errores ======================= */

    /** Lista inmutable de tokens válidos. */
    public List<Token> getTokens() {
        return Collections.unmodifiableList(tokens);
    }

    public void setTokens(List<Token> tokens) {
        this.tokens = (tokens == null) ? List.of() : List.copyOf(tokens);
    }

    /** Lista inmutable de errores léxicos. */
    public List<LexError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void setErrors(List<LexError> errors) {
        this.errors = (errors == null) ? List.of() : List.copyOf(errors);
    }

    /** ¿El análisis actual contiene errores? */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /** Limpia tokens y errores (p.ej. antes de un nuevo análisis). */
    public void clearAnalysis() {
        tokens = List.of();
        errors = List.of();
        generalReport = null;
    }

    /* ======================== reportes adicionales =================== */

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
    }

    public GeneralReport getGeneralReport() {
        return generalReport;
    }

    public void setGeneralReport(GeneralReport generalReport) {
        this.generalReport = generalReport;
    }

    /** Elimina los datos de la última búsqueda. */
    public void clearSearch() {
        searchResult = null;
    }
}

