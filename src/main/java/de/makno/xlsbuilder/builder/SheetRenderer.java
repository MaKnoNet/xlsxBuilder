package de.makno.xlsbuilder.builder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Führt einen {@link RenderJob} aus: projiziert die (gefilterten) Datensätze gestreamt auf
 * {@link Row}s, sortiert bei Bedarf out-of-core via {@link ExternalMergeSort}, überlappt optional
 * Lesen/Sortieren mit dem Schreiben ({@link PrefetchingRowIterator}) und schreibt das Blatt über den
 * {@link XlsxWriter} in das Workbook. Gegenstück zur Konfigurationsseite ({@link ExcelBuilder}).
 *
 * <p>Zustandslos – alle Eingaben stecken im {@link RenderJob}; die forward-only Datenquelle wird genau
 * einmal konsumiert und vom Renderer geschlossen.
 */
final class SheetRenderer {

    private static final Logger LOG = LogManager.getLogger(SheetRenderer.class);

    private SheetRenderer() {}

    /** Schreibt das vom {@code job} beschriebene Blatt in {@code wb}; liefert die Anzahl Datenzeilen. */
    static <T> int render(SXSSFWorkbook wb, RenderJob<T> job) throws IOException {
        long start = System.nanoTime();
        SortSpec sort = job.sort();
        int rows;
        try (DataProvider<T> provider = job.dataProvider()) {
            Iterator<Row> projected = projection(job, provider);
            if (sort.sortKeys().isEmpty()) {
                rows = consume(wb, job, projected);
            } else {
                RowComparator comparator = new RowComparator(job.columns(), sort.sortKeys());
                try (ExternalMergeSort sorter =
                        new ExternalMergeSort(comparator, sort.sortChunkSize(), sort.sortTempDir())) {
                    CloseableIterator<Row> sorted = sorter.sort(projected);
                    try (sorted) {
                        rows = consume(wb, job, sorted);
                    }
                }
            }
        }
        LOG.debug(
                "Blatt '{}': {} Zeilen ({}{}) in {} ms",
                job.sheetName(),
                rows,
                sort.sortKeys().isEmpty() ? "unsortiert" : "sortiert",
                job.parallel() ? ", parallel" : "",
                (System.nanoTime() - start) / 1_000_000);
        return rows;
    }

    /** Schreibt den Strom – optional über eine Prefetch-Pipeline (lesen/sortieren ∥ schreiben). */
    private static <T> int consume(SXSSFWorkbook wb, RenderJob<T> job, Iterator<Row> rows) throws IOException {
        if (!job.parallel()) {
            return XlsxWriter.addSheet(wb, job.sheetName(), job.columns(), rows, job.summary(), job.layout());
        }
        try (PrefetchingRowIterator prefetch = new PrefetchingRowIterator(rows)) {
            return XlsxWriter.addSheet(wb, job.sheetName(), job.columns(), prefetch, job.summary(), job.layout());
        }
    }

    /**
     * Projiziert jeden (vom optionalen Filter akzeptierten) Datensatz früh auf eine {@link Row}.
     * Look-ahead-Iterator, da die Quelle forward-only ist und nicht passende Datensätze übersprungen
     * werden.
     */
    private static <T> Iterator<Row> projection(RenderJob<T> job, DataProvider<T> provider) {
        Predicate<? super T> filter = job.filter();
        return new Iterator<>() {
            private Row pending; // vorausgelesene, gefilterte Zeile oder null

            @Override
            public boolean hasNext() {
                while (pending == null && provider.hasNext()) {
                    T record = provider.next();
                    if (filter == null || filter.test(record)) {
                        pending = project(job, record);
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
    private static <T> Row project(RenderJob<T> job, T record) {
        List<Column<T>> columns = job.columns();
        Object[] values = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            values[i] = columns.get(i).extract(record);
        }
        return new Row(values);
    }
}
