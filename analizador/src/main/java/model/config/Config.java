package model.config;

import java.util.LinkedHashSet;
import java.util.Set;

import core.lexing.policy.AlphabetPolicy;
import core.lexing.stream.CharCursor;
import core.lexing.table.OperatorTable;

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

        // Normalizar y validar contenido básico de los conjuntos
        palabrasReservadas = trimAndCheck("palabrasReservadas", palabrasReservadas);
        operadores       = trimAndCheck("operadores",       operadores);
        puntuacion       = trimAndCheck("puntuacion",       puntuacion);
        agrupacion       = trimAndCheck("agrupacion",       agrupacion);

        AlphabetPolicy policy = new AlphabetPolicy();
        OperatorTable opTable = new OperatorTable(operadores);
        OperatorTable punctTable = new OperatorTable(puntuacion);
        OperatorTable groupTable = new OperatorTable(agrupacion);

        checkAlphabet("palabrasReservadas", palabrasReservadas, policy, opTable, punctTable, groupTable);
        checkAlphabet("operadores", operadores, policy, opTable, punctTable, groupTable);
        checkAlphabet("puntuacion", puntuacion, policy, opTable, punctTable, groupTable);
        checkAlphabet("agrupacion", agrupacion, policy, opTable, punctTable, groupTable);

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

    /** Normaliza un conjunto: trim de cada elemento y verificación de vacío. */
    private Set<String> trimAndCheck(String key, Set<String> values) {
        Set<String> cleaned = new LinkedHashSet<>();
        for (String v : values) {
            String t = (v == null) ? "" : v.trim();
            if (t.isEmpty()) {
                throw new IllegalArgumentException("Elemento vacío en '" + key + "': '" + v + "'");
            }
            cleaned.add(t);
        }
        return cleaned;
    }

    /** Verifica que cada símbolo del conjunto pertenezca al alfabeto permitido. */
    private void checkAlphabet(String key,
                               Set<String> values,
                               AlphabetPolicy policy,
                               OperatorTable opTable,
                               OperatorTable punctTable,
                               OperatorTable groupTable) {
        for (String s : values) {
            CharCursor cursor = new CharCursor(s);
            while (!cursor.eof()) {
                if (!policy.isAllowedAt(cursor, this, opTable, punctTable, groupTable)) {
                    throw new IllegalArgumentException("Símbolo fuera del alfabeto en '" + key + "': '" + s + "'");
                }
                String match = opTable.longestMatch(cursor);
                if (match == null) match = punctTable.longestMatch(cursor);
                if (match == null) match = groupTable.longestMatch(cursor);
                if (match != null) {
                    for (int i = 0; i < match.length(); i++) cursor.next();
                } else {
                    cursor.next();
                }
            }
        }
    }
}
