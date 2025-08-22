package core.lexing;


import model.config.Config;
import model.lexical.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import core.lexing.policy.*;
import core.lexing.recognizer.*;
import core.lexing.stream.CharClasses;
import core.lexing.stream.CharCursor;
import core.lexing.table.OperatorTable;
import core.lexing.table.ReservedWords;

/**
 * Orquestador del análisis léxico (char-a-char) conforme a la práctica.
 *
 * Responsabilidades:
 *  - Coordinar reconocedores (comentarios, cadenas, números, decimales, identificadores,
 *    operadores, puntuación y agrupación).
 *  - Aplicar la política de alfabeto para reportar símbolos fuera de alfabeto.
 *  - Aplicar la política de recuperación de errores: emitir LexError y continuar.
 *  - Generar tokens con tipo, lexema exacto y posición 1-based del inicio del lexema.
 *
 * Restricciones:
 *  - Trabaja únicamente con CharCursor (peek/next), sin regex ni utilidades de cadena avanzadas.
 *  - Los comentarios se IGNORAN durante el análisis (no se emiten como tokens).
 */
public final class LexerEngine {

    /** Resultado agregado del análisis léxico. */
    public static final class Result {
        private final List<Token> tokens;
        private final List<LexError> errors;

        public Result(List<Token> tokens, List<LexError> errors) {
            this.tokens = tokens;
            this.errors = errors;
        }
        public List<Token> tokens() { return tokens; }
        public List<LexError> errors() { return errors; }
    }

    private final Config config;

    // Tablas configurables
    private final OperatorTable opTable;
    private final OperatorTable punctTable;
    private final OperatorTable groupTable;
    private final ReservedWords reserved;

    // Reconocedores
    private final LineCommentRecognizer lineComment = new LineCommentRecognizer();
    private final BlockCommentRecognizer blockComment = new BlockCommentRecognizer();

    private final StringRecognizer stringRec;
    private final DecimalRecognizer decimalRec = new DecimalRecognizer();
    private final NumberRecognizer numberRec = new NumberRecognizer();
    private final IdentifierRecognizer identRec = new IdentifierRecognizer();

    private final OperatorRecognizer operatorRec;
    private final PunctuationRecognizer punctuationRec;
    private final GroupingRecognizer groupingRec;

    private final TokenClassifier classifier;

    // Políticas
    private final AlphabetPolicy alphabetPolicy = new AlphabetPolicy();
    private final ErrorRecoveryPolicy recoveryPolicy = new ErrorRecoveryPolicy();

    /**
     * Construye el lexer con la configuración dinámica.
     * @param config archivo de configuración cargado desde config.json
     */
    public LexerEngine(Config config) {
        this.config = Objects.requireNonNull(config, "config no puede ser null");

        this.opTable = new OperatorTable(config.getOperadores());
        this.punctTable = new OperatorTable(config.getPuntuacion());
        this.groupTable = new OperatorTable(config.getAgrupacion());
        this.reserved = new ReservedWords(config.getPalabrasReservadas());

        this.operatorRec = new OperatorRecognizer(opTable);
        this.punctuationRec = new PunctuationRecognizer(punctTable);
        this.groupingRec = new GroupingRecognizer(groupTable);

        this.stringRec = new StringRecognizer(config, opTable, punctTable, groupTable);

        this.classifier = new TokenClassifier(reserved);
    }

    /**
     * Ejecuta el análisis léxico sobre el texto indicado.
     * Produce tokens y errores con posiciones 1-based.
     *
     * @param text texto de entrada
     * @return Result con listas inmutables de tokens y errores.
     */
    public Result analyze(String text) {
        if (text == null) throw new IllegalArgumentException("El texto de entrada no puede ser null.");

        var cursor = new CharCursor(text);
        var tokens = new ArrayList<Token>();
        var errors = new ArrayList<LexError>();

        while (!cursor.eof()) {

            // 0) Saltos/blancos del alfabeto (espacio, CR/LF) se omiten
            if (CharClasses.isSpaceOrNewline(cursor.peek())) {
                cursor.next();
                continue;
            }

            // Posición e índice de inicio del posible lexema
            Position pos = cursor.position();
            int startIndex = cursor.index();

            // Delimitador de cierre de bloque sin apertura
            String blockEnd = config.getComentarios() != null ? config.getComentarios().getBloqueFin() : null;
            if (blockEnd != null && startsWith(cursor, blockEnd)) {
                errors.add(recoveryPolicy.buildLexError(text, startIndex, blockEnd.length(), pos,
                        "Delimitador de cierre de bloque sin apertura", null));
                consume(cursor, blockEnd.length());
                continue;
            }

            // 1) Comentarios (se IGNORAN; solo reportar error si bloque no cierra)
            Recognition r = lineComment.recognize(cursor, config.getComentarios());
            if (r.matched()) {
                consume(cursor, r.length());
                continue;
            }
            r = blockComment.recognize(cursor, config.getComentarios());
            if (r.matched()) {
                if (r.hasError()) {
                    // Error: comentario de bloque no cerrado (consume hasta EOF según reconocedor)
                    errors.add(recoveryPolicy.buildLexError(text, startIndex, r.length(), pos, r.errorMessage(), r.errorLexeme()));
                }
                consume(cursor, r.length());
                continue;
            }

            // 2) Cadenas
            r = stringRec.recognize(cursor);
            if (r.matched()) {
                if (r.hasError()) {
                    int consumeLen = r.length();
                    int lexemeLen = consumeLen;
                    if (StringRecognizer.MSG_SIMBOLO_INVALIDO.equals(r.errorMessage())) {
                        lexemeLen = Math.max(0, consumeLen - 1);
                    }
                    errors.add(recoveryPolicy.buildLexError(text, startIndex, lexemeLen, pos, r.errorMessage(), r.errorLexeme()));
                    consume(cursor, consumeLen);
                } else {
                    String lex = substringSafe(text, startIndex, r.length());
                    tokens.add(new Token(TokenType.STRING, lex, pos));
                    consume(cursor, r.length());
                }
                continue;
            }

            // 3) Decimales (primero, para no confundir con enteros válidos)
            r = decimalRec.recognize(cursor);
            if (r.matched()) {
                if (r.hasError()) {
                    errors.add(recoveryPolicy.buildLexError(text, startIndex, r.length(), pos, r.errorMessage(), null));
                    consume(cursor, r.length());
                } else {
                    String lex = substringSafe(text, startIndex, r.length());
                    tokens.add(new Token(TokenType.DECIMAL, lex, pos));
                    consume(cursor, r.length());
                }
                continue;
            }

            // 4) Enteros (y "número mal formado" si tras dígitos hay letra inmediata)
            r = numberRec.recognize(cursor);
            if (r.matched()) {
                if (r.hasError()) {
                    errors.add(recoveryPolicy.buildLexError(text, startIndex, r.length(), pos, r.errorMessage(), null));
                    consume(cursor, r.length());
                } else {
                    String lex = substringSafe(text, startIndex, r.length());
                    tokens.add(new Token(TokenType.NUMBER, lex, pos));
                    consume(cursor, r.length());
                }
                continue;
            }

            // 5) Identificadores / Palabras reservadas
            r = identRec.recognize(cursor);
            if (r.matched()) {
                String lex = substringSafe(text, startIndex, r.length());
                TokenType type = classifier.classifyIdentOrReserved(lex);
                tokens.add(new Token(type, lex, pos));
                consume(cursor, r.length());
                continue;
            }

            // 6) Operadores / Puntuación / Agrupación (greedy longest-first en cada categoría)
            r = operatorRec.recognize(cursor);
            if (r.matched()) {
                String lex = substringSafe(text, startIndex, r.length());
                tokens.add(new Token(TokenType.OPERATOR, lex, pos));
                consume(cursor, r.length());
                continue;
            }
            r = punctuationRec.recognize(cursor);
            if (r.matched()) {
                String lex = substringSafe(text, startIndex, r.length());
                tokens.add(new Token(TokenType.PUNCTUATION, lex, pos));
                consume(cursor, r.length());
                continue;
            }
            r = groupingRec.recognize(cursor);
            if (r.matched()) {
                String lex = substringSafe(text, startIndex, r.length());
                tokens.add(new Token(TokenType.GROUPING, lex, pos));
                consume(cursor, r.length());
                continue;
            }

            // 7) Símbolo fuera del alfabeto → error y avanzar 1
            if (!alphabetPolicy.isAllowedAt(cursor, config, opTable, punctTable, groupTable)) {
                String offending = substringSafe(text, startIndex, 1);
                errors.add(new LexError(offending, pos, "Símbolo fuera del alfabeto permitido"));
                consume(cursor, 1);
                continue;
            }

            // 8) Si está permitido pero no coincide con nada (caso muy raro), avance 1 para evitar bucles
            consume(cursor, 1);
        }

        return new Result(List.copyOf(tokens), List.copyOf(errors));
    }

    /* ----------------- utilitarios internos ----------------- */

    private static void consume(CharCursor cursor, int length) {
        for (int i = 0; i < length; i++) {
            if (cursor.eof()) break;
            cursor.next();
        }
    }

    private static String substringSafe(String text, int startIndex, int length) {
        int end = Math.max(startIndex, Math.min(text.length(), startIndex + Math.max(0, length)));
        if (startIndex < 0 || startIndex >= text.length() || end <= startIndex) return "";
        return text.substring(startIndex, end);
    }

    private static boolean startsWith(CharCursor cursor, String s) {
        if (cursor == null || s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            int ch = cursor.peek(i);
            if (ch == CharCursor.EOF || (char) ch != s.charAt(i)) return false;
        }
        return true;
    }
}
