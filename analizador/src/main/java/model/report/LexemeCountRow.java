package model.report;

import java.util.Objects;

import model.lexical.TokenType;

/**
 * Fila para el "Recuento de lexemas" (solo se muestra si NO hay errores):
 *  - Lexema exacto
 *  - Tipo de token (TokenType)
 *  - Cantidad (ocurrencias)
 */
public final class LexemeCountRow {
    private final String lexema;
    private final TokenType tipo;
    private final int cantidad;

    public LexemeCountRow(String lexema, TokenType tipo, int cantidad) {
        this.lexema = Objects.requireNonNull(lexema, "lexema no puede ser null");
        this.tipo = Objects.requireNonNull(tipo, "tipo no puede ser null");
        if (cantidad < 0) throw new IllegalArgumentException("cantidad no puede ser negativa");
        this.cantidad = cantidad;
    }

    public String lexema() { return lexema; }
    public TokenType tipo() { return tipo; }
    public int cantidad() { return cantidad; }
}
