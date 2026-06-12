package de.makno.xlsxbuilder;

/**
 * Names the follow-up sheets created when a sheet is split at the Excel row limit
 * ({@link XlsxBuilder#splitOnRowLimit(boolean) splitOnRowLimit(true)}).
 *
 * <p>The namer is consulted only for follow-up sheets – the first sheet always keeps the name
 * configured via {@link XlsxBuilder#sheetName(String)} (while streaming, a split is only known once
 * the first sheet is full). Without a namer the default scheme {@code "Name (2)"}, {@code "Name (3)"},
 * ... applies.
 *
 * <p>The returned name is made Excel-safe (invalid characters replaced, at most 31 characters) but is
 * deliberately <em>not</em> deduplicated: a name that already exists in the workbook fails with an
 * {@link IllegalStateException}, so the caller stays in control of the actual names.
 */
@FunctionalInterface
public interface SplitSheetNamer {

    /**
     * Returns the name for a follow-up sheet.
     *
     * @param baseSheetName the sheet name configured via {@link XlsxBuilder#sheetName(String)}
     * @param partNumber    number of the part sheet; {@code 2} for the first follow-up sheet (the
     *                      base sheet is part 1), matching the default suffix {@code " (2)"}
     * @return the sheet name (must not be {@code null} or blank, and must be unique in the workbook)
     */
    String partSheetName(String baseSheetName, int partNumber);
}
