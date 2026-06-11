package de.makno.xlsxbuilder;

/**
 * Thrown when a sheet's data exceeds the maximum number of rows per worksheet (Excel: 1,048,576 –
 * including title/group/column-header rows and the rows reserved for the summary row and footers) and
 * the sheet is not configured to split. Enable
 * {@link XlsxBuilder#splitOnRowLimit(boolean) splitOnRowLimit(true)} to continue on follow-up sheets
 * instead.
 */
public class RowLimitExceededException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    /** Package-internal: thrown only by the library itself. */
    RowLimitExceededException(String message) {
        super(message);
    }
}
