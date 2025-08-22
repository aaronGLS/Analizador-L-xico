package core.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import model.config.Config;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Cargador de la configuración desde un archivo JSON (config.json).
 * - Se limita a deserializar los campos exigidos.
 * - No implementa lógica de análisis ni validaciones adicionales.
 */
public final class ConfigLoader {

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    /**
     * Lee el archivo JSON indicado y retorna un objeto Config válido.
     * @param path ruta a config.json
     * @return Config cargada
     * @throws IOException si hay problemas de lectura, valió xd
     * @throws IllegalArgumentException si faltan secciones obligatorias(por ejemplo alguna regla)
     */
    public Config load(Path path) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Config cfg = gson.fromJson(br, Config.class);
            if (cfg == null) {
                throw new IllegalArgumentException("El archivo de configuración está vacío o es inválido.");
            }
            cfg.validate(); 
            return cfg;
        }
    }
}
