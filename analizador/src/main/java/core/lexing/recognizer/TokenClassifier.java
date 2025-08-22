package core.lexing.recognizer;

import core.lexing.table.ReservedWords;
import model.lexical.TokenType;

import java.util.Objects;

/**
 * Clasificador de lexemas ya reconocidos como IDENTIFICADOR:
 * decide si el lexema es una palabra reservada EXACTA (case-sensitive) o no.
 *
 * Responsabilidad única:
 *  - Dado un lexema (String) y la tabla de reservadas, devolver el TokenType correcto:
 *      RESERVED_WORD si está en la tabla, en otro caso IDENTIFIER.
 *
 * No valida la forma del identificador ni modifica el lexema.
 * No interactúa con el cursor ni con Config directamente.
 */
public final class TokenClassifier {

    private final ReservedWords reserved;

    /**
     * @param reserved tabla de palabras reservadas (case-sensitive) creada desde config.json
     */
    public TokenClassifier(ReservedWords reserved) {
        this.reserved = Objects.requireNonNull(reserved, "ReservedWords no puede ser null");
    }

    /**
     * Clasifica un lexema que ya fue reconocido con la forma de IDENTIFICADOR.
     * @param lexeme lexema tal cual fue leído del texto (no null)
     * @return TokenType.RESERVED_WORD o TokenType.IDENTIFIER
     * @throws IllegalArgumentException si lexeme es null
     */
    public TokenType classifyIdentOrReserved(String lexeme) {
        if (lexeme == null) {
            throw new IllegalArgumentException("El lexema no puede ser null.");
        }
        return reserved.isReserved(lexeme)
                ? TokenType.RESERVED_WORD
                : TokenType.IDENTIFIER;
    }
}
