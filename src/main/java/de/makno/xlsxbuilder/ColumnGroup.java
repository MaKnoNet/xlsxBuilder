package de.makno.xlsxbuilder;

import java.util.Objects;

/**
 * One cell of the optional grouped header row above the column headers: a {@code label} spanning
 * {@code span} consecutive columns (the cell is merged when {@code span > 1}). Used to render
 * multi-row / joined headers in the {@code .xlsx}; see {@link XlsxBuilder#columnGroups(java.util.List)}.
 *
 * @param label group caption (may be empty for an ungrouped column, but never {@code null})
 * @param span  number of consecutive columns the cell spans (must be {@code >= 1})
 */
public record ColumnGroup(String label, int span) {

    public ColumnGroup {
        Objects.requireNonNull(label, "label");
        if (span < 1) {
            throw new IllegalArgumentException("span must be >= 1: " + span);
        }
    }
}
