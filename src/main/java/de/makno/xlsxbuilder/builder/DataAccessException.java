package de.makno.xlsxbuilder.builder;

/**
 * Ungeprüfte Hülle für eine {@link java.sql.SQLException}, die in den {@link DataProvider}-Methoden
 * ({@code hasNext()}/{@code next()}) auftritt – deren Signaturen erlauben keine geprüfte Ausnahme.
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
