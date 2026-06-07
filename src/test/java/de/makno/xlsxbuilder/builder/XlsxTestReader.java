package de.makno.xlsxbuilder.builder;

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
 * Reads back generated {@code .xlsx} files with Apache POI and exposes them as an in-memory
 * {@link Grid} (the workbook is closed again immediately). For verification in the tests.
 */
final class XlsxTestReader {

    private XlsxTestReader() {}

    /** A single cell, read in typed form. */
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

    /** A complete sheet as rows/columns plus metadata. */
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

        /** Column width in POI units (1/256 of a character). */
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

        /** Numeric value as long (for INTEGER/LONG columns and integer sums). */
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

        /** Formula text (without {@code =}) or {@code null} if not a formula cell. */
        String formula(int r, int c) {
            return cell(r, c).formula();
        }

        /** All cells of a row as string values (for header/title rows). */
        List<String> strings(int r) {
            List<String> out = new ArrayList<>();
            for (CellData cd : rows.get(r)) {
                out.add(cd == null ? null : cd.string());
            }
            return out;
        }
    }

    /** Reads the first sheet (index 0). */
    static Grid read(Path xlsx) throws Exception {
        return read(xlsx, 0);
    }

    /** Reads a specific sheet (0-based index). */
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

    /** Names of all sheets in order. */
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
