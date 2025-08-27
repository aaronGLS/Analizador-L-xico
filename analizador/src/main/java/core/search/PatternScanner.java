package core.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Escáner ingenuo (naive) de patrones, char-a-char y sensible/insensible a mayúsculas.
 * No usa regex ni helpers avanzados; compara carácter por carácter para cumplir la práctica.
 *
 * Política:
 *  - Permite coincidencias solapadas (se avanza i++ incluso tras encontrar un match).
 */
public final class PatternScanner {

    private PatternScanner() { /* utilitaria, no instanciable */ }

    /**
     * Busca todas las ocurrencias de {@code pattern} en {@code text}.
     * @param text          texto donde buscar (no null)
     * @param pattern       patrón a buscar (no null ni vacío)
     * @param caseSensitive true = sensible a mayúsculas/minúsculas
     * @param wholeWord     true = coincide solo palabras completas
     * @return lista de pares [startIndex, length] para cada coincidencia
     * @throws IllegalArgumentException si pattern es null o vacío
     */
    public static List<int[]> findAll(String text, String pattern, boolean caseSensitive, boolean wholeWord) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("El patrón de búsqueda no puede ser null ni vacío.");
        }
        final int n = text.length();
        final int m = pattern.length();
        var res = new ArrayList<int[]>();
        if (m > n) return res;

        for (int i = 0; i <= n - m; i++) {
            int j = 0;
            while (j < m) {
                char a = text.charAt(i + j);
                char b = pattern.charAt(j);
                if (!caseSensitive) {
                    a = Character.toLowerCase(a);
                    b = Character.toLowerCase(b);
                }
                if (a != b) break;
                j++;
            }
            if (j == m) {
                if (wholeWord) {
                    boolean leftOk = (i == 0) || !isWordChar(text.charAt(i - 1));
                    boolean rightOk = (i + m >= n) || !isWordChar(text.charAt(i + m));
                    if (!(leftOk && rightOk)) {
                        continue;
                    }
                }
                res.add(new int[]{i, m});
            }
        }
        return res;
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
    
    // Búsqueda por code points (no normaliza, offsets en UTF-16)
    public static List<int[]> findAllCodePoints(String text, String pattern, boolean caseSensitive, boolean wholeWord) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("El patrón de búsqueda no puede ser null ni vacío.");
        }
        final int n = text.length();
        final int m = pattern.length(); // longitud en unidades char (UTF-16)
        var res = new ArrayList<int[]>();
        if (m > n) return res;
        for (int i = 0; i <= n - m; i++) {
            int ti = i;
            int pj = 0;
            boolean matched = true;
            while (pj < m && ti < n) {
                int a = Character.codePointAt(text, ti);
                int b = Character.codePointAt(pattern, pj);
                int ca = caseSensitive ? a : Character.toLowerCase(a);
                int cb = caseSensitive ? b : Character.toLowerCase(b);
                if (ca != cb) { matched = false; break; }
                ti += Character.charCount(a);
                pj += Character.charCount(b);
            }
            if (matched && pj == m) {
                if (wholeWord) {
                    boolean leftOk = (i == 0) || !isWordCp(Character.codePointBefore(text, i));
                    boolean rightOk = (ti >= n) || !isWordCp(Character.codePointAt(text, ti));
                    if (!(leftOk && rightOk)) {
                        continue;
                    }
                }
                res.add(new int[]{i, ti - i});
            }
        }
        return res;
    }

    private static boolean isWordCp(int cp) {
        return Character.isLetterOrDigit(cp) || cp == '_';
    }
}
