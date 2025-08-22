package core.lexing.table;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Tabla de palabras reservadas (case-sensitive) cargadas desde config.json.
 *
 * Responsabilidad:
 *  - Responder de forma eficiente si un lexema IDENT es una palabra reservada EXACTA.
 *  - Mantener la colección inmodificable hacia afuera para evitar efectos laterales.
 *
 * No realiza normalizaciones, ni analiza el lexema; eso es trabajo del reconocedor.
 */
public final class ReservedWords {

    private final Set<String> words; // LinkedHashSet para mantener orden de inserción (si interesa en reportes)

    /**
     * @param words conjunto de palabras reservadas; no null. Puede estar vacío.
     */
    public ReservedWords(Set<String> words) {
        Objects.requireNonNull(words, "El conjunto de palabras reservadas no puede ser null");
        // Copia defensiva (case-sensitive por diseño de la práctica)
        this.words = Collections.unmodifiableSet(new LinkedHashSet<>(words));
    }

    /** ¿El lexema coincide EXACTAMENTE con una palabra reservada? (case-sensitive) */
    public boolean isReserved(String lexeme) {
        if (lexeme == null) return false;
        return words.contains(lexeme);
    }

    /** Vista inmodificable del conjunto subyacente (útil para pruebas/reportes). */
    public Set<String> asSet() {
        return words;
    }

    /** Cantidad de palabras reservadas. */
    public int size() {
        return words.size();
    }
}
