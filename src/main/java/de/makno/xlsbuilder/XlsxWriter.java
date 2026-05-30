package de.makno.xlsbuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Schreibt eine {@code .xlsx}-Datei mit reinem JDK (ZIP + OOXML-XML), ohne externe Bibliothek.
 * Die Datenzeilen werden direkt als XML in den Worksheet-ZIP-Eintrag gestreamt (Inline-Strings,
 * kein In-Memory-Workbook, keine Shared-Strings-Tabelle) – dadurch konstanter Speicherbedarf
 * unabhängig von der Zeilenzahl.
 */
final class XlsxWriter {

    private static final String NS_MAIN =
            "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String NS_REL =
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

    // Style-Indizes aus styles() unten: 0 = allgemein, 1 = Datum, 2 = Datum+Zeit, 3 = Titel.
    private static final int STYLE_DATE = 1;
    private static final int STYLE_DATETIME = 2;
    private static final int STYLE_TITLE = 3;

    // Excel-Datums-Basis (wegen des 1900-Schaltjahr-Bugs auf 1899-12-30 gesetzt).
    private static final LocalDate EXCEL_EPOCH = LocalDate.of(1899, 12, 30);
    private static final double SECONDS_PER_DAY = 86_400.0;

    private XlsxWriter() {
    }

    static void write(OutputStream out, String sheetName, List<? extends Column<?>> columns,
                      List<String> headerLines, Iterator<Row> rows, SummarySpec summary)
            throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(new BufferedOutputStream(out))) {
            writeEntry(zip, "[Content_Types].xml", contentTypes());
            writeEntry(zip, "_rels/.rels", rootRels());
            writeEntry(zip, "xl/workbook.xml", workbook(sheetName));
            writeEntry(zip, "xl/_rels/workbook.xml.rels", workbookRels());
            writeEntry(zip, "xl/styles.xml", styles());
            writeSheet(zip, columns, headerLines, rows, summary);
        }
    }

    private static void writeEntry(ZipOutputStream zip, String name, String content)
            throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static void writeSheet(ZipOutputStream zip, List<? extends Column<?>> columns,
                                   List<String> headerLines, Iterator<Row> rows, SummarySpec summary)
            throws IOException {
        zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
        // Writer NICHT schließen – das würde den ZipOutputStream schließen. Nur flushen.
        Writer w = new BufferedWriter(new OutputStreamWriter(zip, StandardCharsets.UTF_8));
        w.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        w.write("<worksheet xmlns=\"" + NS_MAIN + "\"><sheetData>");

        int rowNum = 0;
        List<String> mergeRefs = new ArrayList<>();
        String lastCol = columnLetter(columns.size() - 1);

        // Optionale Titelzeile(n) oben – je über die volle Tabellenbreite zusammengeführt + zentriert.
        if (headerLines != null) {
            for (String line : headerLines) {
                rowNum++;
                w.write("<row r=\"" + rowNum + "\">");
                writeStyledInlineString(w, cellRef(0, rowNum), line, STYLE_TITLE);
                w.write("</row>");
                if (columns.size() > 1) {
                    mergeRefs.add("A" + rowNum + ":" + lastCol + rowNum);
                }
            }
        }

        // Spaltenüberschriften
        rowNum++;
        w.write("<row r=\"" + rowNum + "\">");
        for (int c = 0; c < columns.size(); c++) {
            writeInlineString(w, cellRef(c, rowNum), columns.get(c).name());
        }
        w.write("</row>");

        // Akkumulatoren der Summenzeile (konstanter Speicher, wird beim Streamen mitgeführt).
        BigDecimal[] sums = null;
        if (summary != null) {
            sums = new BigDecimal[columns.size()];
            for (int c = 0; c < columns.size(); c++) {
                if (summary.sum()[c]) {
                    sums[c] = BigDecimal.ZERO;
                }
            }
        }

        // Datenzeilen
        while (rows.hasNext()) {
            Row row = rows.next();
            rowNum++;
            w.write("<row r=\"" + rowNum + "\">");
            for (int c = 0; c < columns.size(); c++) {
                Object value = row.get(c);
                writeCell(w, cellRef(c, rowNum), columns.get(c).type(), value);
                if (sums != null && sums[c] != null && value != null) {
                    sums[c] = sums[c].add(toBigDecimal(value));
                }
            }
            w.write("</row>");
        }

        if (summary != null) {
            rowNum++;
            writeSummaryRow(w, columns, summary, sums, rowNum);
        }

        w.write("</sheetData>");
        if (!mergeRefs.isEmpty()) {
            w.write("<mergeCells count=\"" + mergeRefs.size() + "\">");
            for (String ref : mergeRefs) {
                w.write("<mergeCell ref=\"" + ref + "\"/>");
            }
            w.write("</mergeCells>");
        }
        w.write("</worksheet>");
        w.flush();
        zip.closeEntry();
    }

    private static void writeSummaryRow(Writer w, List<? extends Column<?>> columns,
                                        SummarySpec summary, BigDecimal[] sums, int rowNum)
            throws IOException {
        w.write("<row r=\"" + rowNum + "\">");
        for (int c = 0; c < columns.size(); c++) {
            String ref = cellRef(c, rowNum);
            if (sums[c] != null) {
                writeCell(w, ref, columns.get(c).type(), summaryValue(columns.get(c).type(), sums[c]));
            } else if (c == summary.labelColumnIndex()) {
                writeInlineString(w, ref, summary.labelText());
            } else {
                w.write("<c r=\"" + ref + "\"/>");
            }
        }
        w.write("</row>");
    }

    /** Liefert den summierten Wert im passenden Java-Typ, damit {@link #writeCell} ihn korrekt formatiert. */
    private static Object summaryValue(ColumnType type, BigDecimal sum) {
        return switch (type) {
            case DECIMAL -> sum;
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

    private static void writeCell(Writer w, String ref, ColumnType type, Object value)
            throws IOException {
        if (value == null) {
            w.write("<c r=\"" + ref + "\"/>");
            return;
        }
        switch (type) {
            case STRING -> writeInlineString(w, ref, String.valueOf(value));
            case BOOLEAN -> {
                boolean b = (value instanceof Boolean bool)
                        ? bool : Boolean.parseBoolean(value.toString());
                w.write("<c r=\"" + ref + "\" t=\"b\"><v>" + (b ? "1" : "0") + "</v></c>");
            }
            case INTEGER, LONG -> {
                long n = ((Number) value).longValue();
                w.write("<c r=\"" + ref + "\"><v>" + n + "</v></c>");
            }
            case DOUBLE -> {
                double d = ((Number) value).doubleValue();
                w.write("<c r=\"" + ref + "\"><v>" + d + "</v></c>");
            }
            case DECIMAL -> {
                String s = (value instanceof BigDecimal bd)
                        ? bd.toPlainString() : value.toString();
                w.write("<c r=\"" + ref + "\"><v>" + s + "</v></c>");
            }
            case DATE -> w.write("<c r=\"" + ref + "\" s=\"" + STYLE_DATE + "\"><v>"
                    + toExcelSerial(value, false) + "</v></c>");
            case DATETIME -> w.write("<c r=\"" + ref + "\" s=\"" + STYLE_DATETIME + "\"><v>"
                    + toExcelSerial(value, true) + "</v></c>");
        }
    }

    private static void writeInlineString(Writer w, String ref, String text) throws IOException {
        w.write("<c r=\"" + ref + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">");
        escapeXml(w, text);
        w.write("</t></is></c>");
    }

    private static void writeStyledInlineString(Writer w, String ref, String text, int style)
            throws IOException {
        w.write("<c r=\"" + ref + "\" s=\"" + style + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">");
        escapeXml(w, text);
        w.write("</t></is></c>");
    }

    private static double toExcelSerial(Object value, boolean withTime) {
        LocalDateTime dt;
        if (value instanceof LocalDate d) {
            dt = d.atStartOfDay();
        } else if (value instanceof LocalDateTime ldt) {
            dt = ldt;
        } else if (value instanceof Date date) {
            dt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        } else {
            throw new IllegalArgumentException(
                    "Nicht unterstützter Datumstyp: " + value.getClass().getName());
        }
        long days = ChronoUnit.DAYS.between(EXCEL_EPOCH, dt.toLocalDate());
        if (!withTime) {
            return days;
        }
        double fraction = dt.toLocalTime().toSecondOfDay() / SECONDS_PER_DAY;
        return days + fraction;
    }

    static String cellRef(int colIndex, int rowNum) {
        return columnLetter(colIndex) + rowNum;
    }

    // 0-basierter Spaltenindex -> A1-Spaltenbuchstaben (A, B, ..., Z, AA, AB, ...).
    static String columnLetter(int colIndex) {
        StringBuilder sb = new StringBuilder();
        int n = colIndex;
        while (n >= 0) {
            sb.insert(0, (char) ('A' + n % 26));
            n = n / 26 - 1;
        }
        return sb.toString();
    }

    private static void escapeXml(Writer w, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&' -> w.write("&amp;");
                case '<' -> w.write("&lt;");
                case '>' -> w.write("&gt;");
                default -> {
                    // Steuerzeichen sind in XML 1.0 ungültig (außer Tab/CR/LF) -> ersetzen.
                    if (ch < 0x20 && ch != '\t' && ch != '\n' && ch != '\r') {
                        w.write(' ');
                    } else {
                        w.write(ch);
                    }
                }
            }
        }
    }

    private static String escapeAttr(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                default -> sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static String sanitizeSheetName(String name) {
        String cleaned = (name == null || name.isBlank()) ? "Sheet1" : name;
        cleaned = cleaned.replaceAll("[:\\\\/?*\\[\\]]", " ");
        if (cleaned.length() > 31) {
            cleaned = cleaned.substring(0, 31);
        }
        return cleaned;
    }

    // ---- statische OOXML-Teile ----

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "</Types>";
    }

    private static String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"" + NS_REL + "/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    private static String workbook(String sheetName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<workbook xmlns=\"" + NS_MAIN + "\" xmlns:r=\"" + NS_REL + "\">"
                + "<sheets><sheet name=\"" + escapeAttr(sanitizeSheetName(sheetName))
                + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                + "</workbook>";
    }

    private static String workbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"" + NS_REL + "/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"" + NS_REL + "/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";
    }

    private static String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<styleSheet xmlns=\"" + NS_MAIN + "\">"
                + "<numFmts count=\"2\">"
                + "<numFmt numFmtId=\"164\" formatCode=\"yyyy\\-mm\\-dd\"/>"
                + "<numFmt numFmtId=\"165\" formatCode=\"yyyy\\-mm\\-dd\\ hh:mm:ss\"/>"
                + "</numFmts>"
                + "<fonts count=\"2\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"14\"/><name val=\"Calibri\"/></font>"
                + "</fonts>"
                + "<fills count=\"1\"><fill><patternFill patternType=\"none\"/></fill></fills>"
                + "<borders count=\"1\"><border/></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"4\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "<xf numFmtId=\"165\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\">"
                + "<alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
    }
}
