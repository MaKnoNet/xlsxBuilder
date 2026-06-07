package de.makno.xlsxbuilder.builder;

import java.util.Objects;

/** A sort stage: column name + direction. Several keys yield a multi-level sort. */
public record SortKey(String columnName, SortOrder order) {
    public SortKey {
        Objects.requireNonNull(columnName, "columnName");
        Objects.requireNonNull(order, "order");
    }
}
