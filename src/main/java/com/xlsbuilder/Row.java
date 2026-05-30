package com.xlsbuilder;

import java.io.Serializable;

/**
 * Eine projizierte Datenzeile: die bereits extrahierten Zellenwerte gemäß der Spalten.
 * Serialisierbar, damit der {@link ExternalMergeSort} ganze Runs auf Temp-Dateien auslagern kann.
 * Die enthaltenen Werttypen (String, Long, Double, BigDecimal, Boolean, LocalDate/-Time, ...)
 * sind ihrerseits {@link Serializable}; der ursprüngliche Datentyp {@code T} muss es nicht sein.
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
