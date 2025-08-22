package core.lexing.recognizer;

/**
 * Resultado de un intento de reconocimiento desde la posición actual del cursor,
 * sin consumir caracteres del flujo.
 *
 * Convenciones:
 *  - matched == false  => no hay coincidencia; length = 0; hasError = false.
 *  - matched == true   => hay coincidencia; length > 0; puede o no haber error.
 *  - hasError == true  => se detectó un problema en el lexema reconocido (p. ej., bloque sin cierre).
 *                         En este caso, el llamador debe registrar el error y decidir la recuperación.
 *
 * Importante:
 *  - length indica CUÁNTOS caracteres debería consumir el llamador si decide avanzar.
 *  - Para comentarios de bloque sin cierre, length suele abarcar desde el inicio del
 *    comentario hasta EOF (ya que no existe cierre).
 */
public final class Recognition {

    private final boolean matched;
    private final int length;
    private final boolean hasError;
    private final String errorMessage;
    private final String errorLexeme; // símbolo o cadena asociada al error (para reporte)

    private Recognition(boolean matched, int length, boolean hasError, String errorMessage, String errorLexeme) {
        this.matched = matched;
        this.length = length;
        this.hasError = hasError;
        this.errorMessage = errorMessage;
        this.errorLexeme = errorLexeme;
    }

    /** No hubo match alguno. */
    public static Recognition noMatch() {
        return new Recognition(false, 0, false, null, null);
    }

    /** Hubo match válido (sin error). */
    public static Recognition match(int length) {
        if (length <= 0) throw new IllegalArgumentException("La longitud de un match debe ser > 0.");
        return new Recognition(true, length, false, null, null);
    }

    /** Hubo match pero se detectó un error sobre el lexema reconocido. */
    public static Recognition error(int length, String errorLexeme, String errorMessage) {
        if (length <= 0) throw new IllegalArgumentException("La longitud de un match debe ser > 0.");
        if (errorMessage == null) throw new IllegalArgumentException("El mensaje de error no puede ser null.");
        return new Recognition(true, length, true, errorMessage, errorLexeme);
    }

    /** ¿Se reconoció el patrón desde la posición actual? */
    public boolean matched() { return matched; }

    /** Longitud del prefijo reconocido (caracteres a consumir si se avanza). */
    public int length() { return length; }

    /** ¿El reconocimiento trae un error asociado? (p. ej., bloque no cerrado) */
    public boolean hasError() { return hasError; }

    /** Mensaje del error (en español), si existe. */
    public String errorMessage() { return errorMessage; }

    /** Símbolo o cadena asociada al error (para reporte), si existe. */
    public String errorLexeme() { return errorLexeme; }
}
