package model.search;

import java.util.Collections;
import java.util.List;

/**
 * Resultado de una búsqueda de patrón: lista de coincidencias (rango + posiciones).
 * Clase contenedor simple, sin lógica adicional.
 */
public final class SearchResult {

    private final List<MatchRange> matches;

    public SearchResult(List<MatchRange> matches) {
        this.matches = List.copyOf(matches);
    }

    /** Lista inmutable de coincidencias. */
    public List<MatchRange> matches() {
        return Collections.unmodifiableList(matches);
    }

    /** Cantidad total de coincidencias. */
    public int total() {
        return matches.size();
    }
}
