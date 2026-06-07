package de.makno.xlsxbuilder.builder;

import java.io.Closeable;

/**
 * Forward-only data source for the {@link XlsxBuilder}. It is traversed exactly once, so that data
 * sets which do not fully fit in memory can also be processed (e.g. a JDBC {@code ResultSet} or a
 * buffered file reader).
 */
public interface DataProvider<T> extends Closeable {

    /** {@code true} as long as another record is available. */
    boolean hasNext();

    /** Returns the next record. */
    T next();

    /** Default: nothing to close. Sources holding resources (DB, file) override this. */
    @Override
    default void close() {}
}
