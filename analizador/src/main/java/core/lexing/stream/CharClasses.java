package core.lexing.stream;

/**
 * Utilidades mínimas de clasificación de caracteres.
 *
 * Importante:
 *  - La práctica delimita el alfabeto permitido a: letras a–z/A–Z, dígitos 0–9,
 *    espacio y salto de línea (puntuación/operadores/agrupación se leen desde config.json).
 *  - Aquí NO se valida alfabeto permitido (eso es responsabilidad de otra capa),
 *    únicamente se proveen helpers básicos para construir el léxico.
 *  - No usa regex ni APIs de reconocimiento complejo; solo comparaciones char-a-char.
 */
public final class CharClasses {

    private CharClasses() { /* utilitaria: no instanciable */ }

    /** ¿Es una letra ASCII (A–Z o a–z)? */
    public static boolean isLetter(int c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    /** ¿Es un dígito ASCII (0–9)? */
    public static boolean isDigit(int c) {
        return (c >= '0' && c <= '9');
    }

    /**
     * ¿Es un espacio o un salto de línea? (los únicos whitespaces del alfabeto).
     * Nota: '\t' (tabulación) NO está contemplado por la guía como alfabeto permitido;
     * su tratamiento corresponde a la política de alfabeto en otra capa.
     */
    public static boolean isSpaceOrNewline(int c) {
        return c == ' ' || c == '\n' || c == '\r';
    }

    /** ¿Es comilla doble? */
    public static boolean isQuote(int c) {
        return c == '"';
    }
}
