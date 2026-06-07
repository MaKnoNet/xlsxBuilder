package de.makno.xlsxbuilder.builder;

/**
 * Unchecked wrapper for a {@link java.sql.SQLException} occurring inside the {@link DataProvider}
 * methods ({@code hasNext()}/{@code next()}) – whose signatures do not allow a checked exception.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
