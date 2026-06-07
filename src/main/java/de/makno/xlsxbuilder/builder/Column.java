package de.makno.xlsxbuilder.builder;

import java.util.Objects;
import java.util.function.Function;

/**
 * A table column: header, logical type and an extractor that produces the cell value from a record
 * {@code T}.
 */
public final class Column<T> {

    private final String name;
    private ColumnType type;
    private String format;
    private String nullText;
    private final Function<? super T, ?> extractor;
    private Function<Object, Object> converter;

    public Column(String name, ColumnType type, Function<? super T, ?> extractor) {
        this(name, type, null, extractor);
    }

    /**
     * @param format optional Excel format code (e.g. {@code "#,##0.00"}, {@code "dd.mm.yyyy"},
     *               {@code "hh:mm:ss"}); {@code null} = the type's default format.
     */
    public Column(String name, ColumnType type, String format, Function<? super T, ?> extractor) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.format = format;
        this.extractor = Objects.requireNonNull(extractor, "extractor");
    }

    public String name() {
        return name;
    }

    public ColumnType type() {
        return type;
    }

    /** Optional Excel format code, or {@code null}. */
    public String format() {
        return format;
    }

    /** Column-specific placeholder for {@code null} values, or {@code null} (= sheet-wide default). */
    public String nullText() {
        return nullText;
    }

    /** Package-internal: set the column type (used by {@code XlsxBuilder.ofType(...)}). */
    void setType(ColumnType type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    /** Package-internal: set the column's format code (used by {@code XlsxBuilder.formatForType(...)}). */
    void setFormat(String format) {
        this.format = format;
    }

    /** Package-internal: set the column-specific null placeholder (used by {@code XlsxBuilder.nullText(...)}). */
    void setNullText(String nullText) {
        this.nullText = nullText;
    }

    /** Package-internal: set the optional value converter (used by {@code XlsxBuilder.convertToColumnType(...)}). */
    void setConverter(Function<Object, Object> converter) {
        this.converter = converter;
    }

    /**
     * Returns the cell value for the record (may be {@code null}). If a converter is set, the extracted
     * raw value (unless {@code null}) is transformed into the representation matching the target type –
     * e.g. an {@code int} into a {@link java.time.LocalTime}.
     */
    public Object extract(T record) {
        Object value = extractor.apply(record);
        if (value == null || converter == null) {
            return value;
        }
        return converter.apply(value);
    }
}
