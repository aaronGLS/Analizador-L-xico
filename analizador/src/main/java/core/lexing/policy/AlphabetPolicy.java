package core.lexing.policy;

import core.lexing.stream.CharClasses;
import core.lexing.stream.CharCursor;
import core.lexing.table.OperatorTable;
import model.config.CommentsConfig;
import model.config.Config;

/**
 * Política mínima para validar si el símbolo en la posición actual
 * pertenece al alfabeto permitido por la práctica.
 *
 * Alfabeto base (según la guía):
 *  - Letras A–Z / a–z
 *  - Dígitos 0–9
 *  - Espacio y salto de línea (CR/LF)
 *  - Comillas dobles (para cadenas)
 *  - Cualquier símbolo que forme parte de: operadores, puntuación, agrupación (config.json)
 *  - Prefijos de comentario (línea y bloque) definidos en config.json
 *
 * Esta política NO reconoce tokens; solo responde si en la posición actual puede
 * iniciar algún símbolo permitido. El reconocimiento/consumo lo hace el lexer.
 */
public final class AlphabetPolicy {

    /**
     * ¿El símbolo en la posición actual pertenece al alfabeto?
     * Se verifica de forma conservadora:
     *  - Si es letra, dígito, espacio o salto de línea ⇒ permitido.
     *  - Si es comilla doble ⇒ permitido (podría iniciar cadena).
     *  - Si coincide con el inicio de un operador/puntuación/agrupación (greedy) ⇒ permitido.
     *  - Si inicia un comentario de línea o bloque ⇒ permitido.
     * En caso contrario ⇒ fuera del alfabeto.
     */
    public boolean isAllowedAt(CharCursor cursor,
                               Config config,
                               OperatorTable operators,
                               OperatorTable punctuation,
                               OperatorTable grouping) {
        if (cursor == null || cursor.eof()) return true; // EOF no se reporta como símbolo inválido
        int c = cursor.peek();

        // Base del alfabeto
        if (CharClasses.isLetter(c) || CharClasses.isDigit(c) || CharClasses.isSpaceOrNewline(c)) return true;
        if (CharClasses.isQuote(c)) return true;

        // Símbolos configurados
        if (operators != null && operators.longestMatch(cursor) != null) return true;
        if (punctuation != null && punctuation.longestMatch(cursor) != null) return true;
        if (grouping != null && grouping.longestMatch(cursor) != null) return true;

        // Comentarios
        CommentsConfig com = (config != null) ? config.getComentarios() : null;
        if (com != null) {
            if (startsWith(cursor, com.getLinea())) return true;
            if (startsWith(cursor, com.getBloqueInicio())) return true;
            // (bloqueFin no debería iniciar lexema normalmente, pero no hace daño contemplarlo)
            if (startsWith(cursor, com.getBloqueFin())) return true;
        }

        return false;
    }

    /** ¿El texto a partir del cursor inicia exactamente con 's'? (sin consumir). */
    private boolean startsWith(CharCursor cursor, String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            int ch = cursor.peek(i);
            if (ch < 0 || (char) ch != s.charAt(i)) return false;
        }
        return true;
    }
}
