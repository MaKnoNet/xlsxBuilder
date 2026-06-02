package de.makno.xlsbuilder.builder;

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
 *   <li>Übersteigt die Anzahl der Runs den maximalen Fan-in ({@value #MAX_FAN_IN}), werden sie in
 *       mehreren Durchgängen vorgemerged, bis höchstens {@value #MAX_FAN_IN} Runs übrig sind. So
 *       sind nie mehr als {@value #MAX_FAN_IN} Dateien gleichzeitig offen (OS-File-Handle-Limit).</li>
 *   <li>Die verbleibenden Runs werden per k-way-Merge (eine {@link PriorityQueue} über die Köpfe
 *       der Runs) zusammengeführt und als sortierter Strom geliefert.</li>
 * </ol>
 * Der Speicherbedarf ist durch die Chunk-Größe und den Fan-in begrenzt, unabhängig von der
 * Gesamtzeilenzahl. Temp-Dateien werden bei {@link #close()} gelöscht.
 */
final class ExternalMergeSort implements Closeable {

    /** Maximale Anzahl gleichzeitig offener Runs beim Merge (begrenzt offene File-Handles). */
    private static final int MAX_FAN_IN = 16;

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

    /**
     * Konsumiert die Quelle vollständig (erzeugt die Runs) und liefert den sortierten Merge-Strom.
     * Bei Fehlern werden alle erstellten Temp-Dateien automatisch gelöscht ({@link #close()} wird aufgerufen).
     */
    CloseableIterator<Row> sort(java.util.Iterator<Row> source) throws IOException {
        try {
            tempDir = Files.createTempDirectory("xlsbuilder-sort-");
            List<Path> runs = new ArrayList<>();
            List<Row> buffer = new ArrayList<>(Math.min(chunkSize, 1024));
            while (source.hasNext()) {
                buffer.add(source.next());
                if (buffer.size() >= chunkSize) {
                    runs.add(flushRun(buffer));
                    buffer.clear();
                }
            }
            if (!buffer.isEmpty()) {
                runs.add(flushRun(buffer));
            }
            // Fan-in begrenzen, damit der finale Merge nie zu viele Dateien gleichzeitig offen hält.
            runs = reduceToFanIn(runs);
            return new MergeIterator(runs, comparator);
        } catch (IOException e) {
            // Cleanup bei Fehler: alle bis jetzt erstellten Temp-Dateien löschen
            close();
            throw e;
        }
    }

    /** Sortiert den Puffer in-memory und schreibt ihn als sortierten Run auf eine neue Temp-Datei. */
    private Path flushRun(List<Row> buffer) throws IOException {
        buffer.sort(comparator);
        Path run = Files.createTempFile(tempDir, "run-", ".bin");
        runFiles.add(run); // sofort vormerken, damit close() auch bei Schreibfehler aufräumt
        writeRun(run, buffer.iterator(), buffer.size());
        return run;
    }

    /**
     * Reduziert die Run-Liste durch mehrstufiges Vormerging, bis höchstens {@value #MAX_FAN_IN} Runs
     * übrig sind. Bereits gemergte Eingangs-Runs werden nach jedem Durchgang gelöscht (Platz sparen).
     */
    private List<Path> reduceToFanIn(List<Path> runs) throws IOException {
        while (runs.size() > MAX_FAN_IN) {
            List<Path> merged = new ArrayList<>();
            for (int i = 0; i < runs.size(); i += MAX_FAN_IN) {
                List<Path> group = runs.subList(i, Math.min(i + MAX_FAN_IN, runs.size()));
                merged.add(mergeGroupToRun(group));
            }
            runs = merged;
        }
        return runs;
    }

    /** Merged eine Gruppe sortierter Runs in einen neuen sortierten Run und löscht die Eingangs-Runs. */
    private Path mergeGroupToRun(List<Path> group) throws IOException {
        Path out = Files.createTempFile(tempDir, "merge-", ".bin");
        runFiles.add(out);
        List<RunReader> readers = new ArrayList<>(group.size());
        try {
            PriorityQueue<Node> queue =
                    new PriorityQueue<>((a, b) -> comparator.compare(a.row(), b.row()));
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
            for (RunReader reader : readers) {
                closeQuietly(reader);
            }
        }
        // Eingangs-Runs werden nicht mehr gebraucht -> Plattenplatz sofort freigeben.
        for (Path run : group) {
            try {
                Files.deleteIfExists(run);
            } catch (IOException ignored) {
                // best effort
            }
        }
        return out;
    }

    /** Schreibt {@code count} Zeilen (in der Reihenfolge des Iterators) als Run-Datei. */
    private static void writeRun(Path file, java.util.Iterator<Row> rows, long count) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(Files.newOutputStream(file)))) {
            out.writeLong(count);
            while (rows.hasNext()) {
                out.writeObject(rows.next());
                // Handle-Tabelle leeren: sonst halten ObjectOutput-/ObjectInputStream Referenzen
                // auf jedes je geschriebene/gelesene Objekt -> unbegrenztes Wachstum bei großen Runs.
                out.reset();
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

    /** Liest einen sortierten Run zeilenweise von der Platte. */
    private static final class RunReader implements Closeable {
        private final ObjectInputStream in;
        private long remaining;

        RunReader(Path file) throws IOException {
            this.in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(file)));
            this.remaining = in.readLong();
        }

        /** Anzahl noch nicht gelesener Zeilen dieses Runs. */
        long remaining() {
            return remaining;
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

    /**
     * Zieht die nächste Zeile aus einer vorbefüllten {@link PriorityQueue} von Run-Köpfen
     * (k-way-Merge). Wird sowohl für das Vormerging in eine Datei ({@link #writeRun}) als auch –
     * über {@link MergeIterator} – für den finalen Strom verwendet.
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

    /** Adaptiert eine Merge-Queue als einfachen (nicht ressourcenhaltenden) Iterator für {@link #writeRun}. */
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

    /** k-way-Merge über die Köpfe aller (verbleibenden) Runs als schließbarer Ergebnis-Strom. */
    private static final class MergeIterator implements CloseableIterator<Row> {
        private final PriorityQueue<Node> queue;
        private final List<RunReader> readers = new ArrayList<>();

        /**
         * Konstruiert einen MergeIterator über alle Run-Dateien.
         * Bei Fehlern beim Öffnen der Dateien werden alle bis dahin geöffneten Reader geschlossen.
         *
         * @throws IOException wenn eine Run-Datei nicht gelesen werden kann
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
                // Cleanup bei Fehler: alle bis jetzt geöffneten Reader schließen
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

        /** Hilfsmethode zum Schließen aller Reader (aufgerufen bei Fehler oder close()). */
        private void closeReaders() {
            for (RunReader reader : readers) {
                closeQuietly(reader);
            }
            readers.clear();
            queue.clear();
        }
    }
}
