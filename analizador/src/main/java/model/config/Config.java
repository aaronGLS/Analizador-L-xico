package model.config;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configuración dinámica cargada desde config.json.
 *
 * Campos exigidos por la práctica y su ejemplo:
 *  - palabrasReservadas: lista de strings (case-sensitive).
 *  - operadores: lista de strings (pueden ser multi-caracter).
 *  - puntuacion: lista de strings.
 *  - agrupacion: lista de strings.
 *  - comentarios: objeto con { linea, bloqueInicio, bloqueFin }.
 *
 * La guía especifica que estos conjuntos deben poder definirse de forma
 * dinámica por el usuario editando config.json. (Se cargan/guardan en IO). 
 */
public final class Config {

    // Se usan Set para evitar duplicados accidentales manteniendo el contrato de "colección" del JSON.
    private Set<String> palabrasReservadas = new LinkedHashSet<>();
    private Set<String> operadores = new LinkedHashSet<>();
    private Set<String> puntuacion = new LinkedHashSet<>();
    private Set<String> agrupacion = new LinkedHashSet<>();
    private CommentsConfig comentarios;

    /** Constructor vacío para librerías JSON. */
    public Config() {}

    public Set<String> getPalabrasReservadas() { return palabrasReservadas; }
    public Set<String> getOperadores() { 
        return operadores; 
    }
    public Set<String> getPuntuacion() { 
        return puntuacion; 
    }
    public Set<String> getAgrupacion() { 
        return agrupacion; 
    }
    public CommentsConfig getComentarios() { 
        return comentarios; 
    }

    public void setPalabrasReservadas(Set<String> palabrasReservadas) {
        this.palabrasReservadas = (palabrasReservadas == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(palabrasReservadas);
    }
    public void setOperadores(Set<String> operadores) {
        this.operadores = (operadores == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(operadores);
    }
    public void setPuntuacion(Set<String> puntuacion) {
        this.puntuacion = (puntuacion == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(puntuacion);
    }
    public void setAgrupacion(Set<String> agrupacion) {
        this.agrupacion = (agrupacion == null) ? new LinkedHashSet<>() : new LinkedHashSet<>(agrupacion);
    }
    public void setComentarios(CommentsConfig comentarios) {
        this.comentarios = comentarios;
    }

    /**
     * Validación mínima: asegura presencia de todas las secciones
     * definidas por la guía y del objeto 'comentarios'.
     * (No aplica reglas de lenguaje; eso se resuelve en el léxico).
     */
    public void validate() {
        if (comentarios == null) {
            throw new IllegalArgumentException("Falta la sección 'comentarios' en config.json.");
        }
        comentarios.validate();

        if (palabrasReservadas == null || operadores == null || puntuacion == null || agrupacion == null) {
            throw new IllegalArgumentException("Las secciones palabrasReservadas, operadores, puntuacion y agrupacion son obligatorias.");
        }

         // Asegurarse de que operadores, puntuacion y agrupacion no tengan símbolos duplicados entre sí.
        Set<String> duplicates = new LinkedHashSet<>();

        Set<String> intersection = new LinkedHashSet<>(operadores);
        intersection.retainAll(puntuacion);
        duplicates.addAll(intersection);

        intersection = new LinkedHashSet<>(operadores);
        intersection.retainAll(agrupacion);
        duplicates.addAll(intersection);

        intersection = new LinkedHashSet<>(puntuacion);
        intersection.retainAll(agrupacion);
        duplicates.addAll(intersection);

        if (!duplicates.isEmpty()) {
            throw new IllegalArgumentException("Símbolos duplicados entre operadores, puntuacion y agrupacion: " + duplicates);
        }
    }
}
