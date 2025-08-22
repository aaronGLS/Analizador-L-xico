package model.lexical;

import java.util.Objects;

/**
 * Representa un error léxico hallado durante el análisis.
 *
 * La práctica exige un "Reporte de errores" con:
 *  - Símbolo o cadena de error.
 *  - Posición (fila y columna).
 * Además, se solicita describir el error; por ello se incluye un mensaje.
 *
 * Ejemplos de mensajes (según las reglas que implementará en otras ramas):
 *  - "Símbolo fuera del alfabeto permitido"
 *  - "Número mal formado"
 *  - "Cadena no cerrada"
 *  - "Comentario de bloque no cerrado"
 *
 * Esta clase solo modela los datos; la generación del mensaje y la
 * detección del error pertenecen a otras capas (lexer/políticas).
 */
public record LexError(String simboloOCadena, Position posicion, String mensaje) {

    public LexError {
        Objects.requireNonNull(simboloOCadena, "El símbolo o cadena no puede ser null");
        Objects.requireNonNull(posicion, "La posición no puede ser null");
        Objects.requireNonNull(mensaje, "El mensaje no puede ser null");
    }
}
