package model.report;

import java.util.Objects;

import model.lexical.Position;
import model.lexical.TokenType;

/**
 * Fila para la tabla de tokens:
 *  - Nombre del token (TokenType)
 *  - Lexema
 *  - Posición (línea/columna) del INICIO del lexema
 *
 * La guía pide mostrar estos tres campos cuando NO hay errores.
 */
public final class TokenRow {
    private final TokenType nombreToken;
    private final String lexema;
    private final Position posicion;

    public TokenRow(TokenType nombreToken, String lexema, Position posicion) {
        this.nombreToken = Objects.requireNonNull(nombreToken, "nombreToken no puede ser null");
        this.lexema = Objects.requireNonNull(lexema, "lexema no puede ser null");
        this.posicion = Objects.requireNonNull(posicion, "posicion no puede ser null");
    }

    public TokenType nombreToken() { return nombreToken; }
    public String lexema() { return lexema; }
    public Position posicion() { return posicion; }
}
