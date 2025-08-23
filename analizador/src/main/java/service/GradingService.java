package service;



import java.util.List;
import java.util.Objects;

import model.config.Config;
import model.lexical.LexError;
import model.lexical.Token;
import model.report.GeneralReport;

/**
 * Servicio para construir el "Reporte General" ("calificación") de la práctica.
 *
 * Reglas aplicadas:
 * - percentValid = (tokensValidos / (tokensValidos + errores)) * 100.
 * * Si el total es 0, se define percentValid = 100.0
 * - "No utilizados": se calcula con NotUsedCalculator contra config.json.
 */
public final class GradingService {

    private final NotUsedCalculator notUsedCalculator = new NotUsedCalculator();

    /**
     * Construye el reporte general.
     * 
     * @param config configuración dinámica (para "no utilizados")
     * @param tokens lista de tokens válidos
     * @param errors lista de errores léxicos
     * @return GeneralReport con calificación y conjuntos "no usados"
     */
    public GeneralReport build(Config config, List<Token> tokens, List<LexError> errors) {
        Objects.requireNonNull(config, "config no puede ser null");
        Objects.requireNonNull(tokens, "tokens no puede ser null");
        Objects.requireNonNull(errors, "errors no puede ser null");

        int valid = tokens.size();
        int err = errors.size();
        int total = valid + err;
        double percent = (total == 0) ? 100.0 : (valid * 100.0) / total;

        var notUsed = notUsedCalculator.compute(config, tokens);

        return new GeneralReport(
                err,
                valid,
                percent,
                notUsed.reservadasNoUsadas(),
                notUsed.operadoresNoUsados(),
                notUsed.puntuacionNoUsada(),
                notUsed.agrupacionNoUsada());
    }
}
