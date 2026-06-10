package de.makno.xlsxbuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/** Factory for common {@link DataProvider} adapters. */
public final class DataProviders {

    private DataProviders() {}

    public static <T> DataProvider<T> ofIterator(Iterator<? extends T> iterator) {
        java.util.Objects.requireNonNull(iterator, "iterator");
        return new DataProvider<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                if (!iterator.hasNext()) {
                    throw new NoSuchElementException();
                }
                return iterator.next();
            }
        };
    }

    public static <T> DataProvider<T> ofIterable(Iterable<? extends T> iterable) {
        java.util.Objects.requireNonNull(iterable, "iterable");
        return ofIterator(iterable.iterator());
    }

    /** Adapts a {@link Stream}; the stream is closed on {@link DataProvider#close()}. */
    public static <T> DataProvider<T> ofStream(Stream<? extends T> stream) {
        java.util.Objects.requireNonNull(stream, "stream");
        Iterator<? extends T> iterator = stream.iterator();
        return new DataProvider<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                if (!iterator.hasNext()) {
                    throw new NoSuchElementException();
                }
                return iterator.next();
            }

            @Override
            public void close() {
                stream.close();
            }
        };
    }

    /**
     * Adapts a JDBC {@link ResultSet} as a forward-only {@link DataProvider} – ideal for true
     * out-of-core cases, since the database delivers the rows streamed (ideally with
     * {@code TYPE_FORWARD_ONLY} and a suitable {@code fetchSize}).
     *
     * <p>{@link DataProvider#close()} closes <strong>only the {@code ResultSet}</strong>. The associated
     * {@code Statement} and the {@code Connection} are managed by the caller (e.g. via
     * try-with-resources). Any {@link SQLException}s that occur are wrapped in a
     * {@link DataAccessException}.
     *
     * <p>Not thread-safe / single-use (like a {@code ResultSet} itself).
     */
    public static <T> DataProvider<T> ofResultSet(ResultSet rs, ResultSetRowMapper<? extends T> mapper) {
        java.util.Objects.requireNonNull(rs, "rs");
        java.util.Objects.requireNonNull(mapper, "mapper");
        return new DataProvider<T>() {
            private boolean lookedAhead = false;
            private boolean hasRow = false;

            @Override
            public boolean hasNext() {
                if (!lookedAhead) {
                    try {
                        hasRow = rs.next();
                    } catch (SQLException e) {
                        throw new DataAccessException("ResultSet.next() failed", e);
                    }
                    lookedAhead = true;
                }
                return hasRow;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                lookedAhead = false; // the current row is consumed
                try {
                    return mapper.map(rs);
                } catch (SQLException e) {
                    throw new DataAccessException("Mapping the ResultSet row failed", e);
                }
            }

            @Override
            public void close() {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new DataAccessException("Closing the ResultSet failed", e);
                }
            }
        };
    }
}
