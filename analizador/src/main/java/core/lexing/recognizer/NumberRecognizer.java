package core.lexing.recognizer;

import core.lexing.stream.CharClasses;
import core.lexing.stream.CharCursor;

/**
 * Reconocedor de NÚMEROS ENTEROS.
 *
 * Regla (según la práctica):
 *  - Un número entero es una o más cifras [0-9]+.
 *
 * Casos de error que la práctica ejemplifica:
 *  - "Número mal formado" cuando tras una secuencia de dígitos aparece
 *    inmediatamente una LETRA (p. ej., "585f3.40" ⇒ el error es "585f").
 *    En este caso, el reconocedor devuelve un match CON ERROR cuya longitud
 *    incluye el primer carácter inválido (la 'f' en el ejemplo).
 *
 * Importante:
 *  - NO consume del cursor (solo calcula longitud).
 *  - NO gestiona decimales (eso lo hace DecimalRecognizer). El lexer deberá
 *    invocar primero DecimalRecognizer y luego NumberRecognizer para evitar
 *    que un decimal válido sea tomado como entero.
 *  - Sin regex; todo char-a-char con peek(k).
 */
public final class NumberRecognizer {

    private static final String MSG_MAL_FORMADO = "Número mal formado";

    /**
     * Intenta reconocer un número entero o el caso de error indicado.
     *
     * @param cursor flujo de caracteres (no se consume aquí)
     * @return Recognition:
     *   - matched=false si el primer char no es dígito.
     *   - matched=true sin error: longitud = cantidad de dígitos.
     *   - matched=true con error: si el char posterior a los dígitos es LETRA,
     *       longitud = dígitos + 1 (incluye la primera letra), mensaje "Número mal formado".
     */
    public Recognition recognize(CharCursor cursor) {
        if (cursor == null || cursor.eof()) return Recognition.noMatch();

        int c0 = cursor.peek();
        if (!CharClasses.isDigit(c0)) {
            return Recognition.noMatch();
        }

        // Consumimos dígitos en lookahead
        int len = 1;
        while (true) {
            int c = cursor.peek(len);
            if (c == CharCursor.EOF) break;
            if (CharClasses.isDigit(c)) {
                len++;
            } else {
                break;
            }
        }

        // Si el primer no-dígito inmediatamente después es una LETRA, es error "número mal formado".
        int next = cursor.peek(len);
        if (next != CharCursor.EOF && CharClasses.isLetter(next)) {
            // Incluimos únicamente la primera letra inválida en el lexema del error
            return Recognition.error(len + 1, buildErrorLexemeLength(len + 1), MSG_MAL_FORMADO);
        }

        // Número entero válido
        return Recognition.match(len);
    }

    /**
     * Construye un "símbolo o cadena de error" genérico para el reporte, dado
     * que el reconocedor no consume ni guarda substring. Para mantener bajo
     * acoplamiento, retornamos un placeholder que indica la longitud capturada.
     * La materialización del lexema exacto la hará el lexer al consumir.
     */
    private String buildErrorLexemeLength(int length) {
        return "<lexema:" + length + ">";
    }
}
