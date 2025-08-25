package core.highlight;

import java.awt.Color;

/**
 * Paleta de colores centralizada para el resaltado de tokens y otros
 * elementos del texto.  Provee constantes públicas que pueden ser
 * reutilizadas por los distintos componentes.
 */
public final class ColorPalette {

    private ColorPalette() {
        // Utility class
    }

    // Colores para los diferentes tipos de tokens
    public static final Color RESERVED   = Color.BLUE;
    public static final Color IDENTIFIER = new Color(101, 67, 33); // café
    public static final Color NUMBER     = Color.GREEN;
    public static final Color STRING     = Color.ORANGE;
    public static final Color DECIMAL    = Color.BLACK;
    public static final Color PUNCTUATION = DECIMAL; // negro
    public static final Color COMMENT    = new Color(0, 100, 0); // verde oscuro
    public static final Color OPERATOR   = Color.YELLOW;
    public static final Color GROUPING   = new Color(128, 0, 128); // morado
    public static final Color ERROR      = Color.RED;
}
