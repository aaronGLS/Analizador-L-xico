package core.lexing.recognizer;

import core.lexing.stream.CharCursor;
import core.lexing.table.OperatorTable;

import java.util.Objects;

/**
 * Reconocedor de OPERADORES.
 *
 * Responsabilidad:
 *  - Detectar, sin consumir, si en la posición actual del cursor existe un
 *    operador definido en la configuración.
 *  - En caso afirmativo, devolver el match con la longitud EXACTA del símbolo
 *    más largo (política greedy "longest-first").
 *
 * Importante:
 *  - No produce tokens ni errores (la práctica no define errores para esta categoría).
 *  - No consume del cursor; el consumo se hará en el lexer orquestador.
 *  - Trabaja char a char (sin regex) a través de OperatorTable.
 */
public final class OperatorRecognizer {

    private final OperatorTable table;

    /**
     * @param table tabla construida con el conjunto de operadores de config.json
     */
    public OperatorRecognizer(OperatorTable table) {
        this.table = Objects.requireNonNull(table, "OperatorTable no puede ser null");
    }

    /**
     * Intenta reconocer un operador comenzando en la posición actual.
     *
     * @param cursor flujo de caracteres (no se consume aquí)
     * @return Recognition:
     *   - matched=false si no hay operador en esta posición.
     *   - matched=true  y length = tamaño del operador más largo si sí hay.
     */
    public Recognition recognize(CharCursor cursor) {
        if (cursor == null || cursor.eof()) return Recognition.noMatch();

        String sym = table.longestMatch(cursor);
        if (sym == null) return Recognition.noMatch();

        return Recognition.match(sym.length());
    }
}
