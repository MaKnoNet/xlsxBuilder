package de.makno.xlsxbuilder.builder;

import java.util.Comparator;
import java.util.List;

/**
 * Baut aus den {@link SortKey}s einen {@link Comparator} über projizierte {@link Row}s.
 * Vergleicht die Zellenwerte über deren natürliche Ordnung ({@link Comparable}), null-sicher
 * (nulls last bei ASC), und unterstützt mehrstufige Sortierung sowie DESC.
 */
final class RowComparator implements Comparator<Row> {

    private final int[] indices;
    private final boolean[] descending;
    private final String[] columnNames;

    RowComparator(List<? extends Column<?>> columns, List<SortKey> sortKeys) {
        if (sortKeys.isEmpty()) {
            throw new IllegalArgumentException("Mindestens ein SortKey erforderlich");
        }
        indices = new int[sortKeys.size()];
        descending = new boolean[sortKeys.size()];
        columnNames = new String[sortKeys.size()];
        for (int i = 0; i < sortKeys.size(); i++) {
            SortKey key = sortKeys.get(i);
            int idx = indexOf(columns, key.columnName());
            if (idx < 0) {
                throw new IllegalArgumentException("Unbekannte Sortierspalte: " + key.columnName());
            }
            indices[i] = idx;
            descending[i] = key.order() == SortOrder.DESC;
            columnNames[i] = key.columnName();
        }
    }

    private static int indexOf(List<? extends Column<?>> columns, String name) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).name().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int compare(Row a, Row b) {
        for (int i = 0; i < indices.length; i++) {
            int idx = indices[i];
            int c = compareValues(a.get(idx), b.get(idx), columnNames[i]);
            if (c != 0) {
                return descending[i] ? -c : c;
            }
        }
        return 0;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int compareValues(Object x, Object y, String columnName) {
        if (x == null && y == null) {
            return 0;
        }
        if (x == null) {
            return 1; // nulls last (bei ASC)
        }
        if (y == null) {
            return -1;
        }
        if (!(x instanceof Comparable)) {
            throw new IllegalArgumentException("Sortierspalte '" + columnName + "' ist vom Typ "
                    + x.getClass().getSimpleName() + " und kann nicht sortiert werden (nicht Comparable)");
        }
        try {
            return ((Comparable) x).compareTo(y);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Sortierspalte '" + columnName + "' enthält nicht vergleichbare Werttypen ("
                            + x.getClass().getSimpleName() + " vs. "
                            + y.getClass().getSimpleName() + ")",
                    e);
        }
    }
}
