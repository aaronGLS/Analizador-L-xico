package core.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TextLoaderTest {

    @Test
    void loadNormalizesWindowsNewlines(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("win.txt");
        Files.writeString(file, "a\r\nb", StandardCharsets.UTF_8);

        String content = new TextLoader().load(file);
        assertEquals("a\nb", content);
    }

    @Test
    void loadNormalizesMacNewlines(@TempDir Path tempDir) throws Exception {
        Path file = tempDir.resolve("mac.txt");
        Files.writeString(file, "a\rb", StandardCharsets.UTF_8);

        String content = new TextLoader().load(file);
        assertEquals("a\nb", content);
    }
}

