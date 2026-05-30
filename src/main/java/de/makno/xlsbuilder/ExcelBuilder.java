package de.makno.xlsbuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

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
 * <pre>{@code
 * ExcelBuilder.<Employee>create()
 *     .sheetName("Mitarbeiter")
 *     .column("Name",   ColumnType.STRING,  Employee::name)
 *     .column("Gehalt", ColumnType.DECIMAL, Employee::salary)
 *     .sortBy("Gehalt", SortOrder.DESC)
 *     .write(dataProvider, Path.of("out.xlsx"));
 * }</pre>
 */
public final class ExcelBuilder<T> {

    private static final int DEFAULT_CHUNK_SIZE = 100_000;

    private String sheetName = "Sheet1";
    private final List<String> headerLines = new ArrayList<>();
    private final List<Column<T>> columns = new ArrayList<>();
    private final List<SortKey> sortKeys = new ArrayList<>();
    private final List<String> sumColumnNames = new ArrayList<>();
    private String summaryLabelColumn;
    private String summaryLabelText;
    private int sortChunkSize = DEFAULT_CHUNK_SIZE;

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

    public ExcelBuilder<T> column(String name, ColumnType type, Function<? super T, ?> extractor) {
        columns.add(new Column<>(name, type, extractor));
        return this;
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

    /** Chunk-Größe (Zeilen pro in-memory sortiertem Run) des External Merge Sort. */
    public ExcelBuilder<T> sortChunkSize(int chunkSize) {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize muss >= 1 sein");
        }
        this.sortChunkSize = chunkSize;
        return this;
    }

    public void write(DataProvider<T> provider, Path out) throws IOException {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(out, "out");
        try (OutputStream os = Files.newOutputStream(out)) {
            write(provider, os);
        }
    }

    public void write(DataProvider<T> provider, OutputStream out) throws IOException {
        Objects.requireNonNull(provider, "provider");
        Objects.requireNonNull(out, "out");
        if (columns.isEmpty()) {
            throw new IllegalStateException("Mindestens eine Spalte muss definiert sein");
        }

        SummarySpec summary = buildSummarySpec();
        List<String> header = headerLines.isEmpty() ? null : headerLines;

        try (DataProvider<T> p = provider) {
            Iterator<Row> projected = projection(p);
            if (sortKeys.isEmpty()) {
                XlsxWriter.write(out, sheetName, columns, header, projected, summary);
            } else {
                RowComparator comparator = new RowComparator(columns, sortKeys);
                try (ExternalMergeSort sorter = new ExternalMergeSort(comparator, sortChunkSize)) {
                    // sort() konsumiert die Projektion (und damit den Provider) vollständig ...
                    CloseableIterator<Row> sorted = sorter.sort(projected);
                    // ... danach wird der sortierte Strom in die Datei geschrieben.
                    try (sorted) {
                        XlsxWriter.write(out, sheetName, columns, header, sorted, summary);
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
        return new SummarySpec(sum, labelIndex, summaryLabelText);
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

    /** Projiziert jeden Datensatz früh auf eine {@link Row} aus den extrahierten Zellenwerten. */
    private Iterator<Row> projection(DataProvider<T> provider) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return provider.hasNext();
            }

            @Override
            public Row next() {
                T record = provider.next();
                Object[] values = new Object[columns.size()];
                for (int i = 0; i < columns.size(); i++) {
                    values[i] = columns.get(i).extract(record);
                }
                return new Row(values);
            }
        };
    }
}
