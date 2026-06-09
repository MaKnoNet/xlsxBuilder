package de.makno.xlsxbuilder;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Overlaps producing the rows (projection/DB read + k-way merge) with writing: a daemon background
 * thread pulls from the source and fills a bounded {@link BlockingQueue}, while the consuming (writing)
 * thread takes from it. This keeps memory out-of-core (the queue is bounded) while read/sort I/O and
 * POI writing run in parallel.
 *
 * <p>Only one extra thread per sheet. {@link #close()} stops the thread cleanly (no leak) and is closed
 * via try-with-resources <em>before</em> the sorter/data provider.
 *
 * <p><b>Contract:</b> the underlying source ({@link DataProvider}/sort iterator) used in parallel mode
 * must be reasonably bounded and honor thread interruption. {@link #close()} interrupts the producer
 * and waits up to {@value #DEFAULT_JOIN_TIMEOUT_MS} ms; if a {@code next()} ignores the interrupt and
 * runs longer, the producer may still be reading the source while the caller proceeds to close it. In
 * that case a warning is logged (the stop is best-effort) instead of failing silently.
 */
final class PrefetchingRowIterator implements CloseableIterator<Row> {

    private static final Logger LOG = LogManager.getLogger(PrefetchingRowIterator.class);

    private static final int CAPACITY = 2048;
    private static final long DEFAULT_JOIN_TIMEOUT_MS = 5_000;
    private static final Object END = new Object();

    private final BlockingQueue<Object> queue = new ArrayBlockingQueue<>(CAPACITY);
    private final Thread producer;
    private final long joinTimeoutMillis;
    private volatile boolean closed = false;
    private volatile Throwable failure;

    private Row nextRow; // buffered next row or null
    private boolean finished; // END (sentinel) seen
    private boolean failureSurfaced; // consumer-thread only: was the producer failure re-thrown?

    PrefetchingRowIterator(Iterator<Row> source) {
        this(source, DEFAULT_JOIN_TIMEOUT_MS);
    }

    /** Package-private constructor with a configurable join timeout (used in tests for fast verification). */
    PrefetchingRowIterator(Iterator<Row> source, long joinTimeoutMillis) {
        this.joinTimeoutMillis = joinTimeoutMillis;
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
            throw new IllegalStateException("Interrupted while waiting for the next data row", e);
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
            producer.join(joinTimeoutMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (producer.isAlive()) {
            // The source did not honor the interrupt within the timeout. The producer may still be
            // reading the source while the caller proceeds to close it (potential close/read race).
            LOG.warn(
                    "Prefetch producer thread not stopped after {} ms – the data source does not respond to"
                            + " interrupt; it may still be read while it is being closed",
                    joinTimeoutMillis);
        }
        Throwable f = failure;
        if (f != null && !failureSurfaced) {
            // A producer error was recorded but never surfaced to the consumer (e.g. the consumer aborted
            // before reaching END). Don't throw here – a primary exception is likely already in flight.
            LOG.warn("Producer error from the prefetch pipeline is discarded on close (not propagated)", f);
        }
    }

    private void rethrowIfFailed() {
        Throwable t = failure;
        if (t == null) {
            return;
        }
        failureSurfaced = true;
        if (t instanceof RuntimeException re) {
            throw re;
        }
        if (t instanceof Error err) {
            throw err;
        }
        throw new IllegalStateException("Error while reading the data source", t);
    }
}
