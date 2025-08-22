package core.lexing.recognizer;

import core.lexing.stream.CharClasses;
import core.lexing.stream.CharCursor;

/**
 * Reconocedor de IDENTIFICADORES.
 *
 * Regla (según la práctica):
 *  - Debe iniciar con una letra [A–Z a–z].
 *  - Puede continuar con letras o dígitos [A–Z a–z 0–9].
 *
 * Importante:
 *  - NO consume del cursor. Únicamente calcula la longitud del lexema reconocido.
 *  - NO clasifica como reservada; esa decisión se delega a TokenClassifier.
 *  - NO usa regex ni utilidades avanzadas de cadena; sólo peek(k).
 *  - NO considera '_' u otros símbolos como parte del identificador (la guía sólo
 *    acepta letras/dígitos).
 */
public final class IdentifierRecognizer {

    /**
     * Intenta reconocer un identificador comenzando en la posición actual.
     *
     * @param cursor flujo de caracteres (no se consume aquí)
     * @return Recognition:
     *   - matched=false si el primer char no es letra.
     *   - matched=true  y length >= 1 si es un identificador válido (letra (letra|dígito)*).
     */
    public Recognition recognize(CharCursor cursor) {
        if (cursor == null || cursor.eof()) return Recognition.noMatch();

        int c0 = cursor.peek();
        if (!CharClasses.isLetter(c0)) {
            return Recognition.noMatch();
        }

        int len = 1; // ya contamos la primera letra
        while (true) {
            int c = cursor.peek(len);
            if (c == CharCursor.EOF) break;
            if (CharClasses.isLetter(c) || CharClasses.isDigit(c)) {
                len++;
            } else {
                break;
            }
        }
        return Recognition.match(len);
    }
}
