package de.makno.xlsbuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
 */
final class XlsxWriter {

    /** Anzahl Zeilen, die SXSSF gleichzeitig im Speicher hält (Rest wird auf Platte ausgelagert). */
    private static final int ROW_WINDOW = 100;

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

            CreationHelper helper = wb.getCreationHelper();
            CellStyle dateStyle = wb.createCellStyle();
            dateStyle.setDataFormat(helper.createDataFormat().getFormat("yyyy-mm-dd"));
            CellStyle dateTimeStyle = wb.createCellStyle();
            dateTimeStyle.setDataFormat(helper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));

            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

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
                    writeCell(r, c, columns.get(c).type(), value, dateStyle, dateTimeStyle);
                    if (sums != null && sums[c] != null && value != null) {
                        sums[c] = sums[c].add(toBigDecimal(value));
                    }
                }
            }

            // Summenzeile
            if (summary != null) {
                org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowNum);
                for (int c = 0; c < columns.size(); c++) {
                    if (sums[c] != null) {
                        writeCell(r, c, columns.get(c).type(),
                                summaryValue(columns.get(c).type(), sums[c]), dateStyle, dateTimeStyle);
                    } else if (c == summary.labelColumnIndex()) {
                        r.createCell(c).setCellValue(summary.labelText());
                    }
                }
            }

            wb.write(out);
        } finally {
            wb.dispose(); // löscht die temporären SXSSF-Dateien
        }
    }

    private static void writeCell(org.apache.poi.ss.usermodel.Row row, int col, ColumnType type,
                                  Object value, CellStyle dateStyle, CellStyle dateTimeStyle) {
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
            case DATE -> {
                setDateValue(cell, value);
                cell.setCellStyle(dateStyle);
            }
            case DATETIME -> {
                setDateValue(cell, value);
                cell.setCellStyle(dateTimeStyle);
            }
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
