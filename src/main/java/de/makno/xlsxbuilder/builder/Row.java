package de.makno.xlsxbuilder.builder;

import java.io.Serializable;

/**
 * A projected data row: the already-extracted cell values, one per column. Serializable so that the
 * {@link ExternalMergeSort} can spill whole runs to temp files. The contained value types (String,
 * Long, Double, BigDecimal, Boolean, LocalDate/-Time, ...) are themselves {@link Serializable}; the
 * original data type {@code T} need not be.
 */
public final class Row implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Object[] values;

    public Row(Object[] values) {
        this.values = values;
    }

    public Object get(int index) {
        return values[index];
    }

    public int size() {
        return values.length;
    }
}
