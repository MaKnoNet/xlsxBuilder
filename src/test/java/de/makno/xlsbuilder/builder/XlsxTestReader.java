package de.makno.xlsbuilder.builder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * Liest erzeugte {@code .xlsx}-Dateien mit Apache POI zurück und stellt sie als in-memory
 * {@link Grid} bereit (Workbook wird sofort wieder geschlossen). Zur Verifikation in den Tests.
 */
final class XlsxTestReader {

    private XlsxTestReader() {}

    /** Eine einzelne Zelle, typisiert eingelesen. */
    record CellData(
            CellType type,
            String string,
            double number,
            boolean bool,
            boolean dateFormatted,
            LocalDateTime dateTime,
            boolean bold,
            String format,
            String formula) {}

    /** Komplettes Blatt als Zeilen/Spalten plus Metadaten. */
    static final class Grid {
        private final List<List<CellData>> rows;
        private final List<String> mergeRefs;
        private final String sheetName;
        private final int[] columnWidths;

        private Grid(List<List<CellData>> rows, List<String> mergeRefs, String sheetName, int[] columnWidths) {
            this.rows = rows;
            this.mergeRefs = mergeRefs;
            this.sheetName = sheetName;
            this.columnWidths = columnWidths;
        }

        /** Spaltenbreite in POI-Einheiten (1/256 Zeichen). */
        int columnWidth(int c) {
            return columnWidths[c];
        }

        int rowCount() {
            return rows.size();
        }

        String sheetName() {
            return sheetName;
        }

        List<String> mergeRefs() {
            return mergeRefs;
        }

        private CellData cell(int r, int c) {
            List<CellData> row = rows.get(r);
            return c < row.size() ? row.get(c) : null;
        }

        String string(int r, int c) {
            return cell(r, c).string();
        }

        /** Numerischer Wert als long (für INTEGER/LONG-Spalten und ganzzahlige Summen). */
        long number(int r, int c) {
            return (long) cell(r, c).number();
        }

        double dbl(int r, int c) {
            return cell(r, c).number();
        }

        boolean bool(int r, int c) {
            return cell(r, c).bool();
        }

        LocalDateTime dateTime(int r, int c) {
            return cell(r, c).dateTime();
        }

        boolean isDateFormatted(int r, int c) {
            return cell(r, c).dateFormatted();
        }

        boolean bold(int r, int c) {
            return cell(r, c).bold();
        }

        String format(int r, int c) {
            return cell(r, c).format();
        }

        /** Formeltext (ohne {@code =}) oder {@code null}, falls keine Formelzelle. */
        String formula(int r, int c) {
            return cell(r, c).formula();
        }

        /** Alle Zellen einer Zeile als String-Werte (für Kopf-/Überschriftszeilen). */
        List<String> strings(int r) {
            List<String> out = new ArrayList<>();
            for (CellData cd : rows.get(r)) {
                out.add(cd == null ? null : cd.string());
            }
            return out;
        }
    }

    /** Liest das erste Blatt (Index 0). */
    static Grid read(Path xlsx) throws Exception {
        return read(xlsx, 0);
    }

    /** Liest ein bestimmtes Blatt (0-basierter Index). */
    static Grid read(Path xlsx, int sheetIndex) throws Exception {
        try (Workbook wb = WorkbookFactory.create(Files.newInputStream(xlsx))) {
            Sheet sheet = wb.getSheetAt(sheetIndex);

            int maxCols = 0;
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                maxCols = Math.max(maxCols, row.getLastCellNum());
            }

            List<List<CellData>> rows = new ArrayList<>();
            for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(r);
                List<CellData> cells = new ArrayList<>();
                for (int c = 0; c < maxCols; c++) {
                    Cell cell = row == null ? null : row.getCell(c);
                    cells.add(parse(wb, cell));
                }
                rows.add(cells);
            }

            List<String> mergeRefs = new ArrayList<>();
            for (CellRangeAddress region : sheet.getMergedRegions()) {
                mergeRefs.add(region.formatAsString());
            }

            int[] widths = new int[maxCols];
            for (int c = 0; c < maxCols; c++) {
                widths[c] = sheet.getColumnWidth(c);
            }

            return new Grid(rows, mergeRefs, sheet.getSheetName(), widths);
        }
    }

    static String sheetName(Path xlsx) throws Exception {
        return read(xlsx).sheetName();
    }

    /** Namen aller Blätter in Reihenfolge. */
    static List<String> sheetNames(Path xlsx) throws Exception {
        try (Workbook wb = WorkbookFactory.create(Files.newInputStream(xlsx))) {
            List<String> names = new ArrayList<>();
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                names.add(wb.getSheetName(i));
            }
            return names;
        }
    }

    private static CellData parse(Workbook wb, Cell cell) {
        if (cell == null) {
            return new CellData(CellType.BLANK, null, 0, false, false, null, false, null, null);
        }
        boolean bold = false;
        String format = null;
        if (cell.getCellStyle() != null) {
            Font font = wb.getFontAt(cell.getCellStyle().getFontIndex());
            bold = font != null && font.getBold();
            format = cell.getCellStyle().getDataFormatString();
        }
        return switch (cell.getCellType()) {
            case STRING -> new CellData(
                    CellType.STRING, cell.getStringCellValue(), 0, false, false, null, bold, format, null);
            case BOOLEAN -> new CellData(
                    CellType.BOOLEAN, null, 0, cell.getBooleanCellValue(), false, null, bold, format, null);
            case NUMERIC -> {
                boolean dateFmt = DateUtil.isCellDateFormatted(cell);
                yield new CellData(
                        CellType.NUMERIC,
                        null,
                        cell.getNumericCellValue(),
                        false,
                        dateFmt,
                        dateFmt ? cell.getLocalDateTimeCellValue() : null,
                        bold,
                        format,
                        null);
            }
            case FORMULA -> new CellData(
                    CellType.FORMULA, null, 0, false, false, null, bold, format, cell.getCellFormula());
            default -> new CellData(cell.getCellType(), null, 0, false, false, null, bold, format, null);
        };
    }
}
