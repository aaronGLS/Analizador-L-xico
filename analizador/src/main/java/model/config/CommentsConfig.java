package model.config;

import java.util.Objects;

/**
 * Delimitadores de comentario definidos por el usuario en config.json.
 * - linea: prefijo para comentario de línea (p. ej. //)
 * - bloqueInicio: prefijo para comentario de bloque (p. ej. /*)
 * - bloqueFin: sufijo para comentario de bloque (p. ej. *\/)
 *
 * Esta clase modela exclusivamente la estructura de configuración
 * solicitada por la práctica. No implementa lógica de análisis.
 * (Los comentarios deben ignorarse durante el análisis léxico, pero
 * eso se resuelve en otra rama).  Ver guía. 
 */
public final class CommentsConfig {

    private String linea;
    private String bloqueInicio;
    private String bloqueFin;

    /** Constructor vacío requerido por librerías de JSON (Gson/Jackson). */
    public CommentsConfig() {}

    /** Constructor conveniente. */
    public CommentsConfig(String linea, String bloqueInicio, String bloqueFin) {
        this.linea = Objects.requireNonNull(linea, "linea no puede ser null");
        this.bloqueInicio = Objects.requireNonNull(bloqueInicio, "bloqueInicio no puede ser null");
        this.bloqueFin = Objects.requireNonNull(bloqueFin, "bloqueFin no puede ser null");
    }

    public String getLinea() { return linea; }
    public String getBloqueInicio() { return bloqueInicio; }
    public String getBloqueFin() { return bloqueFin; }

    public void setLinea(String linea) { this.linea = linea; }
    public void setBloqueInicio(String bloqueInicio) { this.bloqueInicio = bloqueInicio; }
    public void setBloqueFin(String bloqueFin) { this.bloqueFin = bloqueFin; }

    /** Validación mínima de campos obligatorios (sin lógica extra). */
    public void validate() {
        if (linea == null || bloqueInicio == null || bloqueFin == null) {
            throw new IllegalArgumentException("Los campos de 'comentarios' (linea, bloqueInicio, bloqueFin) son obligatorios.");
        }
    }
}
