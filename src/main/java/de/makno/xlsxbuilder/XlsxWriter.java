package de.makno.xlsxbuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.SheetNameFormatter;
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
 * Writes a {@code .xlsx} file with Apache POI in streaming mode (SXSSF).
 *
 * <p>SXSSF keeps only a sliding window of rows in memory and spills the rest to temporary files; with
 * inline strings (the SXSSF default) no shared-strings table grows either. This keeps memory usage
 * constant, independent of the row count. The sums of the optional summary row are accumulated while
 * streaming (no second pass).
 *
 * <p>Per column a {@link CellStyle} with the desired number/date format is created once (the column's
 * explicit format code, or a default format for date/time types).
 *
 * <p><b>Row limit:</b> a worksheet holds at most {@code maxRowsPerSheet} rows (Excel: 1,048,576),
 * including title/group/column-header rows and the rows reserved for the summary row and footers.
 * When the data exceeds that, either a {@link RowLimitExceededException} is thrown (default) or –
 * with {@code splitOnRowLimit} – the table continues on follow-up sheets: title/group/column-header
 * rows are repeated per part sheet, the summary row and footers are written once on the last sheet
 * and cover all part sheets (with a cross-sheet {@code SUM} formula if requested).
 *
 * <p>One instance writes one <em>logical</em> sheet (one or more part sheets); the instance carries
 * the state that spans part sheets (styles, widths, sums, data ranges).
 */
final class XlsxWriter {

    private static final Logger LOG = LogManager.getLogger(XlsxWriter.class);

    private static final double NANOS_PER_DAY = 86_400d * 1_000_000_000d;

    /** Maximum length of an Excel sheet name. */
    private static final int MAX_SHEET_NAME_LENGTH = 31;

    private final SXSSFWorkbook wb;
    private final String baseSheetName;
    private final List<? extends Column<?>> columns;
    private final SummarySpec summary;
    private final SheetWriteOptions layout;

    private final CellStyle[] columnStyles;
    private final CellStyle titleStyle;
    private final CellStyle footerStyle;
    private CellStyle groupHeaderStyle; // lazy: created only when column groups are configured
    private final ColumnWidthEstimator widths;
    private final BigDecimal[] sums;
    /** 0-based index of the first row data must not occupy (reserved for the summary row + footers). */
    private final int firstReservedRowIndex;

    private final List<SXSSFSheet> sheets = new ArrayList<>();
    private final List<DataRange> dataRanges = new ArrayList<>();

    /** 1-based Excel row span of one part sheet's data area (for cross-sheet {@code SUM} formulas). */
    private record DataRange(String sheetName, int firstRowNum, int lastRowNum) {}

    private XlsxWriter(
            SXSSFWorkbook wb,
            String sheetName,
            List<? extends Column<?>> columns,
            SummarySpec summary,
            SheetWriteOptions layout) {
        this.wb = wb;
        this.baseSheetName = sheetName;
        this.columns = columns;
        this.summary = summary;
        this.layout = layout;
        this.columnStyles = buildColumnStyles(wb, wb.getCreationHelper(), columns);
        this.titleStyle = buildTitleStyle(wb);
        this.footerStyle = buildFooterStyle(wb);
        this.widths = new ColumnWidthEstimator(columns, layout.showColumnHeaders());
        this.sums = initSums(columns, summary);
        int trailerRows = (summary != null ? 1 : 0)
                + (layout.footerLines() == null ? 0 : layout.footerLines().size());
        this.firstReservedRowIndex = layout.maxRowsPerSheet() - trailerRows;
    }

    /**
     * Adds a worksheet to an existing workbook (managed by the {@code WorkbookBuilder}). At the row
     * limit the sheet either fails or splits into part sheets (see {@code splitOnRowLimit}).
     *
     * @return number of data rows written across all part sheets (excluding title/header/summary
     *         rows) – for performance logs.
     */
    static int addSheet(
            SXSSFWorkbook wb,
            String sheetName,
            List<? extends Column<?>> columns,
            Iterator<Row> rows,
            SummarySpec summary,
            SheetWriteOptions layout) {
        return new XlsxWriter(wb, sheetName, columns, summary, layout).writeSheets(rows);
    }

    /** Streams all data rows, splitting into part sheets at the row limit if configured. */
    private int writeSheets(Iterator<Row> rows) {
        SXSSFSheet sheet = startSheet();
        int rowNum = writePrelude(sheet);
        int firstDataRow0 = rowNum; // 0-based index of the first data row of the current part sheet
        int totalDataRows = 0;
        while (rows.hasNext()) {
            if (rowNum >= firstReservedRowIndex) {
                if (!layout.splitOnRowLimit()) {
                    throw new RowLimitExceededException("Sheet '" + sheet.getSheetName()
                            + "' exceeds the limit of " + layout.maxRowsPerSheet()
                            + " rows per sheet (incl. title/header rows and reserved summary/footer"
                            + " rows). Use splitOnRowLimit(true) to continue on follow-up sheets.");
                }
                dataRanges.add(new DataRange(sheet.getSheetName(), firstDataRow0 + 1, rowNum));
                String fullSheetName = sheet.getSheetName();
                sheet = startSheet();
                LOG.debug(
                        "Sheet '{}' full ({} rows) - continuing on '{}'", fullSheetName, rowNum, sheet.getSheetName());
                rowNum = writePrelude(sheet);
                firstDataRow0 = rowNum;
            }
            writeDataRow(sheet, rows.next(), rowNum++);
            totalDataRows++;
        }
        dataRanges.add(new DataRange(sheet.getSheetName(), firstDataRow0 + 1, rowNum));
        rowNum = writeSummaryRow(sheet, rowNum, totalDataRows);
        writeFooterRows(sheet, rowNum, totalDataRows);
        for (SXSSFSheet part : sheets) {
            widths.applyTo(part);
        }
        return totalDataRows;
    }

    /** Starts a (part) sheet: unique name, recalculation flag; registered for the final width pass. */
    private SXSSFSheet startSheet() {
        int partNumber = sheets.size() + 1;
        SXSSFSheet sheet = wb.createSheet(partSheetName(partNumber));
        sheets.add(sheet);
        enableFormulaRecalculationIfNeeded(sheet, columns, summary);
        return sheet;
    }

    /**
     * Name of a part sheet: part 1 and the default scheme use {@link #uniqueSheetName} ("Name (2)",
     * ...); a configured {@link SplitSheetNamer} names the follow-up sheets itself. Its result is
     * made Excel-safe but deliberately <em>not</em> deduplicated – a duplicate fails with a clear
     * exception, so the caller stays in control of the actual names.
     */
    private String partSheetName(int partNumber) {
        SplitSheetNamer namer = layout.splitSheetNamer();
        if (partNumber == 1 || namer == null) {
            return uniqueSheetName(wb, baseSheetName);
        }
        String name = namer.partSheetName(baseSheetName, partNumber);
        if (name == null || name.isBlank()) {
            throw new IllegalStateException(
                    "SplitSheetNamer returned no name for part " + partNumber + " of sheet '" + baseSheetName + "'");
        }
        String safeName = WorkbookUtil.createSafeSheetName(name);
        if (wb.getSheet(safeName) != null) {
            throw new IllegalStateException("SplitSheetNamer returned the name '" + safeName + "' for part "
                    + partNumber + " of sheet '" + baseSheetName
                    + "', but a sheet with that name already exists in the workbook");
        }
        return safeName;
    }

    /**
     * Writes the rows above the data (title rows, group header, column headers). Returns the first
     * data row. Guards against a limit that leaves no room for data at all – otherwise the split
     * loop could keep opening empty part sheets (only reachable with the test seam's tiny limits).
     */
    private int writePrelude(SXSSFSheet sheet) {
        int rowNum = writeTitleRows(sheet);
        rowNum = writeColumnGroups(sheet, rowNum);
        rowNum = writeColumnHeaders(sheet, rowNum);
        if (rowNum >= firstReservedRowIndex) {
            throw new IllegalStateException("maxRowsPerSheet=" + layout.maxRowsPerSheet()
                    + " leaves no room for data rows: " + rowNum + " title/header rows plus "
                    + (layout.maxRowsPerSheet() - firstReservedRowIndex) + " reserved summary/footer rows");
        }
        return rowNum;
    }

    /** For formula columns/sums, tell Excel to recompute on open (the values are not cached). */
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

    /** Writes the optional title rows (each merged across the full width). Returns the next row. */
    private int writeTitleRows(SXSSFSheet sheet) {
        int rowNum = 0;
        List<String> headerLines = layout.headerLines();
        if (headerLines != null) {
            int lastCol = columns.size() - 1;
            for (String line : headerLines) {
                Cell cell = sheet.createRow(rowNum).createCell(0);
                cell.setCellValue(Placeholders.resolve(line, layout.placeholders(), layout.placeholderResolver()));
                cell.setCellStyle(titleStyle);
                if (lastCol > 0) {
                    sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, lastCol));
                }
                rowNum++;
            }
        }
        return rowNum;
    }

    /**
     * Writes the optional grouped header row (multi-row / joined headers) above the column headers.
     * Each {@link ColumnGroup} yields one cell at its start column, merged across its span. Returns the
     * next row (unchanged when there are no groups).
     */
    private int writeColumnGroups(SXSSFSheet sheet, int rowNum) {
        List<ColumnGroup> groups = layout.columnGroups();
        if (groups == null || groups.isEmpty()) {
            return rowNum;
        }
        if (groupHeaderStyle == null) {
            groupHeaderStyle = buildGroupHeaderStyle(wb);
        }
        org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum);
        int col = 0;
        for (ColumnGroup group : groups) {
            Cell cell = row.createCell(col);
            cell.setCellValue(group.label());
            cell.setCellStyle(groupHeaderStyle);
            if (group.span() > 1) {
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, col, col + group.span() - 1));
            }
            col += group.span();
        }
        return rowNum + 1;
    }

    /** Writes the column headers (if enabled). Returns the next row. */
    private int writeColumnHeaders(SXSSFSheet sheet, int rowNum) {
        if (!layout.showColumnHeaders()) {
            return rowNum;
        }
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
        for (int c = 0; c < columns.size(); c++) {
            headerRow.createCell(c).setCellValue(columns.get(c).name());
        }
        return rowNum;
    }

    /** Accumulators of the summary row (constant memory, carried along while streaming), or {@code null}. */
    private static BigDecimal[] initSums(List<? extends Column<?>> columns, SummarySpec summary) {
        if (summary == null) {
            return null;
        }
        boolean[] sumFlags = summary.sum();
        BigDecimal[] sums = new BigDecimal[columns.size()];
        for (int c = 0; c < columns.size(); c++) {
            if (sumFlags[c]) {
                sums[c] = BigDecimal.ZERO;
            }
        }
        return sums;
    }

    /** Writes one data row, measuring column widths and accumulating the sums. */
    private void writeDataRow(SXSSFSheet sheet, Row dataRow, int rowNum) {
        org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowNum);
        for (int c = 0; c < columns.size(); c++) {
            Object value = dataRow.get(c);
            Column<?> col = columns.get(c);
            if (value == null) {
                // Null-value handler: column-specific placeholder before the sheet-wide default.
                String nullText = col.nullText() != null ? col.nullText() : layout.defaultNullText();
                if (nullText != null) {
                    r.createCell(c).setCellValue(nullText);
                    widths.ensureAtLeast(c, nullText.length());
                } else {
                    // Without a placeholder: explicitly create an empty cell (Excel cell type BLANK/"Empty").
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

    /**
     * Writes the optional summary row (a pre-computed value or a real {@code =SUM(...)} formula) onto
     * the last part sheet; the sums cover the data rows of <em>all</em> part sheets. Returns the next
     * free row (for the footer rows).
     */
    private int writeSummaryRow(SXSSFSheet sheet, int rowNum, int totalDataRows) {
        if (summary == null) {
            return rowNum;
        }
        org.apache.poi.ss.usermodel.Row r = sheet.createRow(rowNum);
        boolean asFormula = summary.useFormula() && totalDataRows > 0;
        for (int c = 0; c < columns.size(); c++) {
            ColumnType type = columns.get(c).type();
            if (sums[c] != null) {
                Object value = summaryValue(type, sums[c]); // for width estimation
                if (asFormula) {
                    Cell cell = r.createCell(c);
                    cell.setCellFormula(sumFormula(c));
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
     * {@code SUM(...)} over all data ranges. Without a split this stays the plain same-sheet range
     * (e.g. {@code SUM(B3:B5)}); with a split every range is qualified with its sheet name (quoted if
     * necessary), e.g. {@code SUM('Employees'!B3:B1048570,'Employees (2)'!B2:B123)}.
     */
    private String sumFormula(int columnIndex) {
        String col = CellReference.convertNumToColString(columnIndex);
        boolean qualified = dataRanges.size() > 1;
        StringBuilder formula = new StringBuilder("SUM(");
        for (int i = 0; i < dataRanges.size(); i++) {
            DataRange range = dataRanges.get(i);
            if (i > 0) {
                formula.append(',');
            }
            if (qualified) {
                formula.append(SheetNameFormatter.format(range.sheetName())).append('!');
            }
            formula.append(col)
                    .append(range.firstRowNum())
                    .append(':')
                    .append(col)
                    .append(range.lastRowNum());
        }
        return formula.append(')').toString();
    }

    /**
     * Writes the optional footer rows onto the last part sheet (each merged across the full width).
     * Resolves the dynamic placeholders {@code {rowCount}} and {@code {sum:Column}} with the totals
     * across all part sheets (in addition to the static ones).
     */
    private void writeFooterRows(SXSSFSheet sheet, int rowNum, int totalDataRows) {
        List<String> footerLines = layout.footerLines();
        if (footerLines == null || footerLines.isEmpty()) {
            return;
        }
        Map<String, String> values = new HashMap<>(layout.placeholders());
        values.put("rowCount", Integer.toString(totalDataRows));
        if (sums != null) {
            for (int c = 0; c < columns.size(); c++) {
                if (sums[c] != null) {
                    values.put(
                            "sum:" + columns.get(c).name(),
                            sumAsText(columns.get(c).type(), sums[c]));
                }
            }
        }
        int lastCol = columns.size() - 1;
        for (String line : footerLines) {
            Cell cell = sheet.createRow(rowNum).createCell(0);
            cell.setCellValue(Placeholders.resolve(line, values, layout.placeholderResolver()));
            cell.setCellStyle(footerStyle);
            if (lastCol > 0) {
                sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, lastCol));
            }
            rowNum++;
        }
    }

    /** Text representation of a sum for {@code {sum:Column}} placeholders. */
    private static String sumAsText(ColumnType type, BigDecimal sum) {
        return switch (type) {
            case DOUBLE -> Double.toString(sum.doubleValue());
                // longValueExact: an overflowing sum fails honestly instead of being truncated silently.
            case INTEGER, LONG -> Long.toString(sum.longValueExact());
            default -> sum.toPlainString();
        };
    }

    /** Creates a valid sheet name unique within the workbook (Excel forbids duplicates). */
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

    /** Creates the style with format code once per column (or {@code null} if no format is needed). */
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

    /** Number of integer-part digits (without sign), without allocating intermediate strings. */
    private static int integerDigits(Object value) {
        if (value instanceof BigDecimal bd) {
            // precision - scale = number of integer digits; for |x| < 1 at least one ("0").
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

    /** Number of decimal digits of a non-negative {@code long} (allocation-free). */
    private static int decimalDigits(long v) {
        int digits = 1;
        while (v >= 10) {
            v /= 10;
            digits++;
        }
        return digits;
    }

    /** Decimal places from a format code, {@code 0} without a decimal point, {@code -1} without a format. */
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

    /** Estimated number of visible literal characters of a number format (currency sign, space, %, ...). */
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

    /** Heuristic minimum width (in characters) per type, so formatted values are fully visible. */
    private static int minChars(ColumnType type) {
        return switch (type) {
            case DATETIME -> 18; // "31.12.2026 23:59"
            case DECIMAL, DOUBLE, FORMULA -> 14; // currency/decimal with thousands separators
            case INTEGER, LONG -> 12;
            case DATE -> 11; // "31.12.2026"
            case TIME -> 8; // "23:59:59"
            case BOOLEAN -> 7;
            default -> 10; // STRING
        };
    }

    /**
     * Returns the default Excel format code for date and time types. Note: these are Excel format codes,
     * not Java patterns - {@code mm} means month in a date context, and hours are 24-hour by default;
     * a 12-hour display only results from an {@code AM/PM} marker in the format (Excel does not
     * distinguish {@code hh} from {@code HH}).
     */
    private static String defaultFormat(ColumnType type) {
        return switch (type) {
            case DATE -> "yyyy-mm-dd"; // ISO 8601 default format for date display
            case DATETIME -> "yyyy-mm-dd hh:mm"; // date + time (24h)
            case TIME -> "hh:mm:ss"; // time of day with seconds (24h)
            default -> null; // numbers without an explicit format: Excel default ("General")
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

    private static CellStyle buildGroupHeaderStyle(SXSSFWorkbook wb) {
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
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
            return; // don't even create an empty cell
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
                    "Unsupported date type: " + value.getClass().getName());
        }
    }

    /** Stores the time of day as a fraction of a day (0..1) – Excel shows it as a time with a time format. */
    private static void setTimeValue(Cell cell, Object value) {
        LocalTime time;
        if (value instanceof LocalTime t) {
            time = t;
        } else if (value instanceof LocalDateTime dt) {
            time = dt.toLocalTime();
        } else {
            throw new IllegalArgumentException(
                    "Unsupported time type: " + value.getClass().getName());
        }
        cell.setCellValue(time.toNanoOfDay() / NANOS_PER_DAY);
    }

    /** Returns the summed value in the matching Java type, so {@link #writeCell} writes it correctly. */
    private static Object summaryValue(ColumnType type, BigDecimal sum) {
        return switch (type) {
            case DOUBLE -> sum.doubleValue();
                // longValueExact: sums of INTEGER/LONG columns are always integral, so this only fails
                // on a true long overflow - honestly, instead of truncating silently.
            case INTEGER, LONG -> sum.longValueExact();
            default -> sum; // DECIMAL (and others) as BigDecimal
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
     * Estimates column widths based on content, so that nothing shows as "#####". Since SXSSF autosize
     * cannot see spilled rows, the width is measured while streaming: string lengths exactly, number
     * width from digit count + format (incl. the large summary values). The final width is set only
     * after all rows (always possible in SXSSF) – on every part sheet, so a split keeps uniform
     * widths.
     */
    private static final class ColumnWidthEstimator {

        private static final int POI_UNITS_PER_CHAR = 256; // POI measures widths in 1/256 of a character
        private static final int MAX_WIDTH_CHARS = 255; // Excel upper bound per column
        private static final int PADDING_CHARS = 2; // a little padding against truncated values

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

        /** Updates the estimated width of a column based on the concretely written value. */
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
                    // DATE/DATETIME/TIME/BOOLEAN/FORMULA: the base width is enough.
                default -> {
                    return;
                }
            }
            ensureAtLeast(c, chars);
        }

        /** Ensures the column is at least {@code chars} characters wide. */
        void ensureAtLeast(int c, int chars) {
            if (chars > widthChars[c]) {
                widthChars[c] = chars;
            }
        }

        /** Sets the determined widths onto the sheet (characters -> POI units, with padding). */
        void applyTo(SXSSFSheet sheet) {
            for (int c = 0; c < widthChars.length; c++) {
                int units = Math.min(
                        (widthChars[c] + PADDING_CHARS) * POI_UNITS_PER_CHAR, MAX_WIDTH_CHARS * POI_UNITS_PER_CHAR);
                sheet.setColumnWidth(c, units);
            }
        }
    }
}
