package core.lexing.recognizer;

import core.lexing.stream.CharCursor;
import model.config.CommentsConfig;

/**
 * Reconoce un comentario de BLOQUE:
 *   - Debe iniciar con 'comentarios.bloqueInicio' y terminar con 'comentarios.bloqueFin'.
 *   - Si NO se encuentra el delimitador de cierre hasta EOF, se reporta un ERROR:
 *       mensaje: "Comentario de bloque no cerrado"
 *       errorLexeme: el delimitador de apertura (para el reporte de errores).
 *
 * Este reconocedor NO consume caracteres; únicamente calcula la longitud del
 * match (o hasta EOF en el caso de error) para que el llamador decida consumir.
 *
 * Observación:
 *  - Se realiza una búsqueda ingenua del sufijo de cierre, char a char, coherente con
 *    la restricción de trabajar sin regex ni funciones avanzadas de strings.
 */
public final class BlockCommentRecognizer {

    private static final String MSG_UNCLOSED = "Comentario de bloque no cerrado";

    /**
     * Intenta reconocer un comentario de bloque en la posición actual del cursor.
     *
     * @param cursor flujo de caracteres (no se consume aquí)
     * @param cfg    configuración de comentarios (prefijos/sufijos)
     * @return Recognition:
     *   - matched=false si NO comienza con el prefijo de comentario de bloque.
     *   - matched=true y sin error: longitud incluye apertura, contenido y cierre.
     *   - matched=true y con error: longitud desde la apertura hasta EOF (sin cierre).
     */
    public Recognition recognize(CharCursor cursor, CommentsConfig cfg) {
        if (cursor == null || cursor.eof()) return Recognition.noMatch();
        if (cfg == null) return Recognition.noMatch();

        String open = cfg.getBloqueInicio();
        String close = cfg.getBloqueFin();

        if (open == null || close == null || open.isEmpty() || close.isEmpty()) {
            return Recognition.noMatch();
        }
        if (!startsWith(cursor, open)) {
            return Recognition.noMatch();
        }

        // Buscamos el primer cierre 'close' char a char.
        int offset = open.length();

        while (true) {
            int c = cursor.peek(offset);
            if (c == CharCursor.EOF) {
                // No hubo cierre: se reporta error y la longitud consumirá hasta EOF.
                int lengthToEof = offset; // desde inicio del bloque hasta EOF
                return Recognition.error(lengthToEof, open, MSG_UNCLOSED);
            }
            // ¿En la posición actual comienza el sufijo de cierre?
            if (startsWith(cursor, close, offset)) {
                // Longitud total: apertura + contenido + cierre
                int total = offset + close.length();
                return Recognition.match(total);
            }
            offset++;
        }
    }

    /** ¿El texto a partir del cursor inicia exactamente con 's'? (sin consumir). */
    private boolean startsWith(CharCursor cursor, String s) {
        return startsWith(cursor, s, 0);
    }

    /** ¿El texto a partir de cursor.peek(offset) inicia exactamente con 's'? (sin consumir). */
    private boolean startsWith(CharCursor cursor, String s, int offset) {
        for (int i = 0; i < s.length(); i++) {
            int c = cursor.peek(offset + i);
            if (c == CharCursor.EOF || (char) c != s.charAt(i)) return false;
        }
        return true;
    }
}
