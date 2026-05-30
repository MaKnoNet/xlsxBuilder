package de.makno.xlsbuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Schreibt eine {@code .xlsx}-Datei mit Apache POI im Streaming-Modus (SXSSF).
 *
 * <p>SXSSF hält nur ein gleitendes Fenster von {@link #ROW_WINDOW} Zeilen im Speicher und lagert den
 * Rest auf temporäre Dateien aus; mit Inline-Strings (Default von SXSSF) wächst auch keine
 * Shared-Strings-Tabelle. Dadurch bleibt der Speicherbedarf konstant, unabhängig von der Zeilenzahl.
 * Die Summen der optionalen Summenzeile werden beim Streamen mitgeführt (kein zweiter Durchlauf).
 *
 * <p>Pro Spalte wird einmalig ein {@link CellStyle} mit dem gewünschten Zahlen-/Datumsformat erzeugt
 * (expliziter Format-Code der Spalte oder, für Datums-/Zeittypen, ein Standardformat).
 */
final class XlsxWriter {

    /** Anzahl Zeilen, die SXSSF gleichzeitig im Speicher hält (Rest wird auf Platte ausgelagert). */
    private static final int ROW_WINDOW = 100;

    private static final double NANOS_PER_DAY = 86_400d * 1_000_000_000d;

    private XlsxWriter() {
    }

    static void write(OutputStream out, String sheetName, List<? extends Column<?>> columns,
                      List<String> headerLines, Iterator<Row> rows, SummarySpec summary)
            throws IOException {
        SXSSFWorkbook wb = new SXSSFWorkbook(ROW_WINDOW);
        try {
            String safeName = WorkbookUtil.createSafeSheetName(
                    (sheetName == null || sheetName.isBlank()) ? "Sheet1" : sheetName);
            SXSSFSheet sheet = wb.createSheet(safeName);

            // Bei Formelspalten Excel anweisen, beim Öffnen neu zu berechnen (Werte sind nicht gecacht).
            boolean hasFormula = false;
            for (Column<?> col : columns) {
                if (col.type() == ColumnType.FORMULA) {
                    hasFormula = true;
                    break;
                }
            }
            if (hasFormula) {
                sheet.setForceFormulaRecalculation(true);
            }

            CreationHelper helper = wb.getCreationHelper();
            CellStyle[] columnStyles = buildColumnStyles(wb, helper, columns);
            CellStyle titleStyle = buildTitleStyle(wb);

            // Spaltenbreiten inhaltsbasiert schätzen, damit nichts als "#####" erscheint. Da SXSSF-
            // Autosize ausgelagerte Zeilen nicht sehen kann, messen wir die Breite beim Streamen mit:
            // Stringlängen exakt, Zahlenbreite aus Stellenzahl + Format (inkl. der großen Summenwerte).
            // Die endgültige Breite wird erst nach allen Zeilen gesetzt (in SXSSF jederzeit möglich).
            int columnCount = columns.size();
            int[] widthChars = new int[columnCount];
            int[] formatDecimals = new int[columnCount];
            boolean[] grouping = new boolean[columnCount];
            int[] literalChars = new int[columnCount];
            for (int c = 0; c < columnCount; c++) {
                Column<?> col = columns.get(c);
                String fmt = col.format() != null ? col.format() : defaultFormat(col.type());
                boolean numericLike = isNumericLike(col.type());
                formatDecimals[c] = decimalsOf(fmt);
                grouping[c] = numericLike && fmt != null && fmt.indexOf(',') >= 0;
                literalChars[c] = numericLike ? literalCharsOf(fmt) : 0;
                widthChars[c] = Math.max(col.name().length(), minChars(col.type()) + literalChars[c]);
            }

            int rowNum = 0;
            int lastCol = columns.size() - 1;

            // Optionale Titelzeile(n): je über die volle Tabellenbreite zusammengeführt + zentriert.
            if (headerLines != null) {
                for (String line : headerLines) {
                    Cell cell = sheet.createRow(rowNum).createCell(0);
                    cell.setCellValue(line);
                    cell.setCellStyle(titleStyle);
                    if (lastCol > 0) {
                        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, lastCol));
                    }
                    rowNum++;
                }
            }

            // Spaltenüberschriften
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
            for (int c = 0; c < columns.size(); c++) {
                headerRow.createCell(c).setCellValue(columns.get(c).name());
            }

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
                Row dataRow = rows.next();
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowNum++);
                for (int c = 0; c < columns.size(); c++) {
                    Object value = dataRow.get(c);
                    ColumnType type = columns.get(c).type();
                    writeCell(r, c, type, value, columnStyles[c]);
                    trackWidth(widthChars, formatDecimals, grouping, literalChars, c, type, value);
                    if (sums != null && sums[c] != null && value != null) {
                        sums[c] = sums[c].add(toBigDecimal(value));
                    }
                }
            }

            // Summenzeile
            if (summary != null) {
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowNum);
                for (int c = 0; c < columns.size(); c++) {
                    ColumnType type = columns.get(c).type();
                    if (sums[c] != null) {
                        Object value = summaryValue(type, sums[c]);
                        writeCell(r, c, type, value, columnStyles[c]);
                        trackWidth(widthChars, formatDecimals, grouping, literalChars, c, type, value);
                    } else if (c == summary.labelColumnIndex()) {
                        r.createCell(c).setCellValue(summary.labelText());
                        widthChars[c] = Math.max(widthChars[c], summary.labelText().length());
                    }
                }
            }

            // Endgültige Spaltenbreiten setzen (Zeichen -> POI-Einheiten 1/256, +2 Polster).
            for (int c = 0; c < columnCount; c++) {
                sheet.setColumnWidth(c, Math.min((widthChars[c] + 2) * 256, 255 * 256));
            }

            wb.write(out);
        } finally {
            wb.dispose(); // löscht die temporären SXSSF-Dateien
        }
    }

    /** Erzeugt je Spalte einmalig den Style mit Format-Code (oder {@code null}, falls kein Format nötig). */
    private static CellStyle[] buildColumnStyles(SXSSFWorkbook wb, CreationHelper helper,
                                                 List<? extends Column<?>> columns) {
        CellStyle[] styles = new CellStyle[columns.size()];
        for (int c = 0; c < columns.size(); c++) {
            Column<?> col = columns.get(c);
            String format = col.format() != null ? col.format() : defaultFormat(col.type());
            if (format != null) {
                CellStyle style = wb.createCellStyle();
                style.setDataFormat(helper.createDataFormat().getFormat(format));
                styles[c] = style;
            }
        }
        return styles;
    }

    private static boolean isNumericLike(ColumnType type) {
        return switch (type) {
            case INTEGER, LONG, DOUBLE, DECIMAL, FORMULA -> true;
            default -> false;
        };
    }

    /** Aktualisiert die geschätzte Spaltenbreite anhand des konkret geschriebenen Werts. */
    private static void trackWidth(int[] widthChars, int[] formatDecimals, boolean[] grouping,
                                   int[] literalChars, int c, ColumnType type, Object value) {
        if (value == null) {
            return;
        }
        int chars;
        switch (type) {
            case STRING -> chars = value.toString().length();
            case INTEGER, LONG, DOUBLE, DECIMAL -> {
                int intDigits = integerDigits(value);
                int dec = formatDecimals[c] >= 0 ? formatDecimals[c]
                        : (type == ColumnType.INTEGER || type == ColumnType.LONG ? 0 : 2);
                chars = intDigits
                        + (grouping[c] ? (intDigits - 1) / 3 : 0)
                        + (dec > 0 ? 1 + dec : 0)
                        + literalChars[c];
            }
            // DATE/DATETIME/TIME/BOOLEAN/FORMULA: Basisbreite genügt.
            default -> {
                return;
            }
        }
        if (chars > widthChars[c]) {
            widthChars[c] = chars;
        }
    }

    /** Anzahl Stellen des ganzzahligen Anteils (ohne Vorzeichen). */
    private static int integerDigits(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd.signum() == 0 ? 1 : bd.toBigInteger().abs().toString().length();
        }
        if (value instanceof Double || value instanceof Float) {
            return Long.toString((long) Math.abs(((Number) value).doubleValue())).length();
        }
        return Long.toString(Math.abs(((Number) value).longValue())).length();
    }

    /** Dezimalstellen aus einem Format-Code, {@code 0} ohne Dezimalpunkt, {@code -1} ohne Format. */
    private static int decimalsOf(String format) {
        if (format == null) {
            return -1;
        }
        int dot = format.indexOf('.');
        if (dot < 0) {
            return 0;
        }
        int n = 0;
        for (int i = dot + 1; i < format.length(); i++) {
            char ch = format.charAt(i);
            if (ch == '0' || ch == '#') {
                n++;
            } else {
                break;
            }
        }
        return n;
    }

    /** Geschätzte Anzahl sichtbarer Literalzeichen eines Zahlenformats (Währungszeichen, Leerzeichen, %, ...). */
    private static int literalCharsOf(String format) {
        if (format == null) {
            return 0;
        }
        int n = 0;
        boolean inQuote = false;
        for (int i = 0; i < format.length(); i++) {
            char ch = format.charAt(i);
            if (ch == '"') {
                inQuote = !inQuote;
            } else if (ch == '\\') {
                i++;
                n++;
            } else if (inQuote) {
                n++;
            } else if (ch == ' ' || ch == '%' || ch == '€' || ch == '$' || Character.isLetter(ch)) {
                n++;
            }
        }
        return n;
    }

    /** Heuristische Mindestbreite (in Zeichen) je Typ, damit formatierte Werte vollständig sichtbar sind. */
    private static int minChars(ColumnType type) {
        return switch (type) {
            case DATETIME -> 18;                 // "31.12.2026 23:59"
            case DECIMAL, DOUBLE, FORMULA -> 14; // Währung/Dezimal mit Tausendertrennung
            case INTEGER, LONG -> 12;
            case DATE -> 11;                     // "31.12.2026"
            case TIME -> 8;                      // "23:59:59"
            case BOOLEAN -> 7;
            default -> 10;                       // STRING
        };
    }

    private static String defaultFormat(ColumnType type) {
        return switch (type) {
            case DATE -> "yyyy-mm-dd";
            case DATETIME -> "yyyy-mm-dd hh:mm:ss";
            case TIME -> "hh:mm:ss";
            default -> null; // Zahlen ohne explizites Format: Excel-Standard ("General")
        };
    }

    private static CellStyle buildTitleStyle(SXSSFWorkbook wb) {
        Font titleFont = wb.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        CellStyle titleStyle = wb.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return titleStyle;
    }

    private static void writeCell(org.apache.poi.ss.usermodel.Row row, int col, ColumnType type,
                                  Object value, CellStyle style) {
        if (value == null) {
            return; // leere Zelle gar nicht erst anlegen
        }
        Cell cell = row.createCell(col);
        switch (type) {
            case STRING -> cell.setCellValue(String.valueOf(value));
            case BOOLEAN -> cell.setCellValue(
                    value instanceof Boolean b ? b : Boolean.parseBoolean(value.toString()));
            case INTEGER, LONG -> cell.setCellValue(((Number) value).longValue());
            case DOUBLE -> cell.setCellValue(((Number) value).doubleValue());
            case DECIMAL -> cell.setCellValue(
                    (value instanceof BigDecimal bd ? bd : new BigDecimal(value.toString())).doubleValue());
            case DATE, DATETIME -> setDateValue(cell, value);
            case TIME -> setTimeValue(cell, value);
            case FORMULA -> {
                String formula = String.valueOf(value);
                if (formula.startsWith("=")) {
                    formula = formula.substring(1);
                }
                cell.setCellFormula(formula.replace("{row}", Integer.toString(row.getRowNum() + 1)));
            }
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    private static void setDateValue(Cell cell, Object value) {
        if (value instanceof LocalDate d) {
            cell.setCellValue(d);
        } else if (value instanceof LocalDateTime dt) {
            cell.setCellValue(dt);
        } else if (value instanceof Date d) {
            cell.setCellValue(d);
        } else {
            throw new IllegalArgumentException(
                    "Nicht unterstützter Datumstyp: " + value.getClass().getName());
        }
    }

    /** Uhrzeit als Tagesbruchteil (0..1) speichern – Excel stellt das mit einem Zeitformat als Uhrzeit dar. */
    private static void setTimeValue(Cell cell, Object value) {
        LocalTime time;
        if (value instanceof LocalTime t) {
            time = t;
        } else if (value instanceof LocalDateTime dt) {
            time = dt.toLocalTime();
        } else {
            throw new IllegalArgumentException(
                    "Nicht unterstützter Uhrzeit-Typ: " + value.getClass().getName());
        }
        cell.setCellValue(time.toNanoOfDay() / NANOS_PER_DAY);
    }

    /** Liefert den summierten Wert im passenden Java-Typ, damit {@link #writeCell} ihn korrekt schreibt. */
    private static Object summaryValue(ColumnType type, BigDecimal sum) {
        return switch (type) {
            case DOUBLE -> sum.doubleValue();
            case INTEGER, LONG -> sum.longValue();
            default -> sum; // DECIMAL (und andere) als BigDecimal
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
}
