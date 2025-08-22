package model.lexical;

import java.util.Objects;

/**
 * Representa un token reconocido en el texto de entrada.
 *
 * Contiene:
 * - tipo: uno de los definidos en {@link TokenType}.
 * - lexema: la secuencia exacta de caracteres tal y como aparece en el texto.
 * - posicion: la posición (fila/columna) del inicio del lexema en el texto.
 */
public record Token(TokenType tipo, String lexema, Position posicion) {

    public Token {
        Objects.requireNonNull(tipo, "El tipo de token no puede ser null");
        Objects.requireNonNull(lexema, "El lexema no puede ser null");
        Objects.requireNonNull(posicion, "La posición no puede ser null");
    }
}
