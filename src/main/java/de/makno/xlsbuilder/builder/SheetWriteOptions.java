package de.makno.xlsbuilder.builder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Bündelt die Layout-Parameter für das Schreiben eines Blatts (xlsx) und hält die
 * Writer-Signaturen schlank.
 *
 * @param headerLines         optionale Titelzeilen oberhalb der Kopfzeile (oder {@code null})
 * @param footerLines         optionale Footer-Zeilen unterhalb der Daten/Summe (oder leer)
 * @param placeholders        statische {@code {key}}->Wert-Ersetzungen (inkl. {@code {date}}/{@code {datetime}})
 * @param placeholderResolver optionaler Fallback für lazy/berechnete Platzhalter (oder {@code null});
 *                            wird nur konsultiert, wenn {@code placeholders} den Schlüssel nicht kennt
 * @param showColumnHeaders   Spaltenüberschriften-Zeile schreiben?
 * @param defaultNullText     sheet-weiter Platzhalter für {@code null}-Werte (oder {@code null})
 */
record SheetWriteOptions(
        List<String> headerLines,
        List<String> footerLines,
        Map<String, String> placeholders,
        Function<String, String> placeholderResolver,
        boolean showColumnHeaders,
        String defaultNullText) {}
