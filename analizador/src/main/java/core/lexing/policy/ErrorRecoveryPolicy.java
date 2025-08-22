package core.lexing.policy;

import model.lexical.LexError;
import model.lexical.Position;

/**
 * Política simple de recuperación de errores:
 *  - Construye el objeto LexError con el lexema exacto y el mensaje.
 *  - La política de avance es "consumir la longitud reconocida" para reanudar el análisis.
 *
 * Nota:
 *  - Esta política NO mueve el cursor; quien llama decide el consumo real.
 *  - No aplica reintentos ni backtracking; se apega a "emitir error y continuar".
 */
public final class ErrorRecoveryPolicy {

    /**
     * Construye un LexError con el fragmento de texto que produjo el error.
     *
     * @param sourceText  texto completo (no null)
     * @param startIndex  índice 0-based del inicio del lexema con error
     * @param length      longitud a tomar desde startIndex (acotada al tamaño del texto)
     * @param position    posición 1-based (línea/columna) del inicio del lexema
     * @param message     mensaje de error en español (no null)
     * @param fallbackLexeme si no es posible tomar el substring (o length<=0), usar este valor (puede ser null)
     * @return LexError con "símbolo o cadena de error", posición y mensaje
     */
    public LexError buildLexError(String sourceText, int startIndex, int length,
                                  Position position, String message, String fallbackLexeme) {
        if (sourceText == null) throw new IllegalArgumentException("sourceText no puede ser null.");
        if (position == null) throw new IllegalArgumentException("position no puede ser null.");
        if (message == null) throw new IllegalArgumentException("message no puede ser null.");

        String lexema;
        if (length > 0 && startIndex >= 0 && startIndex < sourceText.length()) {
            int end = Math.min(sourceText.length(), startIndex + length);
            lexema = sourceText.substring(startIndex, end);
        } else {
            lexema = (fallbackLexeme != null) ? fallbackLexeme : "";
        }
        return new LexError(lexema, position, message);
    }
}
