package com.xlsbuilder;

/**
 * Konfiguration der optionalen Summenzeile.
 *
 * @param sum              je Spaltenindex: soll diese (numerische) Spalte summiert werden?
 * @param labelColumnIndex Spaltenindex für ein Label (z. B. "Summe") oder {@code -1}, wenn keines.
 * @param labelText        der Label-Text (nur relevant, wenn {@code labelColumnIndex >= 0}).
 */
record SummarySpec(boolean[] sum, int labelColumnIndex, String labelText) {
}
