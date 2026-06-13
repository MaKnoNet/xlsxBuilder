package de.makno.xlsxbuilder;

import java.util.Comparator;
import java.util.List;

/**
 * Builds a {@link Comparator} over projected {@link Row}s from the {@link SortKey}s. Compares the cell
 * values by their natural ordering ({@link Comparable}), null-safe, and supports multi-level sorting as
 * well as DESC.
 *
 * <p><b>Null ordering:</b> nulls sort <em>last</em> for {@link SortOrder#ASC}. DESC negates the whole
 * comparison (including the null handling), so under {@link SortOrder#DESC} nulls sort <em>first</em> –
 * the conventional consequence of reversing a nulls-last comparator.
 */
final class RowComparator implements Comparator<Row> {

    private final int[] indices;
    private final boolean[] descending;
    private final String[] columnNames;

    RowComparator(List<? extends Column<?>> columns, List<SortKey> sortKeys) {
        if (sortKeys.isEmpty()) {
            throw new IllegalArgumentException("At least one SortKey is required");
        }
        indices = new int[sortKeys.size()];
        descending = new boolean[sortKeys.size()];
        columnNames = new String[sortKeys.size()];
        for (int i = 0; i < sortKeys.size(); i++) {
            SortKey key = sortKeys.get(i);
            int idx = indexOf(columns, key.columnName());
            if (idx < 0) {
                throw new IllegalArgumentException("Unknown sort column: " + key.columnName());
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

    private static int compareValues(Object x, Object y, String columnName) {
        if (x == null && y == null) {
            return 0;
        }
        if (x == null) {
            return 1; // nulls last (for ASC)
        }
        if (y == null) {
            return -1;
        }
        if (!(x instanceof Comparable)) {
            throw new IllegalArgumentException("Sort column '" + columnName + "' is of type "
                    + x.getClass().getSimpleName() + " and cannot be sorted (not Comparable)");
        }
        try {
            // Raw Comparable: the element type is unknown at compile time; the ClassCastException below
            // turns a genuinely incomparable pair into a clear IllegalArgumentException.
            @SuppressWarnings({"unchecked", "rawtypes"})
            int result = ((Comparable) x).compareTo(y);
            return result;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    "Sort column '" + columnName + "' contains non-comparable value types ("
                            + x.getClass().getSimpleName() + " vs. "
                            + y.getClass().getSimpleName() + ")",
                    e);
        }
    }
}
