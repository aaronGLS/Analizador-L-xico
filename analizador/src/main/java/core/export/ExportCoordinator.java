package core.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import core.io.TextSaver;
import model.lexical.LexError;
import model.lexical.Token;
import model.report.LexemeCountRow;

/**
 * Orquesta exportaciones a CSV y a archivo de texto:
 * - Exporta tokens.csv, errores.csv y recuento.csv según lo que exija la
 * práctica.
 * - Exporta el texto de entrada (tal cual) usando TextSaver.
 *
 * No realiza formateo visual ni agrega columnas no requeridas.
 */
public final class ExportCoordinator {

    private final CsvWriter csvWriter = new CsvWriter();
    private final TextSaver textSaver = new TextSaver();

    /**
     * Exporta la tabla de errores a CSV.
     * Columnas: "SimboloOCadena", "Linea", "Columna", "Mensaje"
     */
    public void exportErrors(Path path, List<LexError> errors) throws IOException {
        Objects.requireNonNull(path, "path no puede ser null");
        Objects.requireNonNull(errors, "errors no puede ser null");

        String[] headers = { "SimboloOCadena", "Linea", "Columna", "Mensaje" };
        String[][] rows = new String[errors.size()][headers.length];

        for (int i = 0; i < errors.size(); i++) {
            LexError e = errors.get(i);
            rows[i][0] = e.simboloOCadena();
            rows[i][1] = String.valueOf(e.posicion().linea());
            rows[i][2] = String.valueOf(e.posicion().columna());
            rows[i][3] = e.mensaje();
        }
        csvWriter.write(path, headers, rows);
    }

    /**
     * Exporta la tabla de tokens a CSV (cuando NO hay errores).
     * Columnas: "NombreToken", "Lexema", "Linea", "Columna"
     */
    public void exportTokens(Path path, List<Token> tokens) throws IOException {
        Objects.requireNonNull(path, "path no puede ser null");
        Objects.requireNonNull(tokens, "tokens no puede ser null");

        String[] headers = { "NombreToken", "Lexema", "Linea", "Columna" };
        String[][] rows = new String[tokens.size()][headers.length];

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            rows[i][0] = t.tipo().toString();
            rows[i][1] = t.lexema();
            rows[i][2] = String.valueOf(t.posicion().linea());
            rows[i][3] = String.valueOf(t.posicion().columna());
        }
        csvWriter.write(path, headers, rows);
    }

    /**
     * Exporta la tabla de recuento a CSV (cuando NO hay errores).
     * Columnas: "Lexema", "Tipo", "Cantidad"
     */
    public void exportLexemeCount(Path path, List<LexemeCountRow> countRows) throws IOException {
        Objects.requireNonNull(path, "path no puede ser null");
        Objects.requireNonNull(countRows, "countRows no puede ser null");

        String[] headers = { "Lexema", "Tipo", "Cantidad" };
        String[][] rows = new String[countRows.size()][headers.length];

        for (int i = 0; i < countRows.size(); i++) {
            LexemeCountRow r = countRows.get(i);
            rows[i][0] = r.lexema();
            rows[i][1] = r.tipo().toString();
            rows[i][2] = String.valueOf(r.cantidad());
        }
        csvWriter.write(path, headers, rows);
    }

    /**
     * Exporta el texto de entrada tal cual a un archivo de texto (UTF-8).
     */
    public void exportInputText(Path path, String content) throws IOException {
        textSaver.save(path, content);
    }
}
