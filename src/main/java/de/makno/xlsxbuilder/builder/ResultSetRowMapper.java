package de.makno.xlsxbuilder.builder;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Bildet die aktuelle Zeile eines {@link ResultSet} auf ein Objekt {@code T} ab. Wird von
 * {@link DataProviders#ofResultSet(ResultSet, ResultSetRowMapper)} verwendet.
 *
 * <p>Der Mapper liest nur die Spalten der <em>aktuellen</em> Zeile aus (z. B. {@code rs.getString(...)})
 * und ruft <strong>nicht</strong> {@code rs.next()} auf – das übernimmt der Adapter.
 */
@FunctionalInterface
public interface ResultSetRowMapper<T> {

    /** Liest die aktuelle Zeile des {@link ResultSet} und liefert das gemappte Objekt. */
    T map(ResultSet rs) throws SQLException;
}
