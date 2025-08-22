package core.lexing.recognizer;

import core.lexing.stream.CharClasses;
import core.lexing.stream.CharCursor;
import core.lexing.table.OperatorTable;
import model.config.Config;
import model.config.CommentsConfig;

/**
 * Reconocedor de CADENAS entre comillas dobles.
 *
 * Regla (según la práctica):
 *  - Una cadena inicia con '"' y termina con '"' (cierre obligatorio).
 *  - El contenido puede contener letras, dígitos y espacios/saltos de línea,
 *    además de cualquier símbolo definido en las tablas de operadores,
 *    puntuación, agrupación o los prefijos de comentario configurados.
 *
 * Casos de error cubiertos:
 *  - "Cadena no cerrada": si se llega a EOF sin encontrar la comilla de cierre.
 *  - "Símbolo fuera del alfabeto permitido en cadena": si algún carácter dentro
 *    de la cadena no pertenece al alfabeto permitido.
 *
 * Importante:
 *  - NO consume del cursor (sólo calcula longitud).
 *  - NO usa regex; char-a-char con peek(k).
 */
public final class StringRecognizer {

    private static final String MSG_NO_CERRADA = "Cadena no cerrada";
    public static final String MSG_SIMBOLO_INVALIDO = "Símbolo fuera del alfabeto permitido en cadena";

    private final Config config;
    private final OperatorTable operators;
    private final OperatorTable punctuation;
    private final OperatorTable grouping;

    public StringRecognizer(Config config,
                             OperatorTable operators,
                             OperatorTable punctuation,
                             OperatorTable grouping) {
        this.config = config;
        this.operators = operators;
        this.punctuation = punctuation;
        this.grouping = grouping;
    }

    /**
     * Intenta reconocer una cadena delimitada por comillas.
     *
     * @param cursor flujo de caracteres (no se consume aquí)
     * @return Recognition:
     *   - matched=false si el primer char no es comilla doble ".
     *   - matched=true sin error: longitud = desde la comilla inicial hasta la comilla de cierre inclusive.
     *   - matched=true con error: si no hay comilla de cierre hasta EOF, longitud = desde inicio hasta EOF,
     *       con mensaje "Cadena no cerrada".
     *   - matched=true con error: si se encuentra un símbolo fuera del alfabeto
     *       permitido, longitud = lexema parcial + símbolo inválido.
     */
    public Recognition recognize(CharCursor cursor) {
        if (cursor == null || cursor.eof()) return Recognition.noMatch();
        if (!CharClasses.isQuote(cursor.peek())) return Recognition.noMatch();

        int len = 1; // contamos la comilla inicial
        while (true) {
            int c = cursor.peek(len);
            if (c == CharCursor.EOF) {
                // No apareció cierre
                return Recognition.error(len, "\"", MSG_NO_CERRADA);
            }
            if (c == '"') {
                // cierre incluido
                len++;
                return Recognition.match(len);
            }
            if (!isAllowedChar(c)) {
                // Consumimos también el símbolo inválido para evitar bucles
                return Recognition.error(len + 1, null, MSG_SIMBOLO_INVALIDO);
            }
            len++;
        }
    }

    private boolean isAllowedChar(int c) {
        if (CharClasses.isLetter(c) || CharClasses.isDigit(c) || CharClasses.isSpaceOrNewline(c) || CharClasses.isQuote(c)) {
            return true;
        }
        if (containsChar(operators, c)) return true;
        if (containsChar(punctuation, c)) return true;
        if (containsChar(grouping, c)) return true;
        CommentsConfig com = (config != null) ? config.getComentarios() : null;
        if (com != null) {
            if (containsChar(com.getLinea(), c)) return true;
            if (containsChar(com.getBloqueInicio(), c)) return true;
            if (containsChar(com.getBloqueFin(), c)) return true;
        }
        return false;
    }

    private boolean containsChar(OperatorTable table, int c) {
        if (table == null) return false;
        for (String s : table.symbols()) {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == c) return true;
            }
        }
        return false;
    }

    private boolean containsChar(String s, int c) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) return true;
        }
        return false;
    }
}

