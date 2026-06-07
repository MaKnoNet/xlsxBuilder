package de.makno.xlsxbuilder.builder;

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
 *     .sheet(XlsxBuilder.<Employee>create().sheetName("Mitarbeiter")
 *         .column("Name", Employee::name)
 *         .data(employeeProvider))
 *     .sheet(XlsxBuilder.<Order>create().sheetName("Aufträge")
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
            throw new IllegalArgumentException("sxssfRowWindow muss >= 1 sein");
        }
        this.sxssfRowWindow = window;
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
            throw new IllegalStateException("Mindestens ein Blatt erforderlich");
        }
        if (written) {
            throw new IllegalStateException(
                    "WorkbookBuilder ist Einmal-Nutzung: bereits geschrieben – pro Auftrag eine neue Instanz"
                            + " erstellen");
        }
        written = true;
        long startNanos = System.nanoTime();
        try (SXSSFWorkbook wb = new SXSSFWorkbook(sxssfRowWindow)) {
            for (XlsxBuilder<?> sheet : sheets) {
                sheet.renderInto(wb);
            }
            wb.write(out);
        }
        LOG.debug(
                "Workbook: {} Blätter in {} ms geschrieben (sxssfRowWindow={})",
                sheets.size(),
                (System.nanoTime() - startNanos) / 1_000_000,
                sxssfRowWindow);
    }
}
