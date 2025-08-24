package service;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import core.lexing.LexerEngine;
import core.lexing.recognizer.BlockCommentRecognizer;
import core.lexing.recognizer.LineCommentRecognizer;
import core.lexing.recognizer.Recognition;
import core.lexing.stream.CharCursor;
import model.config.Config;
import model.lexical.LexError;
import model.lexical.Position;
import model.lexical.Token;
import model.lexical.TokenType;

/**
 * Servicio responsable de generar las instrucciones de coloreo
 * (resaltado) para el texto de entrada.  Utiliza el {@link LexerEngine}
 * para obtener tokens y errores, y escanea manualmente los comentarios
 * para pintarlos con color especial.
 */
public final class HighlightService {

    /** Representa un rango a colorear dentro del texto. */
    public record HighlightSpan(int start, int end, Color color) {
        public HighlightSpan {
            if (start < 0 || end < start)
                throw new IllegalArgumentException("Rango inválido");
            Objects.requireNonNull(color, "color no puede ser null");
        }
    }

    private final Config config;
    private final LineCommentRecognizer lineComment = new LineCommentRecognizer();
    private final BlockCommentRecognizer blockComment = new BlockCommentRecognizer();

    public HighlightService(Config config) {
        this.config = Objects.requireNonNull(config, "config no puede ser null");
    }

    /**
     * Calcula los rangos de coloreo para el texto dado.
     *
     * @param text contenido a analizar
     * @return lista inmutable de rangos coloreados
     */
    public List<HighlightSpan> highlight(String text) {
        Objects.requireNonNull(text, "text no puede ser null");

        // 1) Analizar texto para obtener tokens y errores
        var lexer = new LexerEngine(config);
        var result = lexer.analyze(text);

        // 2) Precalcular inicios de línea para traducir Position -> índice
        int[] lineStarts = computeLineStarts(text);

        List<HighlightSpan> spans = new ArrayList<>();

        // 3) Tokens válidos
        for (Token t : result.tokens()) {
            int start = positionToIndex(lineStarts, t.posicion());
            int end = start + t.lexema().length();
            spans.add(new HighlightSpan(start, end, colorFor(t.tipo())));
        }

        // 4) Errores léxicos
        for (LexError e : result.errors()) {
            int start = positionToIndex(lineStarts, e.posicion());
            int end = start + e.simboloOCadena().length();
            spans.add(new HighlightSpan(start, end, COLOR_ERROR));
        }

        // 5) Comentarios (no incluidos en tokens)
        markComments(text, spans);

        return Collections.unmodifiableList(spans);
    }

    /* ===================== helpers internos ===================== */

    private void markComments(String text, List<HighlightSpan> spans) {
        var cfg = config.getComentarios();
        if (cfg == null)
            return;

        var cursor = new CharCursor(text);
        while (!cursor.eof()) {
            int start = cursor.index();
            Recognition r = lineComment.recognize(cursor, cfg);
            if (r.matched()) {
                spans.add(new HighlightSpan(start, start + r.length(), COLOR_COMMENT));
                consume(cursor, r.length());
                continue;
            }
            r = blockComment.recognize(cursor, cfg);
            if (r.matched()) {
                spans.add(new HighlightSpan(start, start + r.length(), COLOR_COMMENT));
                consume(cursor, r.length());
                continue;
            }
            cursor.next();
        }
    }

    private static void consume(CharCursor cursor, int len) {
        for (int i = 0; i < len && !cursor.eof(); i++) cursor.next();
    }

    private static int[] computeLineStarts(String text) {
        int n = text.length();
        int[] tmp = new int[n + 1];
        int count = 0;
        tmp[count++] = 0;
        for (int i = 0; i < n; i++) {
            char ch = text.charAt(i);
            if (ch == '\r') {
                int next = i + 1;
                if (next < n && text.charAt(next) == '\n') {
                    tmp[count++] = next + 1;
                    i = next;
                } else {
                    tmp[count++] = next;
                }
            } else if (ch == '\n') {
                tmp[count++] = i + 1;
            }
        }
        int[] starts = new int[count];
        System.arraycopy(tmp, 0, starts, 0, count);
        return starts;
    }

    private static int positionToIndex(int[] lineStarts, Position pos) {
        int line = pos.linea();
        int col = pos.columna();
        if (line < 1 || line > lineStarts.length)
            return 0;
        int base = lineStarts[line - 1];
        return base + Math.max(0, col - 1);
    }

    private static Color colorFor(TokenType type) {
        return switch (type) {
            case RESERVED_WORD -> COLOR_RESERVED;
            case IDENTIFIER -> COLOR_IDENTIFIER;
            case NUMBER -> COLOR_NUMBER;
            case STRING -> COLOR_STRING;
            case DECIMAL -> COLOR_DECIMAL;
            case OPERATOR -> COLOR_OPERATOR;
            case GROUPING -> COLOR_GROUPING;
            case PUNCTUATION -> COLOR_DECIMAL; // negro
        };
    }

    // Paleta básica según la guía
    private static final Color COLOR_RESERVED  = Color.BLUE;
    private static final Color COLOR_IDENTIFIER = new Color(101, 67, 33); // café
    private static final Color COLOR_NUMBER    = Color.GREEN;
    private static final Color COLOR_STRING    = Color.ORANGE;
    private static final Color COLOR_DECIMAL   = Color.BLACK;
    private static final Color COLOR_COMMENT   = new Color(0, 100, 0); // verde oscuro
    private static final Color COLOR_OPERATOR  = Color.YELLOW;
    private static final Color COLOR_GROUPING  = new Color(128, 0, 128); // morado
    private static final Color COLOR_ERROR     = Color.RED;
}

