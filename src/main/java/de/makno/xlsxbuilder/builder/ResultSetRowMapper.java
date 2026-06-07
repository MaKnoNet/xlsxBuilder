package de.makno.xlsxbuilder.builder;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Maps the current row of a {@link ResultSet} to an object {@code T}. Used by
 * {@link DataProviders#ofResultSet(ResultSet, ResultSetRowMapper)}.
 *
 * <p>The mapper only reads the columns of the <em>current</em> row (e.g. {@code rs.getString(...)})
 * and does <strong>not</strong> call {@code rs.next()} – the adapter does that.
 */
@FunctionalInterface
public interface ResultSetRowMapper<T> {

    /** Reads the current row of the {@link ResultSet} and returns the mapped object. */
    T map(ResultSet rs) throws SQLException;
}
