package service;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import core.highlight.ColorPalette;
import core.lexing.LexerEngine;
import model.config.Config;
import model.lexical.Position;
import model.lexical.Token;
import model.lexical.TokenType;

/**
 * Servicio responsable de generar las instrucciones de coloreo
 * (resaltado) para el texto de entrada.  Utiliza el {@link LexerEngine}
 * para obtener tokens (incluyendo comentarios y errores) y generar
 * instrucciones de coloreo.
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

        // Normalizar saltos de línea para tratar CR, LF y CRLF como '\n'
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');

        // 1) Analizar texto para obtener tokens (incluyen comentarios y errores)
        var lexer = new LexerEngine(config);
        var result = lexer.analyze(normalized);

        // 2) Precalcular inicios de línea para traducir Position -> índice
        int[] lineStarts = computeLineStarts(normalized);

        List<HighlightSpan> spans = new ArrayList<>();

        // 3) Tokens
        for (Token t : result.tokens()) {
            int start = positionToIndex(lineStarts, t.posicion());
            int end = start + t.lexema().length();
            spans.add(new HighlightSpan(start, end, colorFor(t.tipo())));
        }

        return Collections.unmodifiableList(spans);
    }

    /* ===================== helpers internos ===================== */

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
            case RESERVED_WORD -> ColorPalette.RESERVED;
            case IDENTIFIER -> ColorPalette.IDENTIFIER;
            case NUMBER -> ColorPalette.NUMBER;
            case STRING -> ColorPalette.STRING;
            case DECIMAL -> ColorPalette.DECIMAL;
            case OPERATOR -> ColorPalette.OPERATOR;
            case GROUPING -> ColorPalette.GROUPING;
            case PUNCTUATION -> ColorPalette.PUNCTUATION;
            case COMMENT -> ColorPalette.COMMENT;
            case ERROR -> ColorPalette.ERROR;
        };
    }
}

