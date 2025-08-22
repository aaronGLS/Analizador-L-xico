package core.lexing.recognizer;

import core.lexing.stream.CharClasses;
import core.lexing.stream.CharCursor;

/**
 * Reconocedor de NÚMEROS DECIMALES.
 *
 * Regla (según la práctica):
 *  - Un decimal válido es: dígitos '.' dígitos+
 *    (es OBLIGATORIO al menos un dígito tras el punto).
 *
 * Casos de error cubiertos:
 *  - "Decimal mal formado: faltan dígitos" cuando se encuentra "dígitos '.'" y
 *    NO hay dígito inmediatamente después del punto (p. ej., "12.").
 *
 * Importante:
 *  - NO consume del cursor (solo calcula longitud).
 *  - NO usa regex; char-a-char con peek(k).
 *  - No intenta validar segundos puntos u otras variaciones; si tras el punto
 *    no hay dígito directo, reporta el error de la guía y regresa longitud hasta el '.'.
 */
public final class DecimalRecognizer {

    private static final String MSG_FALTAN_DIGITOS = "Decimal mal formado: faltan dígitos";

    /**
     * Intenta reconocer un número decimal válido o el caso de error "12.".
     *
     * @param cursor flujo de caracteres (no se consume aquí)
     * @return Recognition:
     *   - matched=false si no empieza con dígitos seguidos de '.'.
     *   - matched=true sin error: longitud = dígitosAntes + 1 (punto) + dígitosDespués(>=1).
     *   - matched=true con error: si no hay dígito tras '.', longitud = dígitosAntes + 1.
     */
    public Recognition recognize(CharCursor cursor) {
        if (cursor == null || cursor.eof()) return Recognition.noMatch();

        int i = 0;
        int c0 = cursor.peek(i);
        if (!CharClasses.isDigit(c0)) return Recognition.noMatch();

        // 1) dígitos antes del punto
        int countBefore = 1;
        while (true) {
            int c = cursor.peek(i + countBefore);
            if (c == CharCursor.EOF || !CharClasses.isDigit(c)) break;
            countBefore++;
        }

        // Debe haber un punto a continuación
        int dot = cursor.peek(i + countBefore);
        if (dot != '.') return Recognition.noMatch();

        // 2) validar al menos UN dígito después del punto
        int firstAfter = cursor.peek(i + countBefore + 1);
        if (firstAfter == CharCursor.EOF || !CharClasses.isDigit(firstAfter)) {
            // Error "12."
            int errorLen = countBefore + 1; // incluye el '.'
            return Recognition.error(errorLen, "<lexema:" + errorLen + ">", MSG_FALTAN_DIGITOS);
        }

        // 3) consumir dígitos después del punto
        int countAfter = 1; // ya contamos el primero
        while (true) {
            int c = cursor.peek(i + countBefore + 1 + countAfter);
            if (c == CharCursor.EOF || !CharClasses.isDigit(c)) break;
            countAfter++;
        }

        int total = countBefore + 1 + countAfter;
        return Recognition.match(total);
    }
}
