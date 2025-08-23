package service;

import java.util.*;

import model.config.Config;
import model.lexical.Token;

/**
 * Calcula el conjunto de "tokens definidos pero NO utilizados" según
 * config.json:
 * - Palabras reservadas no usadas
 * - Operadores no usados
 * - Puntuación no usada
 * - Agrupación no usada
 *
 * Reglas:
 * - Case-sensitive, consistente con la configuración y el clasificador.
 * - Se consideran usados solo los tokens de los tipos correspondientes:
 * RESERVED_WORD, OPERATOR, PUNCTUATION, GROUPING.
 * - No decide visibilidad del reporte; lo hará otra capa (p.ej.,
 * GradingService).
 */
public final class NotUsedCalculator {

    public record Result(Set<String> reservadasNoUsadas,
            Set<String> operadoresNoUsados,
            Set<String> puntuacionNoUsada,
            Set<String> agrupacionNoUsada) {
    }

    public Result compute(Config config, List<Token> tokens) {
        Objects.requireNonNull(config, "config no puede ser null");
        Set<String> usedReserved = new LinkedHashSet<>();
        Set<String> usedOperators = new LinkedHashSet<>();
        Set<String> usedPunctuation = new LinkedHashSet<>();
        Set<String> usedGrouping = new LinkedHashSet<>();

        if (tokens != null) {
            for (Token t : tokens) {
                switch (t.tipo()) {
                    case RESERVED_WORD -> usedReserved.add(t.lexema());
                    case OPERATOR -> usedOperators.add(t.lexema());
                    case PUNCTUATION -> usedPunctuation.add(t.lexema());
                    case GROUPING -> usedGrouping.add(t.lexema());
                    default -> {
                        /* otros tipos no aplican al cálculo de “no usados” */ }
                }
            }
        }

        Set<String> notUsedReserved = diff(config.getPalabrasReservadas(), usedReserved);
        Set<String> notUsedOperators = diff(config.getOperadores(), usedOperators);
        Set<String> notUsedPunctuation = diff(config.getPuntuacion(), usedPunctuation);
        Set<String> notUsedGrouping = diff(config.getAgrupacion(), usedGrouping);

        return new Result(notUsedReserved, notUsedOperators, notUsedPunctuation, notUsedGrouping);
    }

    private static Set<String> diff(Set<String> universe, Set<String> used) {
        Set<String> res = new LinkedHashSet<>();
        if (universe != null) {
            for (String s : universe) {
                if (!used.contains(s))
                    res.add(s);
            }
        }
        return res;
    }
}
