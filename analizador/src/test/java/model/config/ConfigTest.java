package model.config;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {
    @Test
    void validateThrowsWhenSymbolsOverlap() {
        Config cfg = new Config();
        cfg.setOperadores(Set.of("+"));
        cfg.setPuntuacion(Set.of("+",";"));
        cfg.setAgrupacion(Set.of("(", ")"));
        cfg.setComentarios(new CommentsConfig("//", "/*", "*/"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, cfg::validate);
        assertTrue(ex.getMessage().contains("+"));
    }

    @Test
    void validateThrowsWhenSymbolAppearsInAllSets() {
        Config cfg = new Config();
        cfg.setOperadores(Set.of(","));
        cfg.setPuntuacion(Set.of(","));
        cfg.setAgrupacion(Set.of(","));
        cfg.setComentarios(new CommentsConfig("//", "/*", "*/"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, cfg::validate);
        assertTrue(ex.getMessage().contains(","));
    }
}
