package core.search;

import core.lexing.recognizer.*;
import core.lexing.stream.CharCursor;
import model.config.CommentsConfig;
import model.config.Config;
import model.lexical.Position;
import model.search.MatchRange;
import model.search.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Motor de búsqueda de patrones con conversión a posiciones (línea/columna).
 *
 * Funcionalidad:
 * - Busca un patrón (char-a-char, sin regex) con sensibilidad opcional.
 * - Opcionalmente excluye coincidencias dentro de comentarios (línea/bloque)
 * según la configuración (config.json).
 * - Calcula posiciones 1-based para inicio y fin de cada coincidencia,
 * tratando CR, LF y CRLF como saltos de línea válidos.
 *
 * Sin UI, sin coloreo: este motor provee datos para que la capa de vista
 * resalte.
 */
public final class SearchEngine {

    private final LineCommentRecognizer lineComment = new LineCommentRecognizer();
    private final BlockCommentRecognizer blockComment = new BlockCommentRecognizer();

    /**
     * Ejecuta una búsqueda de patrón en {@code text}.
     *
     * @param text            texto donde buscar (no null)
     * @param pattern         patrón a buscar (no null ni vacío)
     * @param caseSensitive   true para respetar mayúsculas/minúsculas
     * @param wholeWord       true para coincidir solo palabras completas
     * @param includeComments true para incluir coincidencias dentro de comentarios;
     *                        false para excluirlas (si hay config de comentarios)
     * @param config          configuración (se usa solo si includeComments=false;
     *                        puede ser null)
     * @return SearchResult con rangos y posiciones de cada coincidencia
     */
    public SearchResult search(String text,
            String pattern,
            boolean caseSensitive,
            boolean wholeWord,
            boolean includeComments,
            Config config) {
        Objects.requireNonNull(text, "El texto no puede ser null.");
        Objects.requireNonNull(pattern, "El patrón no puede ser null.");
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("El patrón de búsqueda no puede ser vacío.");
        }

        // 1) Precalcular líneas (para mapear índice -> (línea, columna))
        int[] lineStarts = computeLineStarts(text);

        // 2) Si se deben excluir comentarios, construir la máscara de comentarios
        boolean[] inComment = includeComments ? null
                : buildCommentMask(text, (config != null) ? config.getComentarios() : null);

        // 3) Buscar todas las coincidencias (índices y longitudes)
        List<int[]> spans = PatternScanner.findAllCodePoints(text, pattern, caseSensitive, wholeWord);

        // 4) Filtrar por comentarios (si corresponde) y construir rangos con posiciones
        var ranges = new ArrayList<MatchRange>(spans.size());
        for (int[] sp : spans) {
            int start = sp[0];
            int len = sp[1];
            int end = start + len - 1;

            if (inComment != null && overlapsComment(inComment, start, end)) {
                continue; // excluir coincidencias dentro de comentarios
            }

            Position pStart = indexToPosition(lineStarts, start);
            Position pEnd = indexToPosition(lineStarts, end);
            ranges.add(new MatchRange(start, end, pStart, pEnd));
        }

        return new SearchResult(ranges);
    }

    /* ---------------------- utilitarios internos ---------------------- */

    /**
     * Construye arreglo de inicios de línea (0-based) tratando CR/LF/CRLF como
     * saltos.
     */
    private static int[] computeLineStarts(String text) {
        // Peor caso: cada carácter inicia una línea => tamaño = length + 1
        int n = text.length();
        int[] tmp = new int[n + 1];
        int count = 0;
        tmp[count++] = 0; // primera línea siempre inicia en 0

        for (int i = 0; i < n; i++) {
            char ch = text.charAt(i);
            if (ch == '\r') {
                // CR: inicio de nueva línea es i+1
                int next = i + 1;
                // Si es CRLF, considerarlo un solo salto (la siguiente línea empieza en i+2)
                if (next < n && text.charAt(next) == '\n') {
                    tmp[count++] = next + 1;
                    i = next; // saltar el '\n'
                } else {
                    tmp[count++] = next;
                }
            } else if (ch == '\n') {
                tmp[count++] = i + 1;
            }
        }
        int[] lineStarts = new int[count];
        System.arraycopy(tmp, 0, lineStarts, 0, count);
        return lineStarts;
    }

    /** Convierte índice 0-based a (línea, columna) 1-based usando lineStarts. */
    private static Position indexToPosition(int[] lineStarts, int index) {
        int line = binarySearchLine(lineStarts, index);
        int lineStart = lineStarts[line];
        int col = index - lineStart + 1;
        return new Position(line + 1, col);
    }

    /** Devuelve la línea (0-based) que contiene al índice dado. */
    private static int binarySearchLine(int[] lineStarts, int index) {
        int lo = 0, hi = lineStarts.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int v = lineStarts[mid];
            if (v == index)
                return mid;
            if (v < index)
                lo = mid + 1;
            else
                hi = mid - 1;
        }
        return Math.max(0, lo - 1);
    }

    /**
     * Construye una máscara booleana donde {@code true} indica "posición dentro de
     * comentario".
     */
    private boolean[] buildCommentMask(String text, CommentsConfig cfg) {
        int n = text.length();
        boolean[] mask = new boolean[n];
        if (cfg == null)
            return mask;

        var cursor = new CharCursor(text);
        while (!cursor.eof()) {
            int start = cursor.index();

            // Comentario de línea
            Recognition r = lineComment.recognize(cursor, cfg);
            if (r.matched()) {
                mark(mask, start, r.length());
                consume(cursor, r.length());
                continue;
            }

            // Comentario de bloque (si no cierra, length llega a EOF)
            r = blockComment.recognize(cursor, cfg);
            if (r.matched()) {
                mark(mask, start, r.length());
                consume(cursor, r.length());
                continue;
            }

            cursor.next(); // avanzar 1
        }
        return mask;
    }

    /**
     * Marca en 'mask' el intervalo [start, start+length) como dentro de comentario.
     */
    private static void mark(boolean[] mask, int start, int length) {
        int n = mask.length;
        int end = Math.min(n, start + Math.max(0, length));
        for (int i = Math.max(0, start); i < end; i++) {
            mask[i] = true;
        }
    }

    /** ¿[start..end] (inclusive) se solapa con posiciones marcadas en mask? */
    private static boolean overlapsComment(boolean[] mask, int start, int end) {
        int n = mask.length;
        int a = Math.max(0, start);
        int b = Math.min(n - 1, end);
        for (int i = a; i <= b; i++) {
            if (mask[i])
                return true;
        }
        return false;
    }

    private static void consume(CharCursor cursor, int length) {
        for (int i = 0; i < length && !cursor.eof(); i++)
            cursor.next();
    }
}
