package service;

import java.util.*;

import model.lexical.Token;
import model.lexical.TokenType;
import model.report.LexemeCountRow;

/**
 * Servicio para calcular el "Recuento de lexemas":
 * - Agrupa por (lexema, tipo) y cuenta ocurrencias.
 *
 * Nota:
 * - La regla de "mostrar recuento SOLO si no hay errores" se aplicará
 * en ReportBuilder (esta clase solo calcula).
 */
public final class StatsService {

    /**
     * Calcula el recuento de lexemas por (lexema, tipo).
     * 
     * @param tokens lista de tokens válidos
     * @return lista de filas LexemeCountRow
     */
    public List<LexemeCountRow> countByLexemeAndType(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty())
            return List.of();

        // Mapa (lexema -> (tipo -> conteo))
        Map<String, Map<TokenType, Integer>> map = new LinkedHashMap<>();

        for (Token t : tokens) {
            map.computeIfAbsent(t.lexema(), k -> new LinkedHashMap<>())
                    .merge(t.tipo(), 1, Integer::sum);
        }

        List<LexemeCountRow> rows = new ArrayList<>();
        for (var e1 : map.entrySet()) {
            String lex = e1.getKey();
            for (var e2 : e1.getValue().entrySet()) {
                rows.add(new LexemeCountRow(lex, e2.getKey(), e2.getValue()));
            }
        }
        return rows;
    }
}
