package model.report;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Reporte general ("calificación") solicitado por la práctica.
 *
 * Contiene:
 * - errorCount: cantidad de errores léxicos encontrados.
 * - validTokenCount: cantidad de tokens válidos.
 * - totalItems: validTokenCount + errorCount.
 * - percentValid: porcentaje de elementos válidos (tokens / total * 100).
 * * Regla: si totalItems == 0, percentValid = 100.0
 * - Conjuntos de "definidos y NO utilizados" según config.json:
 * reservadasNoUsadas, operadoresNoUsados, puntuacionNoUsada, agrupacionNoUsada.
 */
public final class GeneralReport {

    private final int errorCount;
    private final int validTokenCount;
    private final int totalItems;
    private final double percentValid;

    private final Set<String> reservadasNoUsadas;
    private final Set<String> operadoresNoUsados;
    private final Set<String> puntuacionNoUsada;
    private final Set<String> agrupacionNoUsada;

    public GeneralReport(int errorCount,
            int validTokenCount,
            double percentValid,
            Set<String> reservadasNoUsadas,
            Set<String> operadoresNoUsados,
            Set<String> puntuacionNoUsada,
            Set<String> agrupacionNoUsada) {
        if (errorCount < 0 || validTokenCount < 0) {
            throw new IllegalArgumentException("Los conteos no pueden ser negativos.");
        }
        this.errorCount = errorCount;
        this.validTokenCount = validTokenCount;
        this.totalItems = errorCount + validTokenCount;
        this.percentValid = percentValid;

        this.reservadasNoUsadas = Collections.unmodifiableSet(Objects.requireNonNull(reservadasNoUsadas));
        this.operadoresNoUsados = Collections.unmodifiableSet(Objects.requireNonNull(operadoresNoUsados));
        this.puntuacionNoUsada = Collections.unmodifiableSet(Objects.requireNonNull(puntuacionNoUsada));
        this.agrupacionNoUsada = Collections.unmodifiableSet(Objects.requireNonNull(agrupacionNoUsada));
    }

    public int errorCount() {
        return errorCount;
    }

    public int validTokenCount() {
        return validTokenCount;
    }

    public int totalItems() {
        return totalItems;
    }

    public double percentValid() {
        return percentValid;
    }

    public Set<String> reservadasNoUsadas() {
        return reservadasNoUsadas;
    }

    public Set<String> operadoresNoUsados() {
        return operadoresNoUsados;
    }

    public Set<String> puntuacionNoUsada() {
        return puntuacionNoUsada;
    }

    public Set<String> agrupacionNoUsada() {
        return agrupacionNoUsada;
    }
}
