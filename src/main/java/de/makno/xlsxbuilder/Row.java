package de.makno.xlsxbuilder;

import java.io.Serializable;

/**
 * A projected data row: the already-extracted cell values, one per column. Serializable so that the
 * {@link ExternalMergeSort} can spill whole runs to temp files. The contained value types (String,
 * Long, Double, BigDecimal, Boolean, LocalDate/-Time, ...) are themselves {@link Serializable}; the
 * original data type {@code T} need not be.
 *
 * <p>Package-private (not part of the public API). The value array is deliberately not copied – rows
 * are created internally once per record on the hot path and never shared.
 */
final class Row implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Object[] values;

    Row(Object[] values) {
        this.values = values;
    }

    Object get(int index) {
        return values[index];
    }

    int size() {
        return values.length;
    }
}
