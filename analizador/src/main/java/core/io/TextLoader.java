package core.io;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Carga el texto de entrada desde un archivo de texto.
 *
 * Requisitos de la práctica:
 *  - "Carga de archivo de entrada": el usuario puede cargar un archivo para su análisis/edición. 
 *    (La UI lo mostrará; aquí solo proveemos la lectura). 
 *
 * Decisiones:
 *  - Se utiliza UTF-8.
 *  - Se lee carácter por carácter en bloques (char[]) para preservar exactamente el contenido,
 *    incluyendo saltos de línea tal cual existan en el archivo.
 *  - Sin lógica adicional de normalización ni análisis (no corresponde a esta rama).
 *
 * Errores:
 *  - Lanza IOException en problemas de E/S.
 *  - Lanza IllegalArgumentException si la ruta no es legible o no apunta a un archivo regular.
 */
public final class TextLoader {

    /**
     * Lee el contenido completo de un archivo de texto como String (UTF-8).
     *
     * @param path Ruta del archivo de texto a cargar.
     * @return Contenido completo del archivo.
     * @throws IOException              Si ocurre un error de lectura.
     * @throws IllegalArgumentException Si la ruta es inválida, no existe o no es legible.
     */
    public String load(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser null.");
        }
        if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
            throw new IllegalArgumentException("El archivo no existe, no es regular o no es legible: " + path);
        }

        // Lectura por bloques (mantiene exactamente los caracteres del archivo).
        StringBuilder sb = new StringBuilder();
        try (Reader reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
            char[] buffer = new char[8192];
            int n;
            while ((n = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, n);
            }
        }
        return sb.toString();
    }
}
