package de.makno.xlsbuilder.builder;

/**
 * Logischer Typ einer Spalte. Steuert, wie der projizierte Wert als Excel-Zelle
 * geschrieben wird (Zelltyp/Format) und wie er beim Sortieren verglichen wird.
 */
public enum ColumnType {
    STRING(true),
    INTEGER(true),
    LONG(true),
    DOUBLE(true),
    DECIMAL(true),
    BOOLEAN(true),
    DATE(true),
    DATETIME(true),
    /** Uhrzeit (Tageszeit ohne Datum), erwartet {@link java.time.LocalTime} oder {@link java.time.LocalDateTime}. */
    TIME(true),
    /**
     * Excel-Formel. Der Extraktor liefert den Formeltext ohne führendes {@code =} (z. B.
     * {@code "F{row}*0.1"}); der Platzhalter {@code {row}} wird durch die tatsächliche Zeilennummer
     * ersetzt. Excel berechnet die Werte beim Öffnen. Formeln sind nicht sortierbar.
     */
    FORMULA(false);

    /** Gibt an, ob dieser Spaltentyp sortierbar ist. Formeln sind z.B. nicht sortierbar. */
    private final boolean sortable;

    ColumnType(boolean sortable) {
        this.sortable = sortable;
    }

    /**
     * Gibt an, ob dieser Spaltentyp sortierbar ist.
     *
     * @return {@code true} wenn der Typ sortiert werden kann, {@code false} sonst
     */
    public boolean isSortable() {
        return sortable;
    }
}
