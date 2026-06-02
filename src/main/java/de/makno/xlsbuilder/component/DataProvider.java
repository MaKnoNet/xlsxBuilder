package de.makno.xlsbuilder.component;

import java.io.Closeable;

/**
 * Forward-only Datenquelle für den {@link ExcelBuilder}. Wird genau einmal durchlaufen,
 * sodass auch Datenmengen verarbeitet werden können, die nicht vollständig in den Speicher passen
 * (z. B. ein JDBC-{@code ResultSet} oder ein gepufferter Datei-Reader).
 */
public interface DataProvider<T> extends Closeable {

    /** {@code true}, solange noch ein weiterer Datensatz verfügbar ist. */
    boolean hasNext();

    /** Liefert den nächsten Datensatz. */
    T next();

    /** Standard: nichts zu schließen. Quellen mit Ressourcen (DB, Datei) überschreiben dies. */
    @Override
    default void close() {
    }
}
