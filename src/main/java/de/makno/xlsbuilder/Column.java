package de.makno.xlsbuilder;

import java.util.Objects;
import java.util.function.Function;

/**
 * Eine Tabellenspalte: Überschrift, logischer Typ und ein Extraktor, der aus einem
 * Datensatz {@code T} den Zellenwert liefert.
 */
public final class Column<T> {

    private final String name;
    private ColumnType type;
    private String format;
    private final Function<? super T, ?> extractor;
    private Function<Object, Object> converter;

    public Column(String name, ColumnType type, Function<? super T, ?> extractor) {
        this(name, type, null, extractor);
    }

    /**
     * @param format optionaler Excel-Format-Code (z. B. {@code "#,##0.00"}, {@code "dd.mm.yyyy"},
     *               {@code "hh:mm:ss"}); {@code null} = Standardformat des Typs.
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

    /** Optionaler Excel-Format-Code oder {@code null}. */
    public String format() {
        return format;
    }

    /** Paket-intern: Typ der Spalte setzen (vom {@code ExcelBuilder.ofType(...)} genutzt). */
    void setType(ColumnType type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    /** Paket-intern: Format-Code der Spalte setzen (vom {@code ExcelBuilder.formatForType(...)} genutzt). */
    void setFormat(String format) {
        this.format = format;
    }

    /** Paket-intern: optionalen Wert-Konverter setzen (vom {@code ExcelBuilder.convertToColumnType(...)}). */
    void setConverter(Function<Object, Object> converter) {
        this.converter = converter;
    }

    /**
     * Liefert den Zellenwert für den Datensatz (kann {@code null} sein). Ist ein Konverter gesetzt,
     * wird der extrahierte Rohwert (sofern nicht {@code null}) in die zum Zieltyp passende
     * Repräsentation umgewandelt – z. B. ein {@code int} in eine {@link java.time.LocalTime}.
     */
    public Object extract(T record) {
        Object value = extractor.apply(record);
        if (value == null || converter == null) {
            return value;
        }
        return converter.apply(value);
    }
}
