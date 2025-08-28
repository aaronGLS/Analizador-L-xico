package core.lexing.dfa;

import core.lexing.stream.CharCursor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AFD (DFA) ejecutor: recorre el texto desde la posición actual de un
 * {@link CharCursor}
 * y determina el prefijo aceptado MÁS LARGO (maximal munch) sin consumir del
 * cursor.
 *
 * @param <T> Tipo de etiqueta asociada a estados de aceptación (p.ej.
 *            categoría, nombre).
 *
 *            Comportamiento:
 *            - Inicia en el estado 'startState'.
 *            - Lee caracteres con cursor.peek(k) avanzando un desplazamiento
 *            local.
 *            - Si encuentra transición, pasa al siguiente estado y continúa.
 *            - Registra el último estado de aceptación alcanzado y la longitud
 *            hasta allí.
 *            - Se detiene cuando no hay transición aplicable o se alcanza EOF.
 *            - Devuelve un Match con: ¿hubo aceptación?, longitud y etiqueta
 *            del estado aceptado.
 *
 *            Importante:
 *            - NO consume del cursor (SRP). Quien llame decide cuántos
 *            caracteres consumir luego.
 *            - No hay minimización ni diagnósticos de no-determinismo aquí
 *            (núcleo mínimo).
 */
public final class Dfa<T> {

    /** Resultado de una evaluación del DFA desde la posición actual del cursor. */
    public static final class Match<T> {
        private final boolean accepted;
        private final int length;
        private final T acceptTag;

        private Match(boolean accepted, int length, T acceptTag) {
            this.accepted = accepted;
            this.length = length;
            this.acceptTag = acceptTag;
        }

        /** ¿Se reconoció algún prefijo válido? */
        public boolean accepted() {
            return accepted;
        }

        /** Longitud del prefijo aceptado más largo. (0 si !accepted) */
        public int length() {
            return length;
        }

        /**
         * Etiqueta del estado aceptado responsable del match (puede ser null si
         * !accepted).
         */
        public T acceptTag() {
            return acceptTag;
        }
    }

    private final List<State<T>> states;
    private final int startState;

    Dfa(List<State<T>> states, int startState) {
        this.states = Collections.unmodifiableList(new ArrayList<>(states));
        this.startState = startState;
    }

    /** Estados (vista inmodificable). Útil para pruebas y diagnósticos. */
    public List<State<T>> states() {
        return states;
    }

    /** Estado inicial. */
    public int startState() {
        return startState;
    }

    /**
     * Evalúa el DFA desde la posición actual del cursor (sin consumir) y
     * retorna el prefijo aceptado MÁS LARGO.
     */
    public Match<T> evaluate(CharCursor cursor) {
        if (cursor == null || cursor.eof()) {
            return new Match<>(false, 0, null);
        }

        int current = startState;
        int offset = 0;

        int lastAcceptLen = -1;
        T lastAcceptTag = null;

        while (true) {
            // Si el estado actual es de aceptación, recordamos el progreso
            State<T> s = states.get(current);
            if (s.isAccepting()) {
                lastAcceptLen = offset;
                lastAcceptTag = s.acceptTag();
            }

            int c = cursor.peek(offset);
            if (c == CharCursor.EOF) {
                break;
            }

            // Buscar una transición que matchee el carácter actual
            int nextState = -1;
            for (Transition t : s.transitions()) {
                if (t.matches(c)) {
                    nextState = t.toState();
                    break; // determinista: la primera que matchee
                }
            }
            if (nextState < 0)
                break; // no hay transición aplicable

            // Avanzamos localmente
            current = nextState;
            offset++;
        }

        if (lastAcceptLen >= 0) {
            return new Match<>(true, lastAcceptLen, lastAcceptTag);
        }
        return new Match<>(false, 0, null);
    }
}
