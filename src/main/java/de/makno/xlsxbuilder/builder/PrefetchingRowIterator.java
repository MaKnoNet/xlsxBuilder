package de.makno.xlsxbuilder.builder;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Overlaps producing the rows (projection/DB read + k-way merge) with writing: a daemon background
 * thread pulls from the source and fills a bounded {@link BlockingQueue}, while the consuming (writing)
 * thread takes from it. This keeps memory out-of-core (the queue is bounded) while read/sort I/O and
 * POI writing run in parallel.
 *
 * <p>Only one extra thread per sheet. {@link #close()} stops the thread cleanly (no leak) and is closed
 * via try-with-resources <em>before</em> the sorter/data provider.
 */
final class PrefetchingRowIterator implements CloseableIterator<Row> {

    private static final int CAPACITY = 2048;
    private static final Object END = new Object();

    private final BlockingQueue<Object> queue = new ArrayBlockingQueue<>(CAPACITY);
    private final Thread producer;
    private volatile boolean closed = false;
    private volatile Throwable failure;

    private Row nextRow; // buffered next row or null
    private boolean finished; // END (sentinel) seen

    PrefetchingRowIterator(Iterator<Row> source) {
        this.producer = new Thread(() -> produce(source), "xlsxbuilder-prefetch");
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
            Thread.currentThread().interrupt(); // on close
        } catch (Throwable t) {
            failure = t; // re-thrown at the consumer
        } finally {
            signalEnd();
        }
    }

    /** Place the sentinel at the end, unless the consumer has already aborted anyway. */
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
        queue.clear(); // unblocks a possibly blocking put() in the producer
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
