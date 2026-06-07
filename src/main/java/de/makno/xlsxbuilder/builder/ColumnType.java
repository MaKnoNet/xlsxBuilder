package de.makno.xlsxbuilder.builder;

/**
 * Logical type of a column. Controls how the projected value is written as an Excel cell
 * (cell type/format) and how it is compared when sorting.
 */
public enum ColumnType {
    STRING(true),
    INTEGER(true),
    LONG(true),
    DOUBLE(true),
    DECIMAL(true),
    BOOLEAN(true),
    DATE(true),
    DATETIME(true),
    /** Time of day (without a date); expects {@link java.time.LocalTime} or {@link java.time.LocalDateTime}. */
    TIME(true),
    /**
     * Excel formula. The extractor supplies the formula text without a leading {@code =} (e.g.
     * {@code "F{row}*0.1"}); the placeholder {@code {row}} is replaced with the actual row number.
     * Excel computes the values when the file is opened. Formulas are not sortable.
     */
    FORMULA(false);

    /** Whether this column type is sortable. Formulas, for instance, are not sortable. */
    private final boolean sortable;

    ColumnType(boolean sortable) {
        this.sortable = sortable;
    }

    /**
     * Whether this column type is sortable.
     *
     * @return {@code true} if the type can be sorted, {@code false} otherwise
     */
    public boolean isSortable() {
        return sortable;
    }
}
