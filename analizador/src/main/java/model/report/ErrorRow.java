package model.report;

import java.util.Objects;

import model.lexical.Position;

/**
 * Fila para el "Reporte de errores":
 *  - Símbolo o cadena de error
 *  - Posición (línea/columna)
 *  - Mensaje de error en español
 */
public final class ErrorRow {
    private final String simboloOCadena;
    private final Position posicion;
    private final String mensaje;

    public ErrorRow(String simboloOCadena, Position posicion, String mensaje) {
        this.simboloOCadena = Objects.requireNonNull(simboloOCadena, "simboloOCadena no puede ser null");
        this.posicion = Objects.requireNonNull(posicion, "posicion no puede ser null");
        this.mensaje = Objects.requireNonNull(mensaje, "mensaje no puede ser null");
    }

    public String simboloOCadena() { return simboloOCadena; }
    public Position posicion() { return posicion; }
    public String mensaje() { return mensaje; }
}
