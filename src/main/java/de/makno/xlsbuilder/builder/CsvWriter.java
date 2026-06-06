package de.makno.xlsbuilder.builder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Schreibt ein Blatt streamend als CSV (RFC-4180-konformes Quoting, konfigurierbar über
 * {@link CsvOptions}). Reihenfolge: optionale Titelzeilen → Kopfzeile → Datenzeilen → Summenzeile
 * (immer vorberechnet) → Footer-Zeilen. Excel-Format-Codes greifen hier nicht; Werte werden als Text
 * gerendert. Formelspalten sind in CSV leer.
 */
final class CsvWriter {

    private CsvWriter() {}

    static int write(
            OutputStream out,
            List<? extends Column<?>> columns,
            Iterator<Row> rows,
            SummarySpec summary,
            SheetWriteOptions layout,
            CsvOptions options)
            throws IOException {
        Writer writer = new BufferedWriter(new OutputStreamWriter(out, options.charset()));
        if (options.bom()) {
            writer.write(0xFEFF); // BOM (U+FEFF) – wird vom Charset (z. B. UTF-8) als EF BB BF kodiert
        }

        int columnCount = columns.size();
        Map<String, String> placeholders = layout.placeholders();
        Function<String, String> resolver = layout.placeholderResolver();

        // Optionale Titelzeilen (einspaltig).
        if (options.includeTitleRows() && layout.headerLines() != null) {
            for (String line : layout.headerLines()) {
                writeSingle(writer, Placeholders.resolve(line, placeholders, resolver), options);
            }
        }

        // Kopfzeile.
        if (layout.showColumnHeaders()) {
            String[] header = new String[columnCount];
            for (int c = 0; c < columnCount; c++) {
                header[c] = columns.get(c).name();
            }
            writeRecord(writer, header, options);
        }

        // Summen-Akkumulatoren (wie im XlsxWriter, konstanter Speicher).
        BigDecimal[] sums = initSums(columns, summary);

        // Datenzeilen.
        int dataRowCount = 0;
        String[] fields = new String[columnCount];
        while (rows.hasNext()) {
            Row dataRow = rows.next();
            dataRowCount++;
            for (int c = 0; c < columnCount; c++) {
                Object value = dataRow.get(c);
                Column<?> col = columns.get(c);
                if (value == null) {
                    String nullText = col.nullText() != null ? col.nullText() : layout.defaultNullText();
                    fields[c] = nullText != null ? nullText : "";
                } else {
                    fields[c] = render(col.type(), value);
                    if (sums != null && sums[c] != null) {
                        sums[c] = sums[c].add(toBigDecimal(value));
                    }
                }
            }
            writeRecord(writer, fields, options);
        }

        // Summenzeile (immer vorberechnete Werte – CSV kennt keine Formeln).
        if (summary != null) {
            String[] summaryFields = new String[columnCount];
            for (int c = 0; c < columnCount; c++) {
                if (sums[c] != null) {
                    ColumnType type = columns.get(c).type();
                    summaryFields[c] = render(type, summaryValue(type, sums[c]));
                } else if (c == summary.labelColumnIndex()) {
                    summaryFields[c] = summary.labelText();
                } else {
                    summaryFields[c] = "";
                }
            }
            writeRecord(writer, summaryFields, options);
        }

        // Footer-Zeilen (einspaltig) mit dynamischen Platzhaltern ({rowCount}, {sum:Spalte}).
        if (layout.footerLines() != null && !layout.footerLines().isEmpty()) {
            Map<String, String> dynamic = footerPlaceholders(columns, sums, placeholders, dataRowCount);
            for (String line : layout.footerLines()) {
                writeSingle(writer, Placeholders.resolve(line, dynamic, resolver), options);
            }
        }

        writer.flush();
        return dataRowCount;
    }

    private static Map<String, String> footerPlaceholders(
            List<? extends Column<?>> columns, BigDecimal[] sums, Map<String, String> staticValues, int rowCount) {
        Map<String, String> values = new HashMap<>(staticValues);
        values.put("rowCount", Integer.toString(rowCount));
        if (sums != null) {
            for (int c = 0; c < columns.size(); c++) {
                if (sums[c] != null) {
                    ColumnType type = columns.get(c).type();
                    values.put("sum:" + columns.get(c).name(), render(type, summaryValue(type, sums[c])));
                }
            }
        }
        return values;
    }

    private static BigDecimal[] initSums(List<? extends Column<?>> columns, SummarySpec summary) {
        if (summary == null) {
            return null;
        }
        BigDecimal[] sums = new BigDecimal[columns.size()];
        for (int c = 0; c < columns.size(); c++) {
            if (summary.sum()[c]) {
                sums[c] = BigDecimal.ZERO;
            }
        }
        return sums;
    }

    /** Rendert einen Zellwert als CSV-Text (Excel-Format-Codes greifen hier nicht). */
    private static String render(ColumnType type, Object value) {
        return switch (type) {
            case STRING -> value.toString();
            case INTEGER, LONG -> Long.toString(((Number) value).longValue());
            case DOUBLE -> Double.toString(((Number) value).doubleValue());
            case DECIMAL -> (value instanceof BigDecimal bd ? bd : new BigDecimal(value.toString())).toPlainString();
            case BOOLEAN -> Boolean.toString(value instanceof Boolean b ? b : Boolean.parseBoolean(value.toString()));
            case FORMULA -> ""; // CSV kennt keine Formeln
            default -> String.valueOf(value); // DATE/DATETIME/TIME -> ISO (java.time#toString)
        };
    }

    private static Object summaryValue(ColumnType type, BigDecimal sum) {
        return switch (type) {
            case DOUBLE -> sum.doubleValue();
            case INTEGER, LONG -> sum.longValue();
            default -> sum;
        };
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Double || value instanceof Float) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return BigDecimal.valueOf(((Number) value).longValue());
    }

    /** Schreibt einen einspaltigen Datensatz (Titel/Footer). */
    private static void writeSingle(Writer writer, String text, CsvOptions options) throws IOException {
        writer.write(quote(text, options));
        writer.write(options.lineSeparator());
    }

    private static void writeRecord(Writer writer, String[] fields, CsvOptions options) throws IOException {
        StringBuilder line = new StringBuilder();
        for (int c = 0; c < fields.length; c++) {
            if (c > 0) {
                line.append(options.delimiter());
            }
            line.append(quote(fields[c], options));
        }
        line.append(options.lineSeparator());
        writer.write(line.toString());
    }

    /** RFC-4180-Quoting: bei Trennzeichen/Quote/CR/LF in Anführungszeichen setzen, interne Quotes verdoppeln. */
    private static String quote(String value, CsvOptions options) {
        String v = value == null ? "" : value;
        char delim = options.delimiter();
        char q = options.quote();
        boolean needsQuote = v.indexOf(delim) >= 0 || v.indexOf(q) >= 0 || v.indexOf('\n') >= 0 || v.indexOf('\r') >= 0;
        if (!needsQuote) {
            return v;
        }
        String quoteStr = String.valueOf(q);
        return quoteStr + v.replace(quoteStr, quoteStr + quoteStr) + quoteStr;
    }
}
