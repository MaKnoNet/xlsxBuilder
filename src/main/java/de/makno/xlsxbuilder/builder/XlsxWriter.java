package de.makno.xlsxbuilder.builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Schreibt eine {@code .xlsx}-Datei mit Apache POI im Streaming-Modus (SXSSF).
 *
 * <p>SXSSF hält nur ein gleitendes Fenster von Zeilen im Speicher und lagert den
 * Rest auf temporäre Dateien aus; mit Inline-Strings (Default von SXSSF) wächst auch keine
 * Shared-Strings-Tabelle. Dadurch bleibt der Speicherbedarf konstant, unabhängig von der Zeilenzahl.
 * Die Summen der optionalen Summenzeile werden beim Streamen mitgeführt (kein zweiter Durchlauf).
 *
 * <p>Pro Spalte wird einmalig ein {@link CellStyle} mit dem gewünschten Zahlen-/Datumsformat erzeugt
 * (expliziter Format-Code der Spalte oder, für Datums-/Zeittypen, ein Standardformat).
 */
final class XlsxWriter {

    private static final double NANOS_PER_DAY = 86_400d * 1_000_000_000d;

    /** Maximale Länge eines Excel-Blattnamens. */
    private static final int MAX_SHEET_NAME_LENGTH = 31;

    private XlsxWriter() {}

    /**
     * Fügt ein Worksheet in ein vorhandenes (vom {@code WorkbookBuilder} verwaltetes) Workbook ein.
     *
     * @return Anzahl geschriebener Datenzeilen (ohne Titel-/Kopf-/Summenzeile) – für Performance-Logs.
     */
    static int addSheet(
            SXSSFWorkbook wb,
            String sheetName,
            List<? extends Column<?>> columns,
            Iterator<Row> rows,
            SummarySpec summary,
            SheetWriteOptions layout) {
        SXSSFSheet sheet = wb.createSheet(uniqueSheetName(wb, sheetName));
        enableFormulaRecalculationIfNeeded(sheet, columns, summary);

        CreationHelper helper = wb.getCreationHelper();
        CellStyle[] columnStyles = buildColumnStyles(wb, helper, columns);
        CellStyle titleStyle = buildTitleStyle(wb);
        CellStyle footerStyle = buildFooterStyle(wb);
        boolean showHeaders = layout.showColumnHeaders();
        Map<String, String> placeholders = layout.placeholders();
        Function<String, String> resolver = layout.placeholderResolver();
        ColumnWidthEstimator widths = new ColumnWidthEstimator(columns, showHeaders);

        int lastCol = columns.size() - 1;
        int rowNum = writeTitleRows(sheet, layout.headerLines(), placeholders, resolver, titleStyle, lastCol);
        rowNum = writeColumnHeaders(sheet, columns, rowNum, showHeaders);

        BigDecimal[] sums = initSums(columns, summary);
        int firstDataRow0 = rowNum; // 0-basierter Index der ersten Datenzeile
        rowNum = writeDataRows(sheet, columns, rows, columnStyles, widths, sums, rowNum, layout.defaultNullText());

        int dataRowCount = rowNum - firstDataRow0;
        // Excel-Zeilennummern sind 1-basiert: erste Datenzeile = firstDataRow0 + 1, letzte = rowNum.
        rowNum = writeSummaryRow(
                sheet, columns, columnStyles, widths, summary, sums, rowNum, firstDataRow0 + 1, rowNum, dataRowCount);

        writeFooterRows(
                sheet,
                layout.footerLines(),
                placeholders,
                resolver,
                columns,
                sums,
                dataRowCount,
                footerStyle,
                lastCol,
                rowNum);

        widths.applyTo(sheet);
        return dataRowCount;
    }

    /** Bei Formelspalten/-summen Excel anweisen, beim Öffnen neu zu berechnen (Werte sind nicht gecacht). */
    private static void enableFormulaRecalculationIfNeeded(
            SXSSFSheet sheet, List<? extends Column<?>> columns, SummarySpec summary) {
        boolean hasFormula = false;
        for (Column<?> col : columns) {
            if (col.type() == ColumnType.FORMULA) {
                hasFormula = true;
                break;
            }
        }
        if (hasFormula || (summary != null && summary.useFormula())) {
            sheet.setForceFormulaRecalculation(true);
        }
    }

    /** Schreibt die optionalen Titelzeilen (je über die volle Breite zusammengeführt). Gibt die nächste Zeile zurück. */
    private static int writeTitleRows(
            SXSSFSheet sheet,
            List<String> headerLines,
            Map<String, String> placeholders,
            Function<String, String> resolver,
            CellStyle titleStyle,
            int lastCol) {
        int rowNum = 0;
        if (headerLines != null) {
            for (String line : headerLines) {
                Cell cell = sheet.createRow(rowNum).createCell(0);
                cell.setCellValue(Placeholders.resolve(line, placeholders, resolver));
                cell.setCellStyle(titleStyle);
                if (lastCol > 0) {
                    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, lastCol));
                }
                rowNum++;
            }
        }
        return rowNum;
    }

    /** Schreibt die Spaltenüberschriften (sofern aktiviert). Gibt die nächste Zeile zurück. */
    private static int writeColumnHeaders(
            SXSSFSheet sheet, List<? extends Column<?>> columns, int rowNum, boolean showColumnHeaders) {
        if (!showColumnHeaders) {
            return rowNum;
        }
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
        for (int c = 0; c < columns.size(); c++) {
            headerRow.createCell(c).setCellValue(columns.get(c).name());
        }
        return rowNum;
    }

    /** Akkumulatoren der Summenzeile (konstanter Speicher, wird beim Streamen mitgeführt) oder {@code null}. */
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

    /** Streamt die Datenzeilen, misst dabei die Spaltenbreiten und führt die Summen mit. Gibt die nächste Zeile zurück. */
    private static int writeDataRows(
            SXSSFSheet sheet,
            List<? extends Column<?>> columns,
            Iterator<Row> rows,
            CellStyle[] columnStyles,
            ColumnWidthEstimator widths,
            BigDecimal[] sums,
            int rowNum,
            String defaultNullText) {
        while (rows.hasNext()) {
            Row dataRow = rows.next();
            org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowNum++);
            for (int c = 0; c < columns.size(); c++) {
                Object value = dataRow.get(c);
                Column<?> col = columns.get(c);
                if (value == null) {
                    // Null-Wert-Handler: spalten-spezifischer Platzhalter vor sheet-weitem Default.
                    String nullText = col.nullText() != null ? col.nullText() : defaultNullText;
                    if (nullText != null) {
                        r.createCell(c).setCellValue(nullText);
                        widths.ensureAtLeast(c, nullText.length());
                    } else {
                        // Ohne Platzhalter: explizit eine leere Zelle (Excel-Zelltyp BLANK/"Empty") anlegen.
                        r.createCell(c, CellType.BLANK);
                    }
                    continue;
                }
                ColumnType type = col.type();
                writeCell(r, c, type, value, columnStyles[c]);
                widths.track(c, value);
                if (sums != null && sums[c] != null) {
                    sums[c] = sums[c].add(toBigDecimal(value));
                }
            }
        }
        return rowNum;
    }

    /**
     * Schreibt die optionale Summenzeile (vorberechneter Wert oder echte {@code =SUM(...)}-Formel).
     * Gibt die nächste freie Zeile zurück (für die Footer-Zeilen).
     */
    private static int writeSummaryRow(
            SXSSFSheet sheet,
            List<? extends Column<?>> columns,
            CellStyle[] columnStyles,
            ColumnWidthEstimator widths,
            SummarySpec summary,
            BigDecimal[] sums,
            int rowNum,
            int firstDataRowNum,
            int lastDataRowNum,
            int dataRowCount) {
        if (summary == null) {
            return rowNum;
        }
        org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowNum);
        boolean asFormula = summary.useFormula() && dataRowCount > 0;
        for (int c = 0; c < columns.size(); c++) {
            ColumnType type = columns.get(c).type();
            if (sums[c] != null) {
                Object value = summaryValue(type, sums[c]); // für Breitenschätzung
                if (asFormula) {
                    String col = CellReference.convertNumToColString(c);
                    Cell cell = r.createCell(c);
                    cell.setCellFormula("SUM(" + col + firstDataRowNum + ":" + col + lastDataRowNum + ")");
                    if (columnStyles[c] != null) {
                        cell.setCellStyle(columnStyles[c]);
                    }
                } else {
                    writeCell(r, c, type, value, columnStyles[c]);
                }
                widths.track(c, value);
            } else if (c == summary.labelColumnIndex()) {
                r.createCell(c).setCellValue(summary.labelText());
                widths.ensureAtLeast(c, summary.labelText().length());
            }
        }
        return rowNum + 1;
    }

    /**
     * Schreibt die optionalen Footer-Zeilen (je über die volle Breite zusammengeführt). Löst dabei die
     * dynamischen Platzhalter {@code {rowCount}} und {@code {sum:Spalte}} (zusätzlich zu den statischen) auf.
     */
    private static void writeFooterRows(
            SXSSFSheet sheet,
            List<String> footerLines,
            Map<String, String> staticPlaceholders,
            Function<String, String> resolver,
            List<? extends Column<?>> columns,
            BigDecimal[] sums,
            int dataRowCount,
            CellStyle footerStyle,
            int lastCol,
            int rowNum) {
        if (footerLines == null || footerLines.isEmpty()) {
            return;
        }
        Map<String, String> values = new HashMap<>(staticPlaceholders);
        values.put("rowCount", Integer.toString(dataRowCount));
        if (sums != null) {
            for (int c = 0; c < columns.size(); c++) {
                if (sums[c] != null) {
                    values.put(
                            "sum:" + columns.get(c).name(),
                            sumAsText(columns.get(c).type(), sums[c]));
                }
            }
        }
        for (String line : footerLines) {
            Cell cell = sheet.createRow(rowNum).createCell(0);
            cell.setCellValue(Placeholders.resolve(line, values, resolver));
            cell.setCellStyle(footerStyle);
            if (lastCol > 0) {
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, lastCol));
            }
            rowNum++;
        }
    }

    /** Textdarstellung einer Summe für {@code {sum:Spalte}}-Platzhalter. */
    private static String sumAsText(ColumnType type, BigDecimal sum) {
        return switch (type) {
            case DOUBLE -> Double.toString(sum.doubleValue());
            case INTEGER, LONG -> Long.toString(sum.longValue());
            default -> sum.toPlainString();
        };
    }

    /** Erzeugt einen für das Workbook eindeutigen, gültigen Blattnamen (Excel verbietet Duplikate). */
    private static String uniqueSheetName(SXSSFWorkbook wb, String sheetName) {
        String base =
                WorkbookUtil.createSafeSheetName((sheetName == null || sheetName.isBlank()) ? "Sheet1" : sheetName);
        String unique = base;
        int n = 2;
        while (wb.getSheet(unique) != null) {
            String suffix = " (" + n++ + ")";
            String head = base.length() + suffix.length() > MAX_SHEET_NAME_LENGTH
                    ? base.substring(0, MAX_SHEET_NAME_LENGTH - suffix.length())
                    : base;
            unique = head + suffix;
        }
        return unique;
    }

    /** Erzeugt je Spalte einmalig den Style mit Format-Code (oder {@code null}, falls kein Format nötig). */
    private static CellStyle[] buildColumnStyles(
            SXSSFWorkbook wb, CreationHelper helper, List<? extends Column<?>> columns) {
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

    /** Anzahl Stellen des ganzzahligen Anteils (ohne Vorzeichen), ohne Zwischen-Strings zu allokieren. */
    private static int integerDigits(Object value) {
        if (value instanceof BigDecimal bd) {
            // precision - scale = Anzahl Vorkommastellen; für |x| < 1 mindestens eine ("0").
            return Math.max(1, bd.precision() - bd.scale());
        }
        long magnitude;
        if (value instanceof Double || value instanceof Float) {
            magnitude = (long) Math.abs(((Number) value).doubleValue());
        } else {
            magnitude = Math.abs(((Number) value).longValue());
        }
        return decimalDigits(magnitude);
    }

    /** Anzahl Dezimalstellen eines nicht-negativen {@code long} (allokationsfrei). */
    private static int decimalDigits(long v) {
        int digits = 1;
        while (v >= 10) {
            v /= 10;
            digits++;
        }
        return digits;
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
            case DATETIME -> 18; // "31.12.2026 23:59"
            case DECIMAL, DOUBLE, FORMULA -> 14; // Währung/Dezimal mit Tausendertrennung
            case INTEGER, LONG -> 12;
            case DATE -> 11; // "31.12.2026"
            case TIME -> 8; // "23:59:59"
            case BOOLEAN -> 7;
            default -> 10; // STRING
        };
    }

    /**
     * Liefert den Standard-Excel-Format-Code für Datums- und Zeittypen.
     * Hinweis: Excel-Format-Codes verwenden {@code mm} für Monat (nicht {@code MM}) und
     * {@code hh} für 12-Stunden-Format (oder {@code HH} für 24-Stunden-Format).
     */
    private static String defaultFormat(ColumnType type) {
        return switch (type) {
            case DATE -> "yyyy-mm-dd"; // ISO 8601 Standardformat für Datumsanzeige
            case DATETIME -> "yyyy-mm-dd hh:mm"; // Datum + Zeit (12h Format)
            case TIME -> "hh:mm:ss"; // Zeit (12h Format mit Sekunden)
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

    private static CellStyle buildFooterStyle(SXSSFWorkbook wb) {
        Font footerFont = wb.createFont();
        footerFont.setItalic(true);
        footerFont.setFontHeightInPoints((short) 10);
        CellStyle footerStyle = wb.createCellStyle();
        footerStyle.setFont(footerFont);
        footerStyle.setAlignment(HorizontalAlignment.CENTER);
        return footerStyle;
    }

    private static void writeCell(
            org.apache.poi.ss.usermodel.Row row, int col, ColumnType type, Object value, CellStyle style) {
        if (value == null) {
            return; // leere Zelle gar nicht erst anlegen
        }
        Cell cell = row.createCell(col);
        switch (type) {
            case STRING -> cell.setCellValue(String.valueOf(value));
            case BOOLEAN -> cell.setCellValue(value instanceof Boolean b ? b : Boolean.parseBoolean(value.toString()));
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

    /**
     * Schätzt die Spaltenbreiten inhaltsbasiert, damit nichts als "#####" erscheint. Da SXSSF-Autosize
     * ausgelagerte Zeilen nicht sehen kann, wird die Breite beim Streamen mitgemessen: Stringlängen
     * exakt, Zahlenbreite aus Stellenzahl + Format (inkl. der großen Summenwerte). Die endgültige
     * Breite wird erst nach allen Zeilen gesetzt (in SXSSF jederzeit möglich).
     */
    private static final class ColumnWidthEstimator {

        private static final int POI_UNITS_PER_CHAR = 256; // POI misst Breiten in 1/256 Zeichen
        private static final int MAX_WIDTH_CHARS = 255; // Excel-Obergrenze je Spalte
        private static final int PADDING_CHARS = 2; // etwas Polster gegen abgeschnittene Werte

        private final ColumnType[] types;
        private final int[] widthChars;
        private final int[] formatDecimals;
        private final boolean[] grouping;
        private final int[] literalChars;

        ColumnWidthEstimator(List<? extends Column<?>> columns, boolean showColumnHeaders) {
            int n = columns.size();
            types = new ColumnType[n];
            widthChars = new int[n];
            formatDecimals = new int[n];
            grouping = new boolean[n];
            literalChars = new int[n];
            for (int c = 0; c < n; c++) {
                Column<?> col = columns.get(c);
                types[c] = col.type();
                String fmt = col.format() != null ? col.format() : defaultFormat(col.type());
                boolean numericLike = isNumericLike(col.type());
                formatDecimals[c] = decimalsOf(fmt);
                grouping[c] = numericLike && fmt != null && fmt.indexOf(',') >= 0;
                literalChars[c] = numericLike ? literalCharsOf(fmt) : 0;
                int nameWidth = showColumnHeaders ? col.name().length() : 0;
                widthChars[c] = Math.max(nameWidth, minChars(col.type()) + literalChars[c]);
            }
        }

        /** Aktualisiert die geschätzte Breite einer Spalte anhand des konkret geschriebenen Werts. */
        void track(int c, Object value) {
            if (value == null) {
                return;
            }
            ColumnType type = types[c];
            int chars;
            switch (type) {
                case STRING -> chars = value.toString().length();
                case INTEGER, LONG, DOUBLE, DECIMAL -> {
                    int intDigits = integerDigits(value);
                    int dec = formatDecimals[c] >= 0
                            ? formatDecimals[c]
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
            ensureAtLeast(c, chars);
        }

        /** Stellt sicher, dass die Spalte mindestens {@code chars} Zeichen breit ist. */
        void ensureAtLeast(int c, int chars) {
            if (chars > widthChars[c]) {
                widthChars[c] = chars;
            }
        }

        /** Setzt die ermittelten Breiten endgültig auf das Blatt (Zeichen -> POI-Einheiten, mit Polster). */
        void applyTo(SXSSFSheet sheet) {
            for (int c = 0; c < widthChars.length; c++) {
                int units = Math.min(
                        (widthChars[c] + PADDING_CHARS) * POI_UNITS_PER_CHAR, MAX_WIDTH_CHARS * POI_UNITS_PER_CHAR);
                sheet.setColumnWidth(c, units);
            }
        }
    }
}
