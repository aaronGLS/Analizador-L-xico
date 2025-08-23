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
     * @return lista de pares [startIndex, length] para cada coincidencia
     * @throws IllegalArgumentException si pattern es null o vacío
     */
    public static List<int[]> findAll(String text, String pattern, boolean caseSensitive) {
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
                res.add(new int[]{i, m});
            }
        }
        return res;
    }
}
