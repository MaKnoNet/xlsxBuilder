package de.makno.xlsbuilder.builder;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Überlappt das Erzeugen der Zeilen (Projektion/DB-Read + k-way-Merge) mit dem Schreiben: ein
 * Daemon-Hintergrund-Thread zieht aus der Quelle und füllt eine beschränkte {@link BlockingQueue},
 * der konsumierende (Schreib-)Thread entnimmt. So bleibt der Speicher out-of-core (Queue ist
 * begrenzt), während Lese-/Sortier-I/O und POI-Schreiben parallel laufen.
 *
 * <p>Nur ein Zusatz-Thread je Blatt. {@link #close()} stoppt den Thread sauber (kein Leak) und wird
 * via try-with-resources <em>vor</em> Sorter/DataProvider geschlossen.
 */
final class PrefetchingRowIterator implements CloseableIterator<Row> {

    private static final int CAPACITY = 2048;
    private static final Object END = new Object();

    private final BlockingQueue<Object> queue = new ArrayBlockingQueue<>(CAPACITY);
    private final Thread producer;
    private volatile boolean closed = false;
    private volatile Throwable failure;

    private Row nextRow; // gepufferte nächste Zeile oder null
    private boolean finished; // END (Sentinel) gesehen

    PrefetchingRowIterator(Iterator<Row> source) {
        this.producer = new Thread(() -> produce(source), "xlsbuilder-prefetch");
        this.producer.setDaemon(true);
        this.producer.start();
    }

    private void produce(Iterator<Row> source) {
        try {
            while (source.hasNext()) {
                queue.put(source.next());
                if (closed) {
                    return;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // beim Schließen
        } catch (Throwable t) {
            failure = t; // wird beim Konsumenten erneut geworfen
        } finally {
            signalEnd();
        }
    }

    /** Sentinel ans Ende stellen, sofern der Konsument nicht ohnehin schon abgebrochen hat. */
    private void signalEnd() {
        if (closed) {
            return;
        }
        try {
            queue.put(END);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean hasNext() {
        if (nextRow != null) {
            return true;
        }
        if (finished) {
            return false;
        }
        Object item;
        try {
            item = queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Unterbrochen beim Warten auf die nächste Datenzeile", e);
        }
        if (item == END) {
            finished = true;
            rethrowIfFailed();
            return false;
        }
        nextRow = (Row) item;
        return true;
    }

    @Override
    public Row next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        Row row = nextRow;
        nextRow = null;
        return row;
    }

    @Override
    public void close() {
        closed = true;
        producer.interrupt();
        queue.clear(); // entsperrt ein evtl. blockierendes put() im Producer
        try {
            producer.join(5_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void rethrowIfFailed() {
        Throwable t = failure;
        if (t == null) {
            return;
        }
        if (t instanceof RuntimeException re) {
            throw re;
        }
        if (t instanceof Error err) {
            throw err;
        }
        throw new IllegalStateException("Fehler beim Lesen der Datenquelle", t);
    }
}
