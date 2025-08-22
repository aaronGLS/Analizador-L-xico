package core.lexing.recognizer;

import core.lexing.stream.CharCursor;
import model.config.CommentsConfig;

/**
 * Reconoce un comentario de LÍNEA:
 *   - Debe iniciar exactamente con el prefijo 'comentarios.linea' (config.json).
 *   - Consume desde el prefijo hasta antes del salto de línea (CR, LF o CRLF).
 *   - Los comentarios se IGNORAN en el análisis (no generan tokens).
 *
 * Este reconocedor NO consume caracteres; únicamente calcula la longitud del
 * lexema reconocido, para que el llamador decida consumir dicha longitud.
 *
 * Errores:
 *  - Un comentario de línea no produce errores por definición (no requiere cierre).
 */
public final class LineCommentRecognizer {

    /**
     * Intenta reconocer un comentario de línea en la posición actual del cursor.
     *
     * @param cursor  flujo de caracteres (no se consume aquí)
     * @param cfg     configuración de comentarios (prefijos/sufijos)
     * @return Recognition:
     *   - matched=false si NO comienza con el prefijo de comentario de línea.
     *   - matched=true  y length > 0 si SÍ hay comentario de línea (sin error).
     */
    public Recognition recognize(CharCursor cursor, CommentsConfig cfg) {
        if (cursor == null || cursor.eof()) return Recognition.noMatch();
        if (cfg == null || cfg.getLinea() == null || cfg.getLinea().isEmpty()) return Recognition.noMatch();

        String prefix = cfg.getLinea();
        if (!startsWith(cursor, prefix)) {
            return Recognition.noMatch();
        }

        // Calcula longitud: prefijo + contenido hasta antes del salto de línea/EOF.
        int offset = prefix.length();

        while (true) {
            int c = cursor.peek(offset);
            if (c == CharCursor.EOF) break;
            if (c == '\r') {
                // Si es CRLF, no incluimos el '\n'
                if (cursor.peek(offset + 1) == '\n') {
                    // Comentario termina antes de CRLF (no se incluyen saltos)
                }
                break;
            }
            if (c == '\n') {
                break;
            }
            offset++;
        }

        return Recognition.match(offset);
    }

    /** ¿El texto a partir del cursor inicia exactamente con 's'? (sin consumir). */
    private boolean startsWith(CharCursor cursor, String s) {
        for (int i = 0; i < s.length(); i++) {
            int c = cursor.peek(i);
            if (c == CharCursor.EOF || (char) c != s.charAt(i)) return false;
        }
        return true;
    }
}
