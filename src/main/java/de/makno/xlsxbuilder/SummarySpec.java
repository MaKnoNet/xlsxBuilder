package de.makno.xlsxbuilder;

/**
 * Configuration of the optional summary row.
 *
 * @param sum              per column index: should this (numeric) column be summed?
 * @param labelColumnIndex column index for a label (e.g. "Total") or {@code -1} if none.
 * @param labelText        the label text (only relevant if {@code labelColumnIndex >= 0}).
 * @param useFormula       {@code true} = write as an Excel formula {@code =SUM(...)}; otherwise a
 *                         pre-computed value.
 */
record SummarySpec(boolean[] sum, int labelColumnIndex, String labelText, boolean useFormula) {

    /**
     * Defensive copy on construction: {@code sum} is a mutable array, so without this an array passed in
     * (and still referenced by the caller) could mutate this value object after the fact.
     */
    SummarySpec {
        sum = sum.clone();
    }

    /** Returns a copy so callers cannot mutate the spec's internal sum flags. */
    @Override
    public boolean[] sum() {
        return sum.clone();
    }
}
