package de.makno.xlsbuilder;

import java.util.Objects;
import java.util.function.Function;

/**
 * Eine Tabellenspalte: Überschrift, logischer Typ und ein Extraktor, der aus einem
 * Datensatz {@code T} den Zellenwert liefert.
 */
public final class Column<T> {

    private final String name;
    private final ColumnType type;
    private final String format;
    private final Function<? super T, ?> extractor;

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

    /** Liefert den Zellenwert für den Datensatz (kann {@code null} sein). */
    public Object extract(T record) {
        return extractor.apply(record);
    }
}
