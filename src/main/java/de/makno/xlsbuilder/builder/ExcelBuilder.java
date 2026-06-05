package de.makno.xlsbuilder.builder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Fluent-Builder zum Erzeugen von {@code .xlsx}-Dateien.
 *
 * <p>Spalten werden via {@link #column} hinzugefügt, eine optionale Sortierung via {@link #sortBy}.
 * Die Daten kommen aus einem {@link DataProvider} und werden gestreamt – große Datenmengen, die
 * nicht in den Speicher passen, werden unterstützt:
 * <ul>
 *   <li>ohne Sortierung: direktes Streaming der Zeilen in die Datei;</li>
 *   <li>mit Sortierung: {@link ExternalMergeSort} (Auslagern sortierter Runs auf Temp-Dateien +
 *       k-way-Merge), sodass auch die Sortierung nicht durch den RAM begrenzt ist.</li>
 * </ul>
 *
 * <p>Ein {@code ExcelBuilder} beschreibt genau <em>ein</em> Blatt (inkl. Datenquelle via
 * {@link #data(DataProvider)}). Geschrieben wird über den {@link WorkbookBuilder}, der ein oder
 * mehrere Blätter in eine Datei zusammenfasst.
 *
 * <pre>{@code
 * WorkbookBuilder.create()
 *     .sheet(ExcelBuilder.<Employee>create()
 *         .sheetName("Mitarbeiter")
 *         .column("Name", Employee::name)                                  // Default: Text
 *         .column("Gehalt", Employee::salary).ofType(ColumnType.DECIMAL).formatForType("#,##0.00")
 *         .sortBy("Gehalt", SortOrder.DESC)
 *         .data(dataProvider))
 *     .write(Path.of("out.xlsx"));
 * }</pre>
 *
 * <p><b>Thread-Sicherheit:</b> Diese Klasse ist <em>nicht</em> thread-safe und auf Einmal-Nutzung
 * ausgelegt – pro Auftrag/Request eine neue Instanz erzeugen und nicht zwischen Threads teilen.
 * Nebenläufige Aufträge mit jeweils eigenen Builder-Instanzen laufen isoliert (kein geteilter/
 * statischer Zustand). Beachte aber: Der externe Merge Sort puffert {@link #sortChunkSize(int)}
 * Zeilen je Sortierung im Speicher – bei vielen gleichzeitigen Aufträgen summiert sich das, daher
 * ggf. die Nebenläufigkeit begrenzen oder {@code sortChunkSize} kleiner wählen. Der übergebene
 * {@link DataProvider} darf ebenfalls nicht zwischen Threads geteilt werden.
 */
public final class ExcelBuilder<T> {

    private static final Logger LOG = LogManager.getLogger(ExcelBuilder.class);

    private static final int DEFAULT_CHUNK_SIZE = 100_000;

    private String sheetName = "Sheet1";
    private final List<String> headerLines = new ArrayList<>();
    private final List<Column<T>> columns = new ArrayList<>();
    private final List<SortKey> sortKeys = new ArrayList<>();
    private final List<String> sumColumnNames = new ArrayList<>();
    private String summaryLabelColumn;
    private String summaryLabelText;
    private boolean summaryAsFormula;
    private boolean showColumnHeaders = true;
    private int sortChunkSize = DEFAULT_CHUNK_SIZE;
    private Path sortTempDir; // null = System-Temp (java.io.tmpdir)
    private Predicate<? super T> filter; // null = keine Filterung (alle Objekte)
    private String defaultNullText; // sheet-weiter Platzhalter für null; null = leere Zelle
    private DataProvider<T> dataProvider;

    private ExcelBuilder() {
    }

    public static <T> ExcelBuilder<T> create() {
        return new ExcelBuilder<>();
    }

    public ExcelBuilder<T> sheetName(String name) {
        this.sheetName = Objects.requireNonNull(name, "name");
        return this;
    }

    /**
     * Optionale Titelzeile(n) oberhalb der Spaltenüberschriften. Jede Zeile wird über die volle
     * Tabellenbreite zusammengeführt und zentriert dargestellt. Mehrfacher Aufruf hängt weitere
     * Titelzeilen an.
     */
    public ExcelBuilder<T> header(String... lines) {
        for (String line : lines) {
            headerLines.add(Objects.requireNonNull(line, "line"));
        }
        return this;
    }

    /**
     * Definiert eine Spalte. Standardtyp ist {@code STRING} (Text). Typ und Format sind optional und
     * werden direkt dahinter mit {@link #ofType(ColumnType)} bzw. {@link #formatForType(String)} gesetzt:
     * <pre>{@code
     * .column("Gehalt", Employee::salary).ofType(ColumnType.DECIMAL).formatForType("#,##0.00")
     * }</pre>
     */
    public ExcelBuilder<T> column(String name, Function<? super T, ?> extractor) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(extractor, "extractor");
        columns.add(new Column<>(name, ColumnType.STRING, null, extractor));
        return this;
    }

    /** Setzt den Typ der zuletzt definierten Spalte. */
    public ExcelBuilder<T> ofType(ColumnType type) {
        lastColumn().setType(type);
        return this;
    }

    /**
     * Setzt den Excel-Format-Code der zuletzt definierten Spalte, z. B. {@code "#,##0.00"},
     * {@code "0.00%"}, {@code "dd.mm.yyyy"} oder {@code "hh:mm:ss"}.
     */
    public ExcelBuilder<T> formatForType(String format) {
        lastColumn().setFormat(format);
        return this;
    }

    /**
     * Platzhalter für {@code null}-Werte der zuletzt definierten Spalte (überschreibt
     * {@link #defaultNullText(String)}). Ohne Angabe gilt der sheet-weite Default bzw. eine leere
     * Zelle. {@code ""} erzwingt eine leere Textzelle trotz gesetztem Default.
     */
    public ExcelBuilder<T> nullText(String text) {
        lastColumn().setNullText(text);
        return this;
    }

    /**
     * Optionaler Konverter, der den extrahierten Rohwert der zuletzt definierten Spalte vor dem
     * Schreiben in die zum Zieltyp passende Repräsentation umwandelt – z. B. ein {@code int} in eine
     * {@link java.time.LocalTime} für {@link ColumnType#TIME}:
     * <pre>{@code
     * .column("Start", Task::sekunden).ofType(ColumnType.TIME)
     *     .convertToColumnType((Integer s) -> java.time.LocalTime.ofSecondOfDay(s))
     * }</pre>
     * Der Lambda-Parametertyp sollte explizit angegeben werden. Die Umwandlung greift auch für
     * Sortierung und Summenzeile, da sie bereits bei der Projektion erfolgt.
     */
    @SuppressWarnings("unchecked")
    public <R> ExcelBuilder<T> convertToColumnType(Function<R, ?> converter) {
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

    /** Optionale Sortierstufe. Mehrfacher Aufruf ergibt eine mehrstufige Sortierung. */
    public ExcelBuilder<T> sortBy(String columnName, SortOrder order) {
        sortKeys.add(new SortKey(columnName, order));
        return this;
    }

    /**
     * Markiert eine numerische Spalte zum Summieren. Aktiviert die optionale Summenzeile am Ende
     * der Tabelle. Mehrfacher Aufruf summiert mehrere Spalten.
     */
    public ExcelBuilder<T> sumColumn(String columnName) {
        sumColumnNames.add(Objects.requireNonNull(columnName, "columnName"));
        return this;
    }

    /** Optionales Label in der Summenzeile (z. B. {@code summaryLabel("Name", "Summe")}). */
    public ExcelBuilder<T> summaryLabel(String columnName, String text) {
        this.summaryLabelColumn = Objects.requireNonNull(columnName, "columnName");
        this.summaryLabelText = Objects.requireNonNull(text, "text");
        return this;
    }

    /**
     * Legt fest, wie die Summenzeile berechnet wird: {@code true} = echte Excel-Formel
     * {@code =SUMME(Bereich)} (aktualisiert sich automatisch), {@code false} (Default) = vorberechneter
     * Wert.
     */
    public ExcelBuilder<T> summaryAsFormula(boolean useFormula) {
        this.summaryAsFormula = useFormula;
        return this;
    }

    /**
     * Steuert, ob die Zeile mit den Spaltenüberschriften in die Datei geschrieben wird.
     * Default: {@code true}. Mit {@code false} beginnt die Tabelle direkt mit den Datenzeilen –
     * nützlich für Rohdaten-Exports oder Blätter, die per Makro weiterverarbeitet werden.
     */
    public ExcelBuilder<T> columnHeaders(boolean show) {
        this.showColumnHeaders = show;
        return this;
    }

    /** Chunk-Größe (Zeilen pro in-memory sortiertem Run) des External Merge Sort. */
    public ExcelBuilder<T> sortChunkSize(int chunkSize) {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize muss >= 1 sein");
        }
        this.sortChunkSize = chunkSize;
        return this;
    }

    /**
     * Optionales Basisverzeichnis für die temporären Sortier-Dateien (External Merge Sort).
     * {@code null} (Default) = System-Temp ({@code java.io.tmpdir}). Im Server-Betrieb kann hier eine
     * dedizierte (schnelle/große) Platte gewählt werden. Das Verzeichnis wird bei Bedarf angelegt;
     * das je Sortierung erzeugte Unterverzeichnis wird nach dem Schreiben wieder gelöscht.
     * Wirkt nur bei aktiver Sortierung ({@link #sortBy(String, SortOrder)}).
     */
    public ExcelBuilder<T> sortTempDir(Path dir) {
        this.sortTempDir = dir;
        return this;
    }

    /**
     * Optionaler Filter auf den Rohdatensätzen: nur Objekte, für die das Prädikat {@code true} liefert,
     * werden geschrieben. Wird <em>vor</em> Projektion, Sortierung und Summenbildung angewandt – die
     * Summenzeile bezieht sich also nur auf die tatsächlich geschriebenen Zeilen. Mehrfacher Aufruf
     * verknüpft die Prädikate mit UND.
     */
    public ExcelBuilder<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        Predicate<? super T> existing = this.filter;
        this.filter = existing == null ? predicate : r -> existing.test(r) && predicate.test(r);
        return this;
    }

    /**
     * Sheet-weiter Platzhalter, der für {@code null}-Zellwerte geschrieben wird (z. B. {@code "-"} oder
     * {@code "n/a"}). Ohne Angabe bleiben {@code null}-Zellen leer. Einzelne Spalten können dies via
     * {@link #nullText(String)} überschreiben.
     */
    public ExcelBuilder<T> defaultNullText(String text) {
        this.defaultNullText = text;
        return this;
    }

    /** Setzt die Datenquelle dieses Blatts. Erforderlich, bevor das Blatt geschrieben wird. */
    public ExcelBuilder<T> data(DataProvider<T> provider) {
        this.dataProvider = Objects.requireNonNull(provider, "provider");
        return this;
    }

    /**
     * Rendert dieses Blatt in ein vorhandenes Workbook (vom {@link WorkbookBuilder} aufgerufen).
     * Verarbeitet die Datenquelle gestreamt; bei Sortierung via {@link ExternalMergeSort} out-of-core.
     */
    void renderInto(SXSSFWorkbook wb) throws IOException {
        if (columns.isEmpty()) {
            throw new IllegalStateException("Mindestens eine Spalte muss definiert sein");
        }
        if (dataProvider == null) {
            throw new IllegalStateException("Kein DataProvider gesetzt (.data(...)) für Blatt: " + sheetName);
        }

        SummarySpec summary = buildSummarySpec();
        List<String> header = headerLines.isEmpty() ? null : headerLines;

        try (DataProvider<T> p = dataProvider) {
            Iterator<Row> projected = projection(p);
            if (sortKeys.isEmpty()) {
                long writeStart = System.nanoTime();
                int rows = XlsxWriter.addSheet(wb, sheetName, columns, header, projected, summary,
                        showColumnHeaders, defaultNullText);
                LOG.debug("Blatt '{}': {} Zeilen, unsortiert – Schreibphase {} ms",
                        sheetName, rows, (System.nanoTime() - writeStart) / 1_000_000);
            } else {
                RowComparator comparator = new RowComparator(columns, sortKeys);
                try (ExternalMergeSort sorter =
                        new ExternalMergeSort(comparator, sortChunkSize, sortTempDir)) {
                    // sort() konsumiert die Projektion (und damit den Provider) vollständig ...
                    long sortStart = System.nanoTime();
                    CloseableIterator<Row> sorted = sorter.sort(projected);
                    long sortMs = (System.nanoTime() - sortStart) / 1_000_000;
                    // ... danach wird der sortierte Strom in das Blatt geschrieben (inkl. finalem Merge).
                    long writeStart = System.nanoTime();
                    try (sorted) {
                        int rows = XlsxWriter.addSheet(wb, sheetName, columns, header, sorted, summary,
                                showColumnHeaders, defaultNullText);
                        LOG.debug("Blatt '{}': {} Zeilen, sortiert – Sortierphase {} ms, "
                                        + "Schreibphase {} ms",
                                sheetName, rows, sortMs, (System.nanoTime() - writeStart) / 1_000_000);
                    }
                }
            }
        }
    }

    /** Baut die Summenzeilen-Konfiguration oder {@code null}, falls keine Summenzeile gewünscht ist. */
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

    /**
     * Projiziert jeden (vom optionalen {@link #filter} akzeptierten) Datensatz früh auf eine
     * {@link Row} aus den extrahierten Zellenwerten. Look-ahead-Iterator, da die Quelle forward-only
     * ist und nicht passende Datensätze übersprungen werden.
     */
    private Iterator<Row> projection(DataProvider<T> provider) {
        return new Iterator<>() {
            private Row pending; // vorausgelesene, gefilterte Zeile oder null

            @Override
            public boolean hasNext() {
                while (pending == null && provider.hasNext()) {
                    T record = provider.next();
                    if (filter == null || filter.test(record)) {
                        pending = project(record);
                    }
                }
                return pending != null;
            }

            @Override
            public Row next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                Row row = pending;
                pending = null;
                return row;
            }
        };
    }

    /** Projiziert einen Datensatz auf eine {@link Row} aus den extrahierten Zellenwerten. */
    private Row project(T record) {
        Object[] values = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            values[i] = columns.get(i).extract(record);
        }
        return new Row(values);
    }
}
