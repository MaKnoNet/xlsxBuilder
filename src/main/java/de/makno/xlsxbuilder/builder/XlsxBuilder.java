package de.makno.xlsxbuilder.builder;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Fluent builder for creating {@code .xlsx} files.
 *
 * <p>Columns are added via {@link #column}, with optional sorting via {@link #sortBy}. The data comes
 * from a {@link DataProvider} and is streamed – large data sets that do not fit in memory are
 * supported:
 * <ul>
 *   <li>without sorting: rows are streamed directly into the file;</li>
 *   <li>with sorting: {@link ExternalMergeSort} (spilling sorted runs to temp files + k-way merge), so
 *       that sorting too is not bounded by RAM.</li>
 * </ul>
 *
 * <p><b>Feature set</b> (all optional, streaming/out-of-core):
 * <ul>
 *   <li>column types + Excel format codes ({@link #ofType}/{@link #formatForType}) and value
 *       converters ({@link #convertToColumnType});</li>
 *   <li>multi-level sorting ({@link #sortBy}) and a data filter ({@link #filter});</li>
 *   <li>summary row ({@link #sumColumn}), title rows ({@link #header}) and footer rows
 *       ({@link #footer}) – each with {@code {placeholders}} ({@link #placeholder});</li>
 *   <li>null-value placeholders ({@link #defaultNullText}/{@link #nullText});</li>
 *   <li>output as {@code .xlsx} (via the {@link WorkbookBuilder});</li>
 *   <li>optional pipeline parallelism ({@link #parallel(boolean)}).</li>
 * </ul>
 *
 * <p>An {@code XlsxBuilder} describes exactly <em>one</em> sheet (incl. its data source via
 * {@link #data(DataProvider)}). It is written as {@code .xlsx} through the {@link WorkbookBuilder}
 * (one or more sheets per file).
 *
 * <pre>{@code
 * WorkbookBuilder.create()
 *     .sheet(XlsxBuilder.<Employee>create()
 *         .sheetName("Mitarbeiter")
 *         .column("Name", Employee::name)                                  // default: text
 *         .column("Gehalt", Employee::salary).ofType(ColumnType.DECIMAL).formatForType("#,##0.00")
 *         .sortBy("Gehalt", SortOrder.DESC)
 *         .data(dataProvider))
 *     .write(Path.of("out.xlsx"));
 * }</pre>
 *
 * <p><b>Thread safety:</b> this class is <em>not</em> thread-safe and is designed for single use –
 * create a new instance per job/request and do not share it between threads. Concurrent jobs, each
 * with their own builder instances, run isolated (no shared/static state). Note, however: the
 * external merge sort buffers {@link #sortChunkSize(int)} rows per sort in memory – with many
 * concurrent jobs this adds up, so limit concurrency or choose a smaller {@code sortChunkSize} if
 * needed. The supplied {@link DataProvider} must likewise not be shared between threads. Writing the
 * same instance a second time (re-writing via the {@link WorkbookBuilder}) throws an
 * {@link IllegalStateException}, because the data source is forward-only/single-use.
 */
public final class XlsxBuilder<T> {

    private static final int DEFAULT_CHUNK_SIZE = 100_000;

    private String sheetName = "Sheet1";
    private final List<String> headerLines = new ArrayList<>();
    private final List<String> footerLines = new ArrayList<>();
    private final List<Column<T>> columns = new ArrayList<>();
    private final List<SortKey> sortKeys = new ArrayList<>();
    private final List<String> sumColumnNames = new ArrayList<>();
    private final Map<String, String> placeholders = new LinkedHashMap<>();
    private Function<String, String> placeholderResolver; // null = static placeholders only
    private String summaryLabelColumn;
    private String summaryLabelText;
    private boolean summaryAsFormula;
    private boolean showColumnHeaders = true;
    private int sortChunkSize = DEFAULT_CHUNK_SIZE;
    private Path sortTempDir; // null = system temp (java.io.tmpdir)
    private Predicate<? super T> filter; // null = no filtering (all objects)
    private String defaultNullText; // sheet-wide placeholder for null; null = empty cell
    private boolean parallel; // pipeline parallelism (producer/consumer); off by default
    private boolean consumed; // single-use: not reusable after writing
    private DataProvider<T> dataProvider;

    private XlsxBuilder() {}

    public static <T> XlsxBuilder<T> create() {
        return new XlsxBuilder<>();
    }

    public XlsxBuilder<T> sheetName(String name) {
        this.sheetName = Objects.requireNonNull(name, "name");
        return this;
    }

    /**
     * Optional title row(s) above the column headers. Each line is merged across the full table width
     * and displayed centered. Calling repeatedly appends further title rows.
     */
    public XlsxBuilder<T> header(String... lines) {
        for (String line : lines) {
            headerLines.add(Objects.requireNonNull(line, "line"));
        }
        return this;
    }

    /**
     * Defines a column. The default type is {@code STRING} (text). Type and format are optional and are
     * set directly afterwards via {@link #ofType(ColumnType)} resp. {@link #formatForType(String)}:
     * <pre>{@code
     * .column("Gehalt", Employee::salary).ofType(ColumnType.DECIMAL).formatForType("#,##0.00")
     * }</pre>
     */
    public XlsxBuilder<T> column(String name, Function<? super T, ?> extractor) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(extractor, "extractor");
        columns.add(new Column<>(name, ColumnType.STRING, null, extractor));
        return this;
    }

    /** Sets the type of the most recently defined column. */
    public XlsxBuilder<T> ofType(ColumnType type) {
        lastColumn().setType(type);
        return this;
    }

    /**
     * Sets the Excel format code of the most recently defined column, e.g. {@code "#,##0.00"},
     * {@code "0.00%"}, {@code "dd.mm.yyyy"} or {@code "hh:mm:ss"}.
     */
    public XlsxBuilder<T> formatForType(String format) {
        lastColumn().setFormat(format);
        return this;
    }

    /**
     * Placeholder for {@code null} values of the most recently defined column (overrides
     * {@link #defaultNullText(String)}). Without it, the sheet-wide default resp. an empty cell
     * applies. {@code ""} forces an empty text cell despite a configured default.
     */
    public XlsxBuilder<T> nullText(String text) {
        lastColumn().setNullText(text);
        return this;
    }

    /**
     * Optional converter that transforms the extracted raw value of the most recently defined column,
     * before writing, into the representation matching the target type – e.g. an {@code int} into a
     * {@link java.time.LocalTime} for {@link ColumnType#TIME}:
     * <pre>{@code
     * .column("Start", Task::sekunden).ofType(ColumnType.TIME)
     *     .convertToColumnType((Integer s) -> java.time.LocalTime.ofSecondOfDay(s))
     * }</pre>
     * The lambda parameter type should be stated explicitly. The conversion also applies to sorting and
     * the summary row, since it happens already at projection time. The return value must match the
     * representation of the configured {@link ColumnType} (e.g. {@link java.time.LocalDate} for
     * {@link ColumnType#DATE}, {@link java.math.BigDecimal} for {@link ColumnType#DECIMAL}).
     *
     * <p><b>Caution – loss of precision:</b> lossy conversions are easy to introduce here and often
     * surface only in the finished report. In particular {@code BigDecimal -> double}
     * ({@link ColumnType#DOUBLE}) loses precision, and {@code number -> String} can change scale/
     * formatting. For <em>full</em> precision, deliberately supply the value as a
     * {@link java.math.BigDecimal} and use {@link ColumnType#DECIMAL} – or, if text is desired, convert
     * to {@code String} in a controlled way (e.g. via {@code BigDecimal#toPlainString()}). There is
     * deliberately no automatic runtime check (performance on the hot path).
     */
    @SuppressWarnings("unchecked")
    public <R> XlsxBuilder<T> convertToColumnType(Function<R, ?> converter) {
        Objects.requireNonNull(converter, "converter");
        lastColumn().setConverter((Function<Object, Object>) converter);
        return this;
    }

    private Column<T> lastColumn() {
        if (columns.isEmpty()) {
            throw new IllegalStateException("ofType()/formatForType() benötigt eine vorherige column(...)");
        }
        return columns.get(columns.size() - 1);
    }

    /** Optional sort stage. Calling repeatedly yields a multi-level sort. */
    public XlsxBuilder<T> sortBy(String columnName, SortOrder order) {
        sortKeys.add(new SortKey(columnName, order));
        return this;
    }

    /**
     * Marks a numeric column for summation. Enables the optional summary row at the end of the table.
     * Calling repeatedly sums multiple columns.
     */
    public XlsxBuilder<T> sumColumn(String columnName) {
        sumColumnNames.add(Objects.requireNonNull(columnName, "columnName"));
        return this;
    }

    /** Optional label in the summary row (e.g. {@code summaryLabel("Name", "Summe")}). */
    public XlsxBuilder<T> summaryLabel(String columnName, String text) {
        this.summaryLabelColumn = Objects.requireNonNull(columnName, "columnName");
        this.summaryLabelText = Objects.requireNonNull(text, "text");
        return this;
    }

    /**
     * Controls how the summary row is computed: {@code true} = a real Excel formula
     * {@code =SUM(range)} (updates automatically), {@code false} (default) = a pre-computed value.
     */
    public XlsxBuilder<T> summaryAsFormula(boolean useFormula) {
        this.summaryAsFormula = useFormula;
        return this;
    }

    /**
     * Controls whether the column-header row is written to the file. Default: {@code true}. With
     * {@code false} the table starts directly with the data rows – useful for raw-data exports or
     * sheets that are post-processed by a macro.
     */
    public XlsxBuilder<T> columnHeaders(boolean show) {
        this.showColumnHeaders = show;
        return this;
    }

    /** Chunk size (rows per in-memory sorted run) of the External Merge Sort. */
    public XlsxBuilder<T> sortChunkSize(int chunkSize) {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize muss >= 1 sein");
        }
        this.sortChunkSize = chunkSize;
        return this;
    }

    /**
     * Optional base directory for the temporary sort files (External Merge Sort). {@code null}
     * (default) = system temp ({@code java.io.tmpdir}). In server operation a dedicated (fast/large)
     * disk can be chosen here. The directory is created on demand; the per-sort subdirectory is deleted
     * again after writing. Only effective with active sorting ({@link #sortBy(String, SortOrder)}).
     */
    public XlsxBuilder<T> sortTempDir(Path dir) {
        this.sortTempDir = dir;
        return this;
    }

    /**
     * Applies a workbook-wide default sort temp directory, unless this sheet already has its own (set
     * via {@link #sortTempDir(Path)}). Called by the {@link WorkbookBuilder} before rendering, so the
     * per-sheet setting always wins.
     */
    void applyDefaultTempDir(Path defaultDir) {
        if (defaultDir != null && sortTempDir == null) {
            sortTempDir = defaultDir;
        }
    }

    /**
     * Optional filter on the raw records: only objects for which the predicate returns {@code true} are
     * written. It is applied <em>before</em> projection, sorting and summation – so the summary row
     * refers only to the rows actually written. Calling repeatedly combines the predicates with AND.
     */
    public XlsxBuilder<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        Predicate<? super T> existing = this.filter;
        this.filter = existing == null ? predicate : r -> existing.test(r) && predicate.test(r);
        return this;
    }

    /**
     * Sheet-wide placeholder written for {@code null} cell values (e.g. {@code "-"} or {@code "n/a"}).
     * Without it, {@code null} cells stay empty. Individual columns can override this via
     * {@link #nullText(String)}.
     */
    public XlsxBuilder<T> defaultNullText(String text) {
        this.defaultNullText = text;
        return this;
    }

    /**
     * Optional footer row(s) below the data (and an optional summary row), each merged across the full
     * width. Calling repeatedly appends further rows. Supports placeholders (see
     * {@link #placeholder(String, String)}), incl. dynamic {@code {rowCount}} and {@code {sum:Column}}.
     */
    public XlsxBuilder<T> footer(String... lines) {
        for (String line : lines) {
            footerLines.add(Objects.requireNonNull(line, "line"));
        }
        return this;
    }

    /**
     * Defines a placeholder {@code {key}} that is replaced in title, header and footer texts.
     * Built-in additionally are {@code {date}}/{@code {datetime}} (overridable) as well as – only in the
     * footer – {@code {rowCount}} and {@code {sum:ColumnName}}.
     */
    public XlsxBuilder<T> placeholder(String key, String value) {
        placeholders.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
        return this;
    }

    /**
     * Sets an optional resolver for lazy/computed placeholders (e.g. a version number or user name from
     * the request context). It is consulted per {@code {key}} <em>only</em> when neither
     * {@link #placeholder(String, String)} nor the built-in placeholders know the key – the static map
     * therefore takes precedence. If the resolver returns {@code null}, the token stays visible
     * unchanged. Calling repeatedly replaces the previous resolver.
     *
     * <p>Resolution happens at write time and only for title, header and footer rows (not per data
     * row) – so the memory footprint stays out-of-core-neutral.
     */
    public XlsxBuilder<T> placeholderResolver(Function<String, String> resolver) {
        this.placeholderResolver = Objects.requireNonNull(resolver, "resolver");
        return this;
    }

    /** Adds several placeholders at once (see {@link #placeholder(String, String)}). */
    public XlsxBuilder<T> placeholders(Map<String, String> values) {
        values.forEach(this::placeholder);
        return this;
    }

    /**
     * Enables the optional pipeline parallelism for this sheet: a background thread reads/sorts
     * (producer) while the calling thread writes (consumer), coupled through a bounded queue. Default
     * {@code false}. The result is <em>identical</em> to the sequential mode; memory stays out-of-core
     * (the queue is bounded).
     *
     * <p><b>When is {@code parallel(true)} worth it?</b> Only when the <em>producer side</em> is the
     * bottleneck and can overlap with writing, e.g.:
     * <ul>
     *   <li>a <b>slow/remote data source</b> with latency (remote DB, network stream);</li>
     *   <li>expensive <b>projection/conversion</b> per row (heavy {@link #convertToColumnType} logic or
     *       extractors).</li>
     * </ul>
     *
     * <p><b>When better leave it off?</b> When <b>POI writing dominates</b> (typical local workloads):
     * POI writes single-threaded, so the producer is then just fast disk I/O – the overlap is small and
     * the thread/queue overhead tends to make it marginally slower. On a <b>multi-user server</b> also
     * consider: every enabled export costs an extra thread – there you scale throughput better
     * <em>between</em> requests (thread pool) than by enabling this individually. When in doubt, measure
     * with and without.
     */
    public XlsxBuilder<T> parallel(boolean enabled) {
        this.parallel = enabled;
        return this;
    }

    /** Sets this sheet's data source. Required before the sheet is written. */
    public XlsxBuilder<T> data(DataProvider<T> provider) {
        this.dataProvider = Objects.requireNonNull(provider, "provider");
        return this;
    }

    /**
     * Renders this sheet into an existing workbook (called by the {@link WorkbookBuilder}). Processes
     * the data source streamed; when sorting, out-of-core via {@link ExternalMergeSort}.
     */
    void renderInto(SXSSFWorkbook wb) throws IOException {
        if (consumed) {
            throw new IllegalStateException(
                    "XlsxBuilder ist Einmal-Nutzung: bereits geschrieben – pro Auftrag eine neue Instanz erstellen"
                            + " (Blatt: "
                            + sheetName + ")");
        }
        if (columns.isEmpty()) {
            throw new IllegalStateException("Mindestens eine Spalte muss definiert sein");
        }
        if (dataProvider == null) {
            throw new IllegalStateException("Kein DataProvider gesetzt (.data(...)) für Blatt: " + sheetName);
        }
        // Validate sorting: only sortable column types.
        for (SortKey sortKey : sortKeys) {
            int idx = indexOf(sortKey.columnName());
            if (idx < 0) {
                throw new IllegalArgumentException("Unbekannte Sortierspalte: " + sortKey.columnName());
            }
            ColumnType type = columns.get(idx).type();
            if (!type.isSortable()) {
                throw new IllegalArgumentException("Sortierspalte '" + sortKey.columnName() + "' ist vom Typ " + type
                        + " und kann nicht sortiert werden");
            }
        }
        // From here the (forward-only, single-use) data source is consumed -> block reuse.
        consumed = true;
        RenderJob<T> job = new RenderJob<>(
                sheetName,
                List.copyOf(columns),
                filter,
                dataProvider,
                new SortSpec(List.copyOf(sortKeys), sortChunkSize, sortTempDir),
                buildSummarySpec(),
                buildLayout(),
                parallel);
        SheetRenderer.render(wb, job);
    }

    /** Builds the layout options incl. the statically resolvable placeholders ({@code {date}}/{@code {datetime}}). */
    private SheetWriteOptions buildLayout() {
        List<String> header = headerLines.isEmpty() ? null : headerLines;
        Map<String, String> staticPlaceholders = new LinkedHashMap<>(placeholders);
        staticPlaceholders.putIfAbsent("date", LocalDate.now().toString());
        staticPlaceholders.putIfAbsent(
                "datetime", LocalDateTime.now().withNano(0).toString());
        return new SheetWriteOptions(
                header, footerLines, staticPlaceholders, placeholderResolver, showColumnHeaders, defaultNullText);
    }

    /** Builds the summary-row configuration, or {@code null} if no summary row is desired. */
    private SummarySpec buildSummarySpec() {
        if (sumColumnNames.isEmpty() && summaryLabelColumn == null) {
            return null;
        }
        boolean[] sum = new boolean[columns.size()];
        for (String name : sumColumnNames) {
            int idx = indexOf(name);
            if (idx < 0) {
                throw new IllegalArgumentException("Unbekannte Summenspalte: " + name);
            }
            if (!isNumeric(columns.get(idx).type())) {
                throw new IllegalArgumentException("Summenspalte ist nicht numerisch: " + name);
            }
            sum[idx] = true;
        }
        int labelIndex = -1;
        if (summaryLabelColumn != null) {
            labelIndex = indexOf(summaryLabelColumn);
            if (labelIndex < 0) {
                throw new IllegalArgumentException("Unbekannte Label-Spalte: " + summaryLabelColumn);
            }
        }
        return new SummarySpec(sum, labelIndex, summaryLabelText, summaryAsFormula);
    }

    private int indexOf(String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).name().equals(columnName)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isNumeric(ColumnType type) {
        return switch (type) {
            case INTEGER, LONG, DOUBLE, DECIMAL -> true;
            default -> false;
        };
    }
}
