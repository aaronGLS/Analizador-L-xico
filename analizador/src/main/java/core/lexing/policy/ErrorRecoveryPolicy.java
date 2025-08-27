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
     * Construye un {@link LexError} usando directamente el lexema ya preparado.
     *
     * @param lexeme   lexema que ocasionó el error (puede ser null)
     * @param position posición 1-based (línea/columna) del inicio del lexema
     * @param message  mensaje de error en español (no null)
     * @return instancia de {@link LexError}
     */
    public LexError buildLexError(String lexeme, Position position, String message) {
        if (position == null) throw new IllegalArgumentException("position no puede ser null.");
        if (message == null) throw new IllegalArgumentException("message no puede ser null.");

        String lex = (lexeme != null) ? lexeme : "";
        return new LexError(lex, position, message);
    }
}
