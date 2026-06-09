package de.makno.xlsxbuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Combines one or more worksheets into a single {@code .xlsx} file. Each sheet is described by its own
 * {@link XlsxBuilder} (with its own data type, columns, sorting, summary row, etc.) and supplies its
 * data via {@link XlsxBuilder#data(DataProvider)}.
 *
 * <p>The sheets are written one after another in a streaming fashion (incl. an external merge sort per
 * sheet), so that the memory footprint stays out-of-core/bounded.
 *
 * <pre>{@code
 * WorkbookBuilder.create()
 *     .sheet(XlsxBuilder.<Employee>create().sheetName("Employees")
 *         .column("Name", Employee::name)
 *         .data(employeeProvider))
 *     .sheet(XlsxBuilder.<Order>create().sheetName("Orders")
 *         .column("Nr", Order::id).ofType(ColumnType.LONG)
 *         .data(orderProvider))
 *     .write(Path.of("report.xlsx"));
 * }</pre>
 *
 * <p><b>Thread safety:</b> not thread-safe and designed for single use – create a separate instance
 * per job, do not share it between threads and do not write to the same file concurrently. As the
 * library has no shared/static state, concurrent jobs each with their own instances run isolated; each
 * {@link #write} creates its own POI workbook.
 *
 * <p><b>Single use:</b> an instance may only be written once; a second {@link #write} call throws an
 * {@link IllegalStateException} (the sheets' data sources are forward-only and exhausted after the
 * first write).
 */
public final class WorkbookBuilder {

    private static final Logger LOG = LogManager.getLogger(WorkbookBuilder.class);

    /**
     * Default number of rows SXSSF keeps in memory per sheet at once (the rest is spilled to disk).
     * This is a good compromise for most use cases.
     */
    private static final int DEFAULT_ROW_WINDOW = 100;

    private final List<XlsxBuilder<?>> sheets = new ArrayList<>();
    private int sxssfRowWindow = DEFAULT_ROW_WINDOW;
    private Path tempDir; // default base dir for the sort runs of all sheets (null = per-sheet/system temp)
    private boolean written; // single-use: not reusable after write(...)

    private WorkbookBuilder() {}

    public static WorkbookBuilder create() {
        return new WorkbookBuilder();
    }

    /**
     * Sets the number of rows SXSSF keeps in memory per sheet at once (the rest is spilled to temp
     * files). Memory usage grows with this size, but so does write performance. Default is
     * {@value #DEFAULT_ROW_WINDOW}.
     *
     * <p><b>Typical values:</b>
     * <ul>
     *   <li>50–100: very memory-frugal, good for very large files with a limited heap;</li>
     *   <li>100–500: standard, a good balance between memory and performance;</li>
     *   <li>1000+: more RAM, but also more performance (if the heap is large enough).</li>
     * </ul>
     *
     * @param window number of rows in memory (>= 1)
     * @return this for the fluent API
     * @throws IllegalArgumentException if window &lt; 1
     */
    public WorkbookBuilder sxssfRowWindow(int window) {
        if (window < 1) {
            throw new IllegalArgumentException("sxssfRowWindow must be >= 1");
        }
        this.sxssfRowWindow = window;
        return this;
    }

    /**
     * Sets a default base directory for the temporary sort-run files (External Merge Sort) of all
     * sheets in this workbook – a single place to direct the library's "junk" files. A per-sheet
     * {@link XlsxBuilder#sortTempDir(Path)} still takes precedence. {@code null} (default) = the
     * per-sheet setting resp. the system temp ({@code java.io.tmpdir}). The directory is created on
     * demand; the per-sort subdirectory created inside it is deleted again after writing.
     *
     * <p><b>Note:</b> this only redirects the library's own sort-run files. Apache POI's SXSSF temp
     * files (the row-window spill) always use POI's process-global temp directory
     * ({@code java.io.tmpdir}); POI offers no per-workbook, multi-user-safe way to relocate them.
     *
     * @param dir base directory for the sort runs (created on demand), or {@code null} for the default
     * @return this for the fluent API
     */
    public WorkbookBuilder tempDir(Path dir) {
        this.tempDir = dir;
        return this;
    }

    /** Adds a sheet. The {@link XlsxBuilder} must have a data source ({@code .data(...)}). */
    public WorkbookBuilder sheet(XlsxBuilder<?> sheet) {
        sheets.add(Objects.requireNonNull(sheet, "sheet"));
        return this;
    }

    public void write(Path out) throws IOException {
        Objects.requireNonNull(out, "out");
        try (OutputStream os = Files.newOutputStream(out)) {
            write(os);
        }
    }

    public void write(OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        if (sheets.isEmpty()) {
            throw new IllegalStateException("At least one sheet is required");
        }
        if (written) {
            throw new IllegalStateException(
                    "WorkbookBuilder is single-use: already written – create a new instance per" + " job");
        }
        written = true;
        long startNanos = System.nanoTime();
        try (SXSSFWorkbook wb = new SXSSFWorkbook(sxssfRowWindow)) {
            for (XlsxBuilder<?> sheet : sheets) {
                sheet.applyDefaultTempDir(tempDir);
                sheet.renderInto(wb);
            }
            wb.write(out);
        }
        LOG.debug(
                "Workbook: {} sheets written in {} ms (sxssfRowWindow={})",
                sheets.size(),
                (System.nanoTime() - startNanos) / 1_000_000,
                sxssfRowWindow);
    }
}
