package core.lexing.dfa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Estado de un AFD (DFA) con lista de transiciones deterministas.
 *
 * @param <T> Tipo de la etiqueta asociada a estados de aceptación (p.ej. una categoría).
 *
 * Responsabilidades:
 *  - Conocer si es de aceptación y, en su caso, qué etiqueta de aceptación tiene.
 *  - Mantener transiciones salientes (deterministas) a otros estados.
 *
 * Este estado NO contiene lógica de recorrido; eso lo hace Dfa<T>.
 */
public final class State<T> {

    private final int id;
    private boolean accepting;
    private T acceptTag;

    private final List<Transition> transitions = new ArrayList<>();

    public State(int id) {
        this.id = id;
    }

    /** Identificador interno del estado (0..N-1). */
    public int id() {
        return id;
    }

    /** ¿Es estado de aceptación? */
    public boolean isAccepting() {
        return accepting;
    }

    /** Etiqueta de aceptación asociada (si accepting == true). */
    public T acceptTag() {
        return acceptTag;
    }

    /** Marca el estado como de aceptación con la etiqueta dada. */
    public void setAccepting(T tag) {
        this.accepting = true;
        this.acceptTag = tag;
    }

    /** Agrega una transición saliente. (Determinismo garantizado por construcción en el builder). */
    void addTransition(Transition t) {
        transitions.add(t);
    }

    /** Vista inmodificable de las transiciones salientes. */
    public List<Transition> transitions() {
        return Collections.unmodifiableList(transitions);
    }
}
