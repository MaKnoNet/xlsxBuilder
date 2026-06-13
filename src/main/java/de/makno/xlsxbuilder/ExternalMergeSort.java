package de.makno.xlsxbuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * External merge sort over {@link Row}s. Works even for data sets that do not fit in memory:
 * <ol>
 *   <li>The source is read in fixed-size chunks; each chunk is sorted in memory and serialized as a
 *       sorted "run" to a temp file.</li>
 *   <li>If the number of runs exceeds the maximum fan-in ({@value #MAX_FAN_IN}), they are pre-merged in
 *       several passes until at most {@value #MAX_FAN_IN} runs remain. This way at most
 *       {@value #MAX_FAN_IN} files are ever open at once (OS file-handle limit).</li>
 *   <li>The remaining runs are combined by a k-way merge (a {@link PriorityQueue} over the run heads)
 *       and delivered as a sorted stream.</li>
 * </ol>
 * Memory usage is bounded by the chunk size and the fan-in, independent of the total row count. Temp
 * files are deleted on {@link #close()}.
 */
final class ExternalMergeSort implements Closeable {

    private static final Logger LOG = LogManager.getLogger(ExternalMergeSort.class);

    /** Maximum number of runs open at once during the merge (bounds open file handles). */
    private static final int MAX_FAN_IN = 16;

    private final Comparator<Row> comparator;
    private final int chunkSize;
    private final Path baseTempDir; // base directory for runs; null = system temp
    private final List<Path> runFiles = new ArrayList<>();
    private Path tempDir;

    // Performance metrics (for logging only).
    private long rowsRead;
    private int initialRuns;
    private int mergePasses;

    ExternalMergeSort(Comparator<Row> comparator, int chunkSize) {
        this(comparator, chunkSize, null);
    }

    /**
     * @param baseTempDir base directory for the run files; {@code null} = system temp
     *                    ({@code java.io.tmpdir}). Created on demand.
     */
    ExternalMergeSort(Comparator<Row> comparator, int chunkSize, Path baseTempDir) {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize must be >= 1");
        }
        this.comparator = comparator;
        this.chunkSize = chunkSize;
        this.baseTempDir = baseTempDir;
    }

    /**
     * Consumes the source fully (creating the runs) and returns the sorted merge stream. On error all
     * created temp files are deleted automatically ({@link #close()} is called).
     */
    CloseableIterator<Row> sort(java.util.Iterator<Row> source) throws IOException {
        long startNanos = System.nanoTime();
        try {
            if (baseTempDir != null) {
                Files.createDirectories(baseTempDir);
                tempDir = Files.createTempDirectory(baseTempDir, "xlsxbuilder-sort-");
            } else {
                tempDir = Files.createTempDirectory("xlsxbuilder-sort-");
            }
            List<Path> runs = new ArrayList<>();
            List<Row> buffer = new ArrayList<>(Math.min(chunkSize, 1024));
            while (source.hasNext()) {
                buffer.add(source.next());
                rowsRead++;
                if (buffer.size() >= chunkSize) {
                    runs.add(flushRun(buffer));
                    buffer.clear();
                }
            }
            if (!buffer.isEmpty()) {
                runs.add(flushRun(buffer));
            }
            initialRuns = runs.size();
            // Bound the fan-in so the final merge never keeps too many files open at once.
            runs = reduceToFanIn(runs);
            CloseableIterator<Row> merged = new MergeIterator(runs, comparator);
            LOG.debug(
                    "External Merge Sort: {} rows, {} runs, {} pre-merge passes (chunkSize={}), "
                            + "runs+pre-merge in {} ms, temp={}",
                    rowsRead,
                    initialRuns,
                    mergePasses,
                    chunkSize,
                    (System.nanoTime() - startNanos) / 1_000_000,
                    tempDir);
            return merged;
        } catch (IOException e) {
            // Cleanup on error: delete all temp files created so far.
            close();
            throw e;
        }
    }

    /** Sorts the buffer in memory and writes it as a sorted run to a new temp file. */
    private Path flushRun(List<Row> buffer) throws IOException {
        buffer.sort(comparator);
        Path run = Files.createTempFile(tempDir, "run-", ".bin");
        runFiles.add(run); // register immediately, so close() also cleans up on a write error
        writeRun(run, buffer.iterator(), buffer.size());
        return run;
    }

    /**
     * Reduces the run list through multi-pass pre-merging until at most {@value #MAX_FAN_IN} runs
     * remain. Already-merged input runs are deleted after each pass (to save space).
     */
    private List<Path> reduceToFanIn(List<Path> runs) throws IOException {
        while (runs.size() > MAX_FAN_IN) {
            mergePasses++;
            List<Path> merged = new ArrayList<>();
            for (int i = 0; i < runs.size(); i += MAX_FAN_IN) {
                List<Path> group = runs.subList(i, Math.min(i + MAX_FAN_IN, runs.size()));
                merged.add(mergeGroupToRun(group));
            }
            runs = merged;
        }
        return runs;
    }

    /** Merges a group of sorted runs into a new sorted run and deletes the input runs. */
    private Path mergeGroupToRun(List<Path> group) throws IOException {
        Path out = Files.createTempFile(tempDir, "merge-", ".bin");
        runFiles.add(out);
        List<RunReader> readers = new ArrayList<>(group.size());
        try {
            PriorityQueue<Node> queue = new PriorityQueue<>((a, b) -> comparator.compare(a.row(), b.row()));
            long total = 0;
            for (Path run : group) {
                RunReader reader = new RunReader(run);
                readers.add(reader);
                total += reader.remaining();
                Row first = reader.next();
                if (first != null) {
                    queue.add(new Node(first, reader));
                }
            }
            writeRun(out, new MergingIterator(queue), total);
        } finally {
            // Close the readers first (Windows refuses to delete a file that is still open), then free
            // the input runs immediately - even on a write error, so a failing pass does not leave the
            // whole group on disk until close() runs. The runs stay registered in runFiles, so close()
            // still reclaims anything missed here.
            for (RunReader reader : readers) {
                closeQuietly(reader);
            }
            for (Path run : group) {
                try {
                    Files.deleteIfExists(run);
                } catch (IOException ignored) {
                    // best effort
                }
            }
        }
        return out;
    }

    /** Writes {@code count} rows (in iterator order) as a run file. */
    private static void writeRun(Path file, java.util.Iterator<Row> rows, long count) throws IOException {
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeLong(count);
            while (rows.hasNext()) {
                RowCodec.writeRow(out, rows.next());
            }
        }
    }

    private static void closeQuietly(Closeable c) {
        try {
            c.close();
        } catch (IOException ignored) {
            // best effort
        }
    }

    @Override
    public void close() {
        for (Path run : runFiles) {
            try {
                Files.deleteIfExists(run);
            } catch (IOException ignored) {
                // best effort
            }
        }
        runFiles.clear();
        if (tempDir != null) {
            try {
                Files.deleteIfExists(tempDir);
            } catch (IOException ignored) {
                // best effort
            }
            tempDir = null;
        }
    }

    /** Reads a sorted run row by row from disk. */
    private static final class RunReader implements Closeable {
        private final DataInputStream in;
        private long remaining;

        RunReader(Path file) throws IOException {
            this.in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)));
            this.remaining = in.readLong();
        }

        /** Number of rows of this run not yet read. */
        long remaining() {
            return remaining;
        }

        Row next() {
            if (remaining <= 0) {
                return null;
            }
            try {
                Row row = RowCodec.readRow(in);
                remaining--;
                return row;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    private record Node(Row row, RunReader reader) {}

    /**
     * Pulls the next row from a pre-filled {@link PriorityQueue} of run heads (k-way merge). Used both
     * for pre-merging into a file ({@link #writeRun}) and – via {@link MergeIterator} – for the final
     * stream.
     */
    private static Row pollNext(PriorityQueue<Node> queue) {
        Node node = queue.poll();
        if (node == null) {
            return null;
        }
        Row result = node.row();
        Row following = node.reader().next();
        if (following != null) {
            queue.add(new Node(following, node.reader()));
        }
        return result;
    }

    /** Adapts a merge queue as a simple (non-resource-holding) iterator for {@link #writeRun}. */
    private static final class MergingIterator implements java.util.Iterator<Row> {
        private final PriorityQueue<Node> queue;

        MergingIterator(PriorityQueue<Node> queue) {
            this.queue = queue;
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public Row next() {
            Row row = pollNext(queue);
            if (row == null) {
                throw new NoSuchElementException();
            }
            return row;
        }
    }

    /** k-way merge over the heads of all (remaining) runs as a closeable result stream. */
    private static final class MergeIterator implements CloseableIterator<Row> {
        private final PriorityQueue<Node> queue;
        private final List<RunReader> readers = new ArrayList<>();

        /**
         * Constructs a MergeIterator over all run files. On errors while opening the files, all readers
         * opened so far are closed.
         *
         * @throws IOException if a run file cannot be read
         */
        MergeIterator(List<Path> runs, Comparator<Row> comparator) throws IOException {
            this.queue = new PriorityQueue<>((a, b) -> comparator.compare(a.row(), b.row()));
            try {
                for (Path run : runs) {
                    RunReader reader = new RunReader(run);
                    readers.add(reader);
                    Row first = reader.next();
                    if (first != null) {
                        queue.add(new Node(first, reader));
                    }
                }
            } catch (IOException e) {
                // Cleanup on error: close all readers opened so far.
                closeReaders();
                throw e;
            }
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public Row next() {
            Row row = pollNext(queue);
            if (row == null) {
                throw new NoSuchElementException();
            }
            return row;
        }

        @Override
        public void close() {
            closeReaders();
        }

        /** Helper to close all readers (called on error or close()). */
        private void closeReaders() {
            for (RunReader reader : readers) {
                closeQuietly(reader);
            }
            readers.clear();
            queue.clear();
        }
    }
}
