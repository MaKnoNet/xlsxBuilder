package de.makno.xlsbuilder;

/**
 * Logischer Typ einer Spalte. Steuert, wie der projizierte Wert als Excel-Zelle
 * geschrieben wird (Zelltyp/Format) und wie er beim Sortieren verglichen wird.
 */
public enum ColumnType {
    STRING,
    INTEGER,
    LONG,
    DOUBLE,
    DECIMAL,
    BOOLEAN,
    DATE,
    DATETIME
}
