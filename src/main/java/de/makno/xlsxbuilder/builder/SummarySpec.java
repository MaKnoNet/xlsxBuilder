package de.makno.xlsxbuilder.builder;

/**
 * Configuration of the optional summary row.
 *
 * @param sum              per column index: should this (numeric) column be summed?
 * @param labelColumnIndex column index for a label (e.g. "Summe") or {@code -1} if none.
 * @param labelText        the label text (only relevant if {@code labelColumnIndex >= 0}).
 * @param useFormula       {@code true} = write as an Excel formula {@code =SUM(...)}; otherwise a
 *                         pre-computed value.
 */
record SummarySpec(boolean[] sum, int labelColumnIndex, String labelText, boolean useFormula) {}
