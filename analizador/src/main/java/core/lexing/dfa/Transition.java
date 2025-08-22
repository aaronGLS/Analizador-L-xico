package core.lexing.dfa;

import java.util.Arrays;

/**
 * Transición determinista desde un estado hacia otro,
 * activada por un carácter, un rango de caracteres o un conjunto de caracteres.
 *
 * Sin regex ni predicados externos: únicamente comparaciones char-a-char.
 */
public final class Transition {

    /** Tipo de transición soportada. */
    public enum Kind { SINGLE, RANGE, SET }

    private final Kind kind;
    private final int toState;

    // SINGLE
    private final char single;

    // RANGE
    private final char from;
    private final char to;

    // SET (char[] ordenado para búsqueda binaria)
    private final char[] set;

    private Transition(Kind kind, int toState, char single, char from, char to, char[] set) {
        this.kind = kind;
        this.toState = toState;
        this.single = single;
        this.from = from;
        this.to = to;
        this.set = set;
    }

    /** Crea una transición para un solo carácter. */
    public static Transition onChar(char ch, int toState) {
        return new Transition(Kind.SINGLE, toState, ch, '\0', '\0', null);
    }

    /** Crea una transición para un rango inclusivo [from..to]. */
    public static Transition onRange(char from, char to, int toState) {
        if (to < from) throw new IllegalArgumentException("El rango es inválido: to < from");
        return new Transition(Kind.RANGE, toState, '\0', from, to, null);
    }

    /** Crea una transición para un conjunto de caracteres (se ordena internamente). */
    public static Transition onSet(char[] chars, int toState) {
        if (chars == null || chars.length == 0) {
            throw new IllegalArgumentException("El conjunto de caracteres no puede ser null ni vacío.");
        }
        char[] copy = Arrays.copyOf(chars, chars.length);
        Arrays.sort(copy);
        return new Transition(Kind.SET, toState, '\0', '\0', '\0', copy);
    }

    /** Estado destino. */
    public int toState() {
        return toState;
    }

    /** ¿El carácter 'c' activa esta transición? */
    public boolean matches(int c) {
        if (c < 0) return false; // EOF no matchea
        char ch = (char) c;
        return switch (kind) {
            case SINGLE -> ch == single;
            case RANGE  -> ch >= from && ch <= to;
            case SET    -> Arrays.binarySearch(set, ch) >= 0;
        };
    }
}
