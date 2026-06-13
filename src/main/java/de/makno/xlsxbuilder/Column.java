package de.makno.xlsxbuilder;

import java.util.Objects;
import java.util.function.Function;

/**
 * A table column: header, logical type and an extractor that produces the cell value from a record
 * {@code T}. Package-private (not part of the public API) – columns are defined exclusively through
 * the fluent {@link XlsxBuilder#column} API.
 *
 * <p>Immutable value type: the optional attributes (type, format, null text, converter) are not set
 * via setters but produced as new instances through the {@code with*} copy-on-write methods. This
 * keeps the column snapshot taken at render time ({@link XlsxBuilder#renderInto}) isolated from any
 * later reconfiguration of the builder – relevant for the multi-user, multi-threaded target.
 */
final class Column<T> {

    private final String name;
    private final ColumnType type;
    private final String format;
    private final String nullText;
    private final Function<? super T, ?> extractor;
    private final Function<Object, Object> converter;

    Column(String name, ColumnType type, Function<? super T, ?> extractor) {
        this(name, type, null, null, extractor, null);
    }

    /**
     * @param format optional Excel format code (e.g. {@code "#,##0.00"}, {@code "dd.mm.yyyy"},
     *               {@code "hh:mm:ss"}); {@code null} = the type's default format.
     */
    Column(String name, ColumnType type, String format, Function<? super T, ?> extractor) {
        this(name, type, format, null, extractor, null);
    }

    private Column(
            String name,
            ColumnType type,
            String format,
            String nullText,
            Function<? super T, ?> extractor,
            Function<Object, Object> converter) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.format = format;
        this.nullText = nullText;
        this.extractor = Objects.requireNonNull(extractor, "extractor");
        this.converter = converter;
    }

    String name() {
        return name;
    }

    ColumnType type() {
        return type;
    }

    /** Optional Excel format code, or {@code null}. */
    String format() {
        return format;
    }

    /** Column-specific placeholder for {@code null} values, or {@code null} (= sheet-wide default). */
    String nullText() {
        return nullText;
    }

    /** Returns a copy with the given type (used by {@code XlsxBuilder.ofType(...)}). */
    Column<T> withType(ColumnType type) {
        return new Column<>(name, Objects.requireNonNull(type, "type"), format, nullText, extractor, converter);
    }

    /** Returns a copy with the given Excel format code (used by {@code XlsxBuilder.formatForType(...)}). */
    Column<T> withFormat(String format) {
        return new Column<>(name, type, format, nullText, extractor, converter);
    }

    /** Returns a copy with the given null placeholder (used by {@code XlsxBuilder.nullText(...)}). */
    Column<T> withNullText(String nullText) {
        return new Column<>(name, type, format, nullText, extractor, converter);
    }

    /** Returns a copy with the given value converter (used by {@code XlsxBuilder.convertToColumnType(...)}). */
    Column<T> withConverter(Function<Object, Object> converter) {
        return new Column<>(name, type, format, nullText, extractor, converter);
    }

    /**
     * Returns the cell value for the record (may be {@code null}). If a converter is set, the extracted
     * raw value (unless {@code null}) is transformed into the representation matching the target type –
     * e.g. an {@code int} into a {@link java.time.LocalTime}.
     */
    Object extract(T record) {
        Object value = extractor.apply(record);
        if (value == null || converter == null) {
            return value;
        }
        return converter.apply(value);
    }
}
