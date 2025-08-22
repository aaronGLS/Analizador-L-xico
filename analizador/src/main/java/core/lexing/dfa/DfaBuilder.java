package core.lexing.dfa;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructor (builder) de AFDs deterministas sin regex.
 *
 * @param <T> Tipo de etiqueta de aceptación (p.ej., categoría).
 *
 * Uso:
 *   DfaBuilder<T> b = new DfaBuilder<>();
 *   int s0 = b.addState();
 *   int s1 = b.addState();
 *   b.onRange(s0, '0', '9', s1);
 *   b.setAccepting(s1, SOME_TAG);
 *   Dfa<T> dfa = b.build(s0);
 *
 * Filosofía:
 *  - Núcleo mínimo: estados, transiciones por char/rango/set, marca de aceptación.
 *  - Sin validaciones pesadas (p.ej., detección de solapamientos); mantener simple.
 *  - El determinismo se garantiza por construcción del usuario del builder.
 */
public final class DfaBuilder<T> {

    private final List<State<T>> states = new ArrayList<>();

    /** Crea y retorna un nuevo estado (su id es el índice en la lista). */
    public int addState() {
        int id = states.size();
        states.add(new State<>(id));
        return id;
    }

    /** Marca el estado 'stateId' como de aceptación con la etiqueta 'tag'. */
    public DfaBuilder<T> setAccepting(int stateId, T tag) {
        checkState(stateId);
        states.get(stateId).setAccepting(tag);
        return this;
    }

    /** Agrega una transición por un solo carácter desde 'from' hacia 'to'. */
    public DfaBuilder<T> onChar(int from, char ch, int to) {
        checkState(from);
        checkState(to);
        states.get(from).addTransition(Transition.onChar(ch, to));
        return this;
    }

    /** Agrega una transición por rango inclusivo [start..end] desde 'from' hacia 'to'. */
    public DfaBuilder<T> onRange(int from, char start, char end, int to) {
        checkState(from);
        checkState(to);
        states.get(from).addTransition(Transition.onRange(start, end, to));
        return this;
    }

    /** Agrega una transición por conjunto de caracteres desde 'from' hacia 'to'. */
    public DfaBuilder<T> onSet(int from, char[] chars, int to) {
        checkState(from);
        checkState(to);
        states.get(from).addTransition(Transition.onSet(chars, to));
        return this;
    }

    /** Construye el DFA con el estado inicial indicado. */
    public Dfa<T> build(int startState) {
        checkState(startState);
        // Copia defensiva ya la hace Dfa en su constructor
        return new Dfa<>(states, startState);
    }

    private void checkState(int id) {
        if (id < 0 || id >= states.size()) {
            throw new IllegalArgumentException("Estado inexistente: " + id);
        }
    }
}
