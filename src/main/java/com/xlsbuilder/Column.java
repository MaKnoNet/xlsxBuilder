package com.xlsbuilder;

import java.util.Objects;
import java.util.function.Function;

/**
 * Eine Tabellenspalte: Überschrift, logischer Typ und ein Extraktor, der aus einem
 * Datensatz {@code T} den Zellenwert liefert.
 */
public final class Column<T> {

    private final String name;
    private final ColumnType type;
    private final Function<? super T, ?> extractor;

    public Column(String name, ColumnType type, Function<? super T, ?> extractor) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.extractor = Objects.requireNonNull(extractor, "extractor");
    }

    public String name() {
        return name;
    }

    public ColumnType type() {
        return type;
    }

    /** Liefert den Zellenwert für den Datensatz (kann {@code null} sein). */
    public Object extract(T record) {
        return extractor.apply(record);
    }
}
