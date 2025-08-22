package core.lexing.table;

import core.lexing.stream.CharCursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Tabla de símbolos para reconocimiento "longest-first".
 *
 * Uso típico:
 *   - Construir con el conjunto de símbolos de config.json (operadores, puntuación o agrupación).
 *   - Invocar longestMatch(cursor) para obtener el símbolo más largo que
 *     coincide exactamente desde la posición actual del cursor (sin consumir).
 *
 * Decisiones de diseño:
 *   - No consume caracteres (SRP). El consumo lo hará el reconocedor/lexer.
 *   - Ordena internamente los símbolos por longitud descendente para garantizar
 *     reconocimiento ávido (greedy). En conjuntos pequeños/medianos es simple y eficiente.
 *   - Sin regex, sin helpers de cadena avanzados; solo comparaciones char-a-char
 *     con CharCursor.peek(k), coherente con la práctica.
 */
public final class OperatorTable {

    private final List<String> symbols;   // ordenados por longitud desc
    private final int maxLen;

    /**
     * @param symbolSet conjunto de símbolos (no null, sin cadenas vacías)
     *                  p. ej. operadores ["+", "-", "*", "/", "%", "="]
     *                  o puntuación/agrupación del config.json
     */
    public OperatorTable(Set<String> symbolSet) {
        Objects.requireNonNull(symbolSet, "El conjunto de símbolos no puede ser null");

        // Copia defensiva y validación mínima
        List<String> tmp = new ArrayList<>(symbolSet.size());
        for (String s : symbolSet) {
            if (s == null) {
                throw new IllegalArgumentException("Un símbolo no puede ser null.");
            }
            if (s.isEmpty()) {
                throw new IllegalArgumentException("Los símbolos no pueden ser cadenas vacías.");
            }
            tmp.add(s);
        }

        // Orden por longitud descendente para greedy matching
        tmp.sort(Comparator.comparingInt(String::length).reversed());
        this.symbols = Collections.unmodifiableList(tmp);

        int ml = 0;
        for (String s : symbols) ml = Math.max(ml, s.length());
        this.maxLen = ml;
    }

    /** Vista inmodificable de los símbolos (útil para diagnósticos/pruebas). */
    public List<String> symbols() {
        return symbols;
    }

    /** Longitud del símbolo más largo. */
    public int maxSymbolLength() {
        return maxLen;
    }

    /**
     * Devuelve el símbolo MÁS LARGO que coincide exactamente desde la
     * posición actual del cursor, o null si no hay coincidencia.
     *
     * No consume caracteres. El reconocedor debe consumir tantos chars
     * como la longitud del símbolo devuelto.
     */
    public String longestMatch(CharCursor cursor) {
        if (cursor == null || cursor.eof()) return null;

        // Recorre de mayor a menor longitud; retorna el primero que calce
        outer:
        for (String sym : symbols) {
            int len = sym.length();
            for (int i = 0; i < len; i++) {
                int ch = cursor.peek(i);
                if (ch == CharCursor.EOF || (char) ch != sym.charAt(i)) {
                    continue outer; // este símbolo no matchea
                }
            }
            return sym; // match completo
        }
        return null;
    }

    /** ¿El conjunto contiene exactamente este símbolo? (útil para validaciones simples). */
    public boolean contains(String symbol) {
        return symbols.contains(symbol);
    }
}
