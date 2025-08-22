package core.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import model.config.Config;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Guardador de la configuración hacia un archivo JSON (config.json).
 * - Solo serializa exactamente los campos definidos en el modelo.
 * - Antes de guardar, ejecuta una validación mínima del modelo.
 */
public final class ConfigSaver {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    /**
     * Escribe el contenido de la Config al archivo indicado en formato JSON.
     * @param path ruta de salida (por convención, config.json)
     * @param cfg  configuración a persistir
     * @throws IOException si hay problemas de escritura
     * @throws IllegalArgumentException si la configuración carece de secciones obligatorias
     */
    public void save(Path path, Config cfg) throws IOException {
        if (cfg == null) {
            throw new IllegalArgumentException("La configuración a guardar no puede ser null.");
        }
        cfg.validate(); // valida presencia de secciones obligatorias

        // Asegura la existencia del directorio padre
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            gson.toJson(cfg, bw);
        }
    }
}
