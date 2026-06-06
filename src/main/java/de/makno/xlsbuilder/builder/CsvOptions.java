package de.makno.xlsbuilder.builder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Konfiguration für den CSV-Export ({@link ExcelBuilder#writeCsv}). Unveränderlich; Varianten werden
 * über die {@code with…}-Methoden erzeugt.
 *
 * @param delimiter        Feldtrennzeichen (Default {@code ,})
 * @param charset          Zeichensatz (Default UTF-8)
 * @param bom              UTF-8-BOM voranstellen (hilft älterem Excel bei Umlauten; Default {@code false})
 * @param lineSeparator    Zeilentrenner (Default {@code "\r\n"} gemäß RFC 4180)
 * @param quote            Anführungszeichen für zu schützende Felder (Default {@code "})
 * @param includeTitleRows Titelzeilen ({@link ExcelBuilder#header}) als CSV-Zeilen ausgeben (Default {@code false})
 */
public record CsvOptions(
        char delimiter, Charset charset, boolean bom, String lineSeparator, char quote, boolean includeTitleRows) {

    public CsvOptions {
        Objects.requireNonNull(charset, "charset");
        Objects.requireNonNull(lineSeparator, "lineSeparator");
    }

    /** RFC-4180-Default: Komma, UTF-8, CRLF, ohne BOM, ohne Titelzeilen. */
    public static final CsvOptions DEFAULT = new CsvOptions(',', StandardCharsets.UTF_8, false, "\r\n", '"', false);

    /** Excel-DE-freundlich: Semikolon + UTF-8-BOM (öffnet in deutschem Excel direkt korrekt). */
    public static CsvOptions excelGerman() {
        return new CsvOptions(';', StandardCharsets.UTF_8, true, "\r\n", '"', false);
    }

    public CsvOptions withDelimiter(char delimiter) {
        return new CsvOptions(delimiter, charset, bom, lineSeparator, quote, includeTitleRows);
    }

    public CsvOptions withCharset(Charset charset) {
        return new CsvOptions(delimiter, charset, bom, lineSeparator, quote, includeTitleRows);
    }

    public CsvOptions withBom(boolean bom) {
        return new CsvOptions(delimiter, charset, bom, lineSeparator, quote, includeTitleRows);
    }

    public CsvOptions withLineSeparator(String lineSeparator) {
        return new CsvOptions(delimiter, charset, bom, lineSeparator, quote, includeTitleRows);
    }

    public CsvOptions withQuote(char quote) {
        return new CsvOptions(delimiter, charset, bom, lineSeparator, quote, includeTitleRows);
    }

    public CsvOptions withTitleRows(boolean includeTitleRows) {
        return new CsvOptions(delimiter, charset, bom, lineSeparator, quote, includeTitleRows);
    }
}
