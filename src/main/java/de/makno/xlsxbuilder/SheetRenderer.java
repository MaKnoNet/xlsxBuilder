package de.makno.xlsxbuilder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Executes a {@link RenderJob}: projects the (filtered) records streamed onto {@link Row}s, sorts
 * out-of-core when needed via {@link ExternalMergeSort}, optionally overlaps reading/sorting with
 * writing ({@link PrefetchingRowIterator}), and writes the sheet into the workbook through the
 * {@link XlsxWriter}. Counterpart to the configuration side ({@link XlsxBuilder}).
 *
 * <p>Stateless – all inputs live in the {@link RenderJob}; the forward-only data source is consumed
 * exactly once and closed by the renderer.
 */
final class SheetRenderer {

    private static final Logger LOG = LogManager.getLogger(SheetRenderer.class);

    private SheetRenderer() {}

    /** Writes the sheet described by {@code job} into {@code wb}; returns the number of data rows. */
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
                "Sheet '{}': {} rows ({}{}) in {} ms",
                job.sheetName(),
                rows,
                sort.sortKeys().isEmpty() ? "unsorted" : "sorted",
                job.parallel() ? ", parallel" : "",
                (System.nanoTime() - start) / 1_000_000);
        return rows;
    }

    /** Writes the stream – optionally via a prefetch pipeline (read/sort ∥ write). */
    private static <T> int consume(SXSSFWorkbook wb, RenderJob<T> job, Iterator<Row> rows) throws IOException {
        if (!job.parallel()) {
            return XlsxWriter.addSheet(wb, job.sheetName(), job.columns(), rows, job.summary(), job.layout());
        }
        try (PrefetchingRowIterator prefetch = new PrefetchingRowIterator(rows)) {
            return XlsxWriter.addSheet(wb, job.sheetName(), job.columns(), prefetch, job.summary(), job.layout());
        }
    }

    /**
     * Projects each record (accepted by the optional filter) eagerly onto a {@link Row}. Look-ahead
     * iterator, since the source is forward-only and non-matching records are skipped.
     */
    private static <T> Iterator<Row> projection(RenderJob<T> job, DataProvider<T> provider) {
        Predicate<? super T> filter = job.filter();
        return new Iterator<>() {
            private Row pending; // pre-read, filtered row or null

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

    /** Projects a record onto a {@link Row} from the extracted cell values. */
    private static <T> Row project(RenderJob<T> job, T record) {
        List<Column<T>> columns = job.columns();
        Object[] values = new Object[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            values[i] = columns.get(i).extract(record);
        }
        return new Row(values);
    }
}
