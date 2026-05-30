package de.makno.xlsbuilder;

import java.util.Objects;

/** Eine Sortierstufe: Spaltenname + Richtung. Mehrere Keys ergeben eine mehrstufige Sortierung. */
public record SortKey(String columnName, SortOrder order) {
    public SortKey {
        Objects.requireNonNull(columnName, "columnName");
        Objects.requireNonNull(order, "order");
    }
}
