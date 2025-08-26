package service;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import core.highlight.ColorPalette;
import core.io.ConfigLoader;
import model.config.Config;

public class HighlightServiceTest {

    private static Config loadConfig() throws Exception {
        return new ConfigLoader().load(Path.of("resources/config.json"));
    }

    @Test
    void colorsCommentAndErrorTokens() throws Exception {
        Config cfg = loadConfig();
        HighlightService hs = new HighlightService(cfg);

        List<HighlightService.HighlightSpan> spans = hs.highlight("// a");
        assertEquals(ColorPalette.COMMENT, spans.get(0).color());

        spans = hs.highlight("@");
        assertEquals(ColorPalette.ERROR, spans.get(0).color());
    }
}
