package de.makno.xlsxbuilder.builder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/** Factory für gängige {@link DataProvider}-Adapter. */
public final class DataProviders {

    private DataProviders() {}

    public static <T> DataProvider<T> ofIterator(Iterator<? extends T> iterator) {
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
        return ofIterator(iterable.iterator());
    }

    /** Adaptiert einen {@link Stream}; der Stream wird bei {@link DataProvider#close()} geschlossen. */
    public static <T> DataProvider<T> ofStream(Stream<? extends T> stream) {
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
     * Adaptiert ein JDBC-{@link ResultSet} als forward-only {@link DataProvider} – ideal für echte
     * Out-of-core-Fälle, da die Datenbank die Zeilen streamend liefert (idealerweise mit
     * {@code TYPE_FORWARD_ONLY} und passender {@code fetchSize}).
     *
     * <p>{@link DataProvider#close()} schließt <strong>nur das {@code ResultSet}</strong>. Das zugehörige
     * {@code Statement} und die {@code Connection} verwaltet der Aufrufer (z. B. via try-with-resources).
     * Auftretende {@link SQLException}s werden in eine {@link DataAccessException} verpackt.
     *
     * <p>Nicht thread-safe / single-use (wie ein {@code ResultSet} selbst).
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
                        throw new DataAccessException("ResultSet.next() fehlgeschlagen", e);
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
                lookedAhead = false; // aktuelle Zeile wird konsumiert
                try {
                    return mapper.map(rs);
                } catch (SQLException e) {
                    throw new DataAccessException("Mapping der ResultSet-Zeile fehlgeschlagen", e);
                }
            }

            @Override
            public void close() {
                try {
                    rs.close();
                } catch (SQLException e) {
                    throw new DataAccessException("Schließen des ResultSet fehlgeschlagen", e);
                }
            }
        };
    }
}
