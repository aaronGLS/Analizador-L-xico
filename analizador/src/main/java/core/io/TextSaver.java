package core.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Guarda el texto de entrada a un archivo de texto.
 *
 * Requisitos de la práctica:
 *  - "Guardar cambios del texto de entrada": exportar el texto de entrada a un archivo de texto.
 *
 * Decisiones:
 *  - Se utiliza UTF-8.
 *  - Se escribe el contenido tal cual, sin normalizar saltos de línea ni transformar el texto.
 *  - Se crean los directorios padre si no existen.
 *
 * Errores:
 *  - Lanza IOException en problemas de E/S.
 *  - Lanza IllegalArgumentException si la ruta es null o el contenido es null.
 */
public final class TextSaver {

    /**
     * Escribe el contenido de texto (UTF-8) en la ruta indicada, sobrescribiendo si existe.
     *
     * @param path     Ruta del archivo de salida (p. ej. *.txt).
     * @param contenido Texto a guardar.
     * @throws IOException              Si ocurre un error de escritura.
     * @throws IllegalArgumentException Si la ruta o el contenido son null.
     */
    public void save(Path path, String contenido) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser null.");
        }
        if (contenido == null) {
            throw new IllegalArgumentException("El contenido a guardar no puede ser null.");
        }

        // Asegura que exista el directorio destino.
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        // Sobrescribe el archivo (comportamiento esperado para "guardar cambios").
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            bw.write(contenido);
        }
    }
}
