package core.lexing.recognizer;

import core.lexing.stream.CharCursor;
import core.lexing.table.OperatorTable;

import java.util.Objects;

/**
 * Reconocedor de SIGNOS DE AGRUPACIÓN.
 *
 * Responsabilidad:
 *  - Detectar, sin consumir, un signo de agrupación definido en config.json
 *    desde la posición actual, devolviendo la longitud del símbolo más largo.
 *
 * Importante:
 *  - No produce tokens ni errores (no estipulados por la práctica para esta categoría).
 *  - No consume del cursor; el consumo lo decide el lexer.
 *  - Greedy longest-first basado en OperatorTable (sin regex).
 */
public final class GroupingRecognizer {

    private final OperatorTable table;

    /**
     * @param table tabla construida con el conjunto de agrupación de config.json
     */
    public GroupingRecognizer(OperatorTable table) {
        this.table = Objects.requireNonNull(table, "OperatorTable no puede ser null");
    }

    /**
     * Intenta reconocer un signo de agrupación en la posición actual.
     *
     * @param cursor flujo de caracteres (no se consume aquí)
     * @return Recognition:
     *   - matched=false si no hay agrupación en esta posición.
     *   - matched=true  y length = tamaño del símbolo más largo si sí hay.
     */
    public Recognition recognize(CharCursor cursor) {
        if (cursor == null || cursor.eof()) return Recognition.noMatch();

        String sym = table.longestMatch(cursor);
        if (sym == null) return Recognition.noMatch();

        return Recognition.match(sym.length());
    }
}
