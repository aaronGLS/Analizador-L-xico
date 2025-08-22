package model.lexical;

/**
 * Posición dentro del texto de entrada.
 * La práctica solicita reportar la posición del token o del error
 * en términos de Fila (línea) y Columna. (1-based)
 *
 * Reglas:
 * - La línea y la columna comienzan en 1.
 * - Esta clase no realiza cómputo de posiciones; únicamente modela el dato.
 */
public record Position(int linea, int columna) {

    /**
     * Crea una posición (línea/columna) 1-based.
     *
     * @param linea   número de línea, debe ser >= 1
     * @param columna número de columna, debe ser >= 1
     * @throws IllegalArgumentException si linea o columna son < 1
     */
    public Position {
        if (linea < 1) {
            throw new IllegalArgumentException("La línea debe ser >= 1");
        }
        if (columna < 1) {
            throw new IllegalArgumentException("La columna debe ser >= 1");
        }
    }

    /**
     * Representación legible en español, útil para mensajes.
     */
    @Override
    public String toString() {
        return "(línea " + linea + ", columna " + columna + ")";
    }
}
