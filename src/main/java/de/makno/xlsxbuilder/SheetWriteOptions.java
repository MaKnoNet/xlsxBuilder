package de.makno.xlsxbuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Bundles the layout parameters for writing a sheet (xlsx) and keeps the writer signatures lean.
 *
 * @param headerLines         optional title rows above the header row (or {@code null})
 * @param footerLines         optional footer rows below the data/summary (or empty)
 * @param columnGroups        optional grouped header row above the column headers (or empty)
 * @param placeholders        static {@code {key}}->value replacements (incl. {@code {date}}/{@code {datetime}})
 * @param placeholderResolver optional fallback for lazy/computed placeholders (or {@code null});
 *                            consulted only when {@code placeholders} does not know the key
 * @param showColumnHeaders   write the column-header row?
 * @param defaultNullText     sheet-wide placeholder for {@code null} values (or {@code null})
 * @param splitOnRowLimit     {@code true} = continue on follow-up sheets when the row limit is
 *                            reached; {@code false} = throw a {@link RowLimitExceededException}
 * @param splitSheetNamer     optional naming of the follow-up sheets created by a split (or
 *                            {@code null} = default scheme {@code "Name (2)"}, {@code "Name (3)"}, ...)
 * @param maxRowsPerSheet     maximum number of rows per sheet (Excel: 1,048,576; lowered only by tests)
 */
record SheetWriteOptions(
        List<String> headerLines,
        List<String> footerLines,
        List<ColumnGroup> columnGroups,
        Map<String, String> placeholders,
        Function<String, String> placeholderResolver,
        boolean showColumnHeaders,
        String defaultNullText,
        boolean splitOnRowLimit,
        SplitSheetNamer splitSheetNamer,
        int maxRowsPerSheet) {

    /**
     * Defensive, unmodifiable copies of the collection components so the options are a true immutable
     * value type: a caller that still holds the passed-in collections cannot mutate the layout, and the
     * accessors hand out views that cannot be modified. {@code headerLines} stays {@code null} when
     * absent (it is optional); {@code footerLines}, {@code columnGroups} and {@code placeholders} are
     * required to be non-null per the contract.
     */
    SheetWriteOptions {
        headerLines = headerLines == null ? null : List.copyOf(headerLines);
        footerLines = List.copyOf(footerLines);
        columnGroups = List.copyOf(columnGroups);
        placeholders = Map.copyOf(placeholders);
    }
}
