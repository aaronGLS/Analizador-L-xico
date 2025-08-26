package core.lexing;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import core.io.ConfigLoader;
import model.config.Config;
import model.lexical.TokenType;

public class LexerEngineTest {

    private static Config loadConfig() throws Exception {
        return new ConfigLoader().load(Path.of("resources/config.json"));
    }

    @Test
    void emitsCommentTokens() throws Exception {
        Config cfg = loadConfig();
        LexerEngine lexer = new LexerEngine(cfg);
        var res = lexer.analyze("// hola");
        assertEquals(1, res.tokens().size());
        var t = res.tokens().get(0);
        assertEquals(TokenType.COMMENT, t.tipo());
        assertEquals("// hola", t.lexema());
        assertTrue(res.errors().isEmpty());
    }

    @Test
    void emitsErrorTokens() throws Exception {
        Config cfg = loadConfig();
        LexerEngine lexer = new LexerEngine(cfg);
        var res = lexer.analyze("@");
        assertEquals(1, res.tokens().size());
        var t = res.tokens().get(0);
        assertEquals(TokenType.ERROR, t.tipo());
        assertEquals("@", t.lexema());
        assertEquals(1, res.errors().size());
    }
}
