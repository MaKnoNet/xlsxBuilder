package de.makno.xlsbuilder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;

/**
 * External Merge Sort über {@link Row}s. Funktioniert auch für Datenmengen, die nicht in den
 * Speicher passen:
 * <ol>
 *   <li>Die Quelle wird in Chunks fester Größe gelesen; jeder Chunk wird in-memory sortiert und
 *       als sortierter "Run" auf eine Temp-Datei serialisiert.</li>
 *   <li>Anschließend werden alle Runs per k-way-Merge (eine {@link PriorityQueue} über die Köpfe
 *       der Runs) zusammengeführt und als sortierter Strom geliefert.</li>
 * </ol>
 * Der Speicherbedarf ist durch die Chunk-Größe und die Anzahl gleichzeitig offener Runs begrenzt,
 * unabhängig von der Gesamtzeilenzahl. Temp-Dateien werden bei {@link #close()} gelöscht.
 */
final class ExternalMergeSort implements Closeable {

    private final Comparator<Row> comparator;
    private final int chunkSize;
    private final List<Path> runFiles = new ArrayList<>();
    private Path tempDir;

    ExternalMergeSort(Comparator<Row> comparator, int chunkSize) {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize muss >= 1 sein");
        }
        this.comparator = comparator;
        this.chunkSize = chunkSize;
    }

    /** Konsumiert die Quelle vollständig (erzeugt die Runs) und liefert den sortierten Merge-Strom. */
    CloseableIterator<Row> sort(java.util.Iterator<Row> source) throws IOException {
        tempDir = Files.createTempDirectory("xlsbuilder-sort-");
        List<Row> buffer = new ArrayList<>(Math.min(chunkSize, 1024));
        while (source.hasNext()) {
            buffer.add(source.next());
            if (buffer.size() >= chunkSize) {
                flushRun(buffer);
                buffer.clear();
            }
        }
        if (!buffer.isEmpty()) {
            flushRun(buffer);
        }
        return new MergeIterator(runFiles, comparator);
    }

    private void flushRun(List<Row> buffer) throws IOException {
        buffer.sort(comparator);
        Path run = Files.createTempFile(tempDir, "run-", ".bin");
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(run)))) {
            out.writeInt(buffer.size());
            for (Row row : buffer) {
                out.writeObject(row);
                // Handle-Tabelle leeren: sonst halten ObjectOutput-/ObjectInputStream Referenzen
                // auf jedes je geschriebene/gelesene Objekt -> unbegrenztes Wachstum bei großen Runs.
                out.reset();
            }
        }
        runFiles.add(run);
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

    /** Liest einen sortierten Run zeilenweise von der Platte. */
    private static final class RunReader implements Closeable {
        private final ObjectInputStream in;
        private int remaining;

        RunReader(Path file) throws IOException {
            this.in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(file)));
            this.remaining = in.readInt();
        }

        Row next() {
            if (remaining <= 0) {
                return null;
            }
            try {
                Row row = (Row) in.readObject();
                remaining--;
                return row;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void close() throws IOException {
            in.close();
        }
    }

    private record Node(Row row, RunReader reader) {
    }

    /** k-way-Merge über die Köpfe aller Runs. */
    private static final class MergeIterator implements CloseableIterator<Row> {
        private final PriorityQueue<Node> queue;
        private final List<RunReader> readers = new ArrayList<>();

        MergeIterator(List<Path> runs, Comparator<Row> comparator) throws IOException {
            this.queue = new PriorityQueue<>((a, b) -> comparator.compare(a.row(), b.row()));
            for (Path run : runs) {
                RunReader reader = new RunReader(run);
                readers.add(reader);
                Row first = reader.next();
                if (first != null) {
                    queue.add(new Node(first, reader));
                }
            }
        }

        @Override
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        @Override
        public Row next() {
            Node node = queue.poll();
            if (node == null) {
                throw new NoSuchElementException();
            }
            Row result = node.row();
            Row following = node.reader().next();
            if (following != null) {
                queue.add(new Node(following, node.reader()));
            }
            return result;
        }

        @Override
        public void close() {
            for (RunReader reader : readers) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    // best effort
                }
            }
            readers.clear();
            queue.clear();
        }
    }
}
