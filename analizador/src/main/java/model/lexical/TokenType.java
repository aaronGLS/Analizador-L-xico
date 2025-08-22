package model.lexical;

/**
 * Tipos de token válidos de acuerdo con la práctica.
 *
 * Incluye:
 *  - IDENTIFIER: inicia con letra y puede contener letras o dígitos.
 *  - NUMBER: uno o más dígitos.
 *  - DECIMAL: dígitos, punto y uno o más dígitos.
 *  - STRING: comillas dobles al inicio y final; el contenido debe ser símbolo permitido.
 *  - RESERVED_WORD: palabra reservada definida en config.json.
 *  - PUNCTUATION: signo de puntuación definido en config.json.
 *  - OPERATOR: operador aritmético definido en config.json.
 *  - GROUPING: signo de agrupación definido en config.json.
 */
public enum TokenType {
    IDENTIFIER,
    NUMBER,
    DECIMAL,
    STRING,
    RESERVED_WORD,
    PUNCTUATION,
    OPERATOR,
    GROUPING
}
