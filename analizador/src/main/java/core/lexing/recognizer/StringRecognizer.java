package core.lexing.recognizer;

import core.lexing.stream.CharClasses;
import core.lexing.stream.CharCursor;

/**
 * Reconocedor de CADENAS entre comillas dobles.
 *
 * Regla (según la práctica):
 *  - Una cadena inicia con '"' y termina con '"' (cierre obligatorio).
 *  - El contenido puede contener letras, dígitos y espacios/saltos de línea
 *    (otros símbolos del alfabeto permitido se validarán en otra capa:
 *     AlphabetPolicy; aquí no se filtra, sólo se busca el cierre).
 *
 * Caso de error cubierto:
 *  - "Cadena no cerrada": si se llega a EOF sin encontrar la comilla de cierre.
 *
 * Importante:
 *  - NO consume del cursor (sólo calcula longitud).
 *  - NO usa regex; char-a-char con peek(k).
 *  - La verificación de "símbolo fuera del alfabeto" se maneja en otra política,
 *    no aquí (evitamos lógica innecesaria).
 */
public final class StringRecognizer {

    private static final String MSG_NO_CERRADA = "Cadena no cerrada";

    /**
     * Intenta reconocer una cadena delimitada por comillas.
     *
     * @param cursor flujo de caracteres (no se consume aquí)
     * @return Recognition:
     *   - matched=false si el primer char no es comilla doble ".
     *   - matched=true sin error: longitud = desde la comilla inicial hasta la comilla de cierre inclusive.
     *   - matched=true con error: si no hay comilla de cierre hasta EOF, longitud = desde inicio hasta EOF,
     *       con mensaje "Cadena no cerrada".
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
            len++;
        }
    }
}
