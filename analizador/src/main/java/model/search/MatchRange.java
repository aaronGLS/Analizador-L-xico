package model.search;

import java.util.Objects;

import model.lexical.Position;

/**
 * Representa un rango de coincidencia dentro del texto:
 *  - startIndex y endIndex son índices 0-based (endIndex es INCLUSIVO).
 *  - startPosition y endPosition son posiciones 1-based (línea/columna).
 *
 * Esta clase es de modelo puro (sin lógica).
 */
public final class MatchRange {
    private final int startIndex;
    private final int endIndex; // inclusivo
    private final Position startPosition;
    private final Position endPosition;

    public MatchRange(int startIndex, int endIndex, Position startPosition, Position endPosition) {
        if (startIndex < 0 || endIndex < startIndex) {
            throw new IllegalArgumentException("Rango inválido: startIndex debe ser >= 0 y endIndex >= startIndex.");
        }
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.startPosition = Objects.requireNonNull(startPosition, "startPosition no puede ser null");
        this.endPosition = Objects.requireNonNull(endPosition, "endPosition no puede ser null");
    }

    public int startIndex() { return startIndex; }
    public int endIndex()   { return endIndex; }
    public Position startPosition() { return startPosition; }
    public Position endPosition()   { return endPosition; }
}
