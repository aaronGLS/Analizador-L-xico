package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import model.lexical.LexError;
import model.lexical.Token;
import model.report.ErrorRow;
import model.report.LexemeCountRow;
import model.report.TokenRow;

/**
 * Construye las tablas que exige la práctica:
 * - Si hay ERRORES: mostrar SOLO "Reporte de errores".
 * - Si NO hay errores: mostrar "Tokens" y "Recuento de lexemas".
 *
 * Esta clase NO exporta ni formatea a CSV/HTML; solo arma las filas de datos.
 */
public final class ReportBuilder {

    public static final class Result {
        private final List<ErrorRow> errores;
        private final List<TokenRow> tokens;
        private final List<LexemeCountRow> recuento;

        private Result(List<ErrorRow> errores, List<TokenRow> tokens, List<LexemeCountRow> recuento) {
            this.errores = errores;
            this.tokens = tokens;
            this.recuento = recuento;
        }

        public List<ErrorRow> errores() {
            return errores;
        }

        public List<TokenRow> tokens() {
            return tokens;
        }

        public List<LexemeCountRow> recuento() {
            return recuento;
        }
    }

    private final StatsService statsService = new StatsService();

    /**
     * Regla de la guía:
     * - Si errors.size() > 0 ⇒ se muestra SOLO "Reporte de errores".
     * - Si errors.size() == 0 ⇒ se muestran "Tokens" y "Recuento de lexemas".
     */
    public Result build(List<Token> tokens, List<LexError> errors) {
        Objects.requireNonNull(tokens, "tokens no puede ser null");
        Objects.requireNonNull(errors, "errors no puede ser null");

        if (!errors.isEmpty()) {
            return new Result(buildErrorRows(errors),
                    List.of(),
                    List.of());
        }
        // Sin errores: mostrar tokens y recuento, EXCLUYENDO comentarios
        List<Token> tokensSinComentarios = new ArrayList<>();
        for (Token t : tokens) {
            if (t.tipo() != model.lexical.TokenType.COMMENT) {
                tokensSinComentarios.add(t);
            }
        }
        var tokenRows = buildTokenRows(tokensSinComentarios);
        var countRows = statsService.countByLexemeAndType(tokensSinComentarios);
        return new Result(List.of(), tokenRows, countRows);
    }

    /** Convierte la lista de errores a filas ErrorRow. */
    public List<ErrorRow> buildErrorRows(List<LexError> errors) {
        if (errors == null || errors.isEmpty())
            return List.of();
        List<ErrorRow> rows = new ArrayList<>(errors.size());
        for (LexError e : errors) {
            rows.add(new ErrorRow(e.simboloOCadena(), e.posicion(), e.mensaje()));
        }
        return Collections.unmodifiableList(rows);
    }

    /** Convierte la lista de tokens a filas TokenRow. */
    public List<TokenRow> buildTokenRows(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty())
            return List.of();
        List<TokenRow> rows = new ArrayList<>(tokens.size());
        for (Token t : tokens) {
            rows.add(new TokenRow(t.tipo(), t.lexema(), t.posicion()));
        }
        return Collections.unmodifiableList(rows);
    }
}
