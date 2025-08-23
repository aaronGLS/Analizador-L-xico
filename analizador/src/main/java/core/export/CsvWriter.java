package core.export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Escritor CSV simple con:
 * - Separador coma (,)
 * - Codificación UTF-8
 * - Escapado de comillas dobles según RFC4180 (doblando comillas y envolviendo
 * en comillas)
 *
 * No implementa encabezados opcionales avanzados ni BOM; es la mínima
 * funcionalidad que requiere la práctica.
 */
public final class CsvWriter {

    /**
     * Escribe un CSV con encabezados y filas en la ruta indicada.
     * 
     * @param path    ruta del archivo destino
     * @param headers arreglo de encabezados (no null)
     * @param rows    matriz de filas (cada fila es un arreglo de columnas)
     * @throws IOException si ocurre un error de E/S
     */
    public void write(Path path, String[] headers, String[][] rows) throws IOException {
        if (path == null)
            throw new IllegalArgumentException("path no puede ser null");
        if (headers == null)
            throw new IllegalArgumentException("headers no puede ser null");
        if (rows == null)
            throw new IllegalArgumentException("rows no puede ser null");

        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (BufferedWriter bw = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // encabezados
            writeRow(bw, headers);
            // filas
            for (String[] row : rows) {
                writeRow(bw, row);
            }
        }
    }

    private void writeRow(BufferedWriter bw, String[] cols) throws IOException {
        for (int i = 0; i < cols.length; i++) {
            if (i > 0)
                bw.write(',');
            bw.write(escape(cols[i] == null ? "" : cols[i]));
        }
        bw.write('\n');
    }

    private String escape(String s) {
        // Si contiene coma, comilla o salto de línea, envolver en comillas y doblar
        // comillas
        boolean needsQuotes = s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0
                || s.indexOf('\r') >= 0;
        if (!needsQuotes)
            return s;
        String escaped = s.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
