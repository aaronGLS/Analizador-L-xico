package core.lexing.stream;

import model.lexical.Position;

/**
 * Cursor de lectura carácter-a-carácter sobre un texto inmutable.
 *
 * Objetivo:
 *  - Proveer operaciones básicas de escaneo: peek(), peek(k), next(), eof().
 *  - Mantener posición 1-based (línea/columna) para reportes.
 *  - Tratar correctamente los saltos de línea: '\n', '\r' y la secuencia Windows "\r\n"
 *    como UN solo salto de línea a efectos de conteo.
 *
 * Decisiones:
 *  - Retorna int en peek/next: -1 indica EOF; en caso contrario es el código del char.
 *  - La posición devuelta por position() corresponde SIEMPRE al próximo carácter a leer.
 *  - No realiza reconocimiento de patrones ni manipulación de cadenas; solo navegación.
 *
 * Coherencia con la práctica:
 *  - La guía pide operar con chars (sin regex ni “helpers” de cadena) y reportar posiciones.
 *  - El alfabeto incluye espacio y salto de línea; aquí SOLO se contabilizan posiciones,
 *    la validación de alfabeto se hará en otra rama (p. ej., AlphabetPolicy).
 */
public final class CharCursor {

    /** Constante EOF: valor negativo para representar fin de flujo. */
    public static final int EOF = -1;

    private final CharSequence texto;
    private final int length;

    // Índice actual (0-based) del siguiente char a leer.
    private int index = 0;

    // Posición 1-based del siguiente char a leer.
    private int linea = 1;
    private int columna = 1;

    // Para detectar la secuencia CRLF y no contar dos saltos.
    private boolean ultimoFueCR = false;

    /**
     * Crea un cursor sobre el texto indicado.
     * @param texto contenido inmutable a recorrer; no puede ser null
     */
    public CharCursor(CharSequence texto) {
        if (texto == null) {
            throw new IllegalArgumentException("El texto de entrada no puede ser null.");
        }
        this.texto = texto;
        this.length = texto.length();
    }

    /**
     * Indica si no quedan más caracteres por leer.
     */
    public boolean eof() {
        return index >= length;
    }

    /**
     * Devuelve el código del próximo carácter sin consumirlo.
     * @return código de carácter (0..65535) o EOF si no hay más.
     */
    public int peek() {
        if (eof()) return EOF;
        return texto.charAt(index);
    }

    /**
     * Devuelve el código del carácter a una distancia k sin consumirlo.
     * @param k desplazamiento (0 = mismo que peek())
     * @return código de carácter o EOF si está fuera de rango.
     */
    public int peek(int k) {
        int pos = index + k;
        if (pos < 0 || pos >= length) return EOF;
        return texto.charAt(pos);
    }

    /**
     * Consume y devuelve el próximo carácter.
     * Actualiza línea/columna de acuerdo al carácter leído.
     * Trata "\r\n" como un solo salto de línea.
     *
     * @return código del carácter consumido o EOF si no hay más.
     */
    public int next() {
        if (eof()) return EOF;

        int ch = texto.charAt(index++);
        // Actualización de posición
        if (ch == '\r') {
            // Salto de línea por CR
            linea++;
            columna = 1;
            ultimoFueCR = true; // si lo siguiente es '\n', no volver a contar otro salto
        } else if (ch == '\n') {
            if (ultimoFueCR) {
                // Parte de CRLF: ya contamos el salto con el CR anterior
                ultimoFueCR = false;
                // columna queda en 1; no incrementamos línea otra vez
            } else {
                // LF solitario: cuenta un salto
                linea++;
                columna = 1;
            }
        } else {
            // Carácter normal
            columna++;
            ultimoFueCR = false;
        }
        return ch;
    }

    /**
     * Posición (1-based) del siguiente carácter a leer.
     * Úsela para capturar la posición de inicio de un lexema antes de consumirlo.
     */
    public Position position() {
        return new Position(linea, columna);
    }

    /** Línea actual (1-based) del siguiente carácter a leer. */
    public int line() { return linea; }

    /** Columna actual (1-based) del siguiente carácter a leer. */
    public int column() { return columna; }

    /** Índice 0-based del siguiente carácter a leer (útil para estructuras auxiliares). */
    public int index() { return index; }

    /** Longitud total del texto subyacente. */
    public int length() { return length; }
}
