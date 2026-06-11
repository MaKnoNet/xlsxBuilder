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
        int maxRowsPerSheet) {}
