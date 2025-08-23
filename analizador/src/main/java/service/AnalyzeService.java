package service;



import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import core.io.TextLoader;
import core.lexing.LexerEngine;
import model.config.Config;
import model.lexical.LexError;
import model.lexical.Token;

/**
 * Fachada mínima para ejecutar el análisis léxico sobre un texto,
 * de acuerdo con la configuración dinámica (config.json).
 *
 * Responsabilidades:
 *  - Recibir texto (o cargarlo desde archivo) y delegar a {@link LexerEngine}.
 *  - Retornar las listas de tokens y errores (sin UI, sin reportes aquí).
 *
 * Restricciones:
 *  - No realiza coloreo, reportes ni exportaciones (eso se implementa en otras capas).
 *  - No altera el comportamiento del lexer (comentarios se ignoran en la salida).
 */
public final class AnalyzeService {

    private final Config config;

    public AnalyzeService(Config config) {
        this.config = Objects.requireNonNull(config, "config no puede ser null");
    }

    /**
     * Analiza un texto en memoria.
     * @param text contenido a analizar
     * @return resultado con tokens y errores (listas inmutables)
     */
    public Result analyzeText(String text) {
        var lexer = new LexerEngine(config);
        var res = lexer.analyze(text);
        return new Result(res.tokens(), res.errors());
    }

    /**
     * Carga un archivo de texto (UTF-8) y lo analiza.
     * @param path ruta del archivo
     * @return resultado con tokens y errores
     * @throws IOException si falla la lectura del archivo
     */
    public Result analyzeFile(Path path) throws IOException {
        var loader = new TextLoader();
        String text = loader.load(path);
        return analyzeText(text);
    }

    /** DTO simple para exponer el resultado (tokens + errores). */
    public record Result(List<Token> tokens, List<LexError> errors) { }
}
