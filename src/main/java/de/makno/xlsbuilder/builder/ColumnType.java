package de.makno.xlsbuilder.builder;

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
    DATETIME,
    /** Uhrzeit (Tageszeit ohne Datum), erwartet {@link java.time.LocalTime} oder {@link java.time.LocalDateTime}. */
    TIME,
    /**
     * Excel-Formel. Der Extraktor liefert den Formeltext ohne führendes {@code =} (z. B.
     * {@code "F{row}*0.1"}); der Platzhalter {@code {row}} wird durch die tatsächliche Zeilennummer
     * ersetzt. Excel berechnet die Werte beim Öffnen.
     */
    FORMULA
}
