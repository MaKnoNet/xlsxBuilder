package de.makno.xlsbuilder.builder;

import java.util.List;
import java.util.Map;

/**
 * Bündelt die Layout-Parameter für das Schreiben eines Blatts (xlsx oder CSV) und hält die
 * Writer-Signaturen schlank.
 *
 * @param headerLines       optionale Titelzeilen oberhalb der Kopfzeile (oder {@code null})
 * @param footerLines       optionale Footer-Zeilen unterhalb der Daten/Summe (oder leer)
 * @param placeholders      statische {@code {key}}->Wert-Ersetzungen (inkl. {@code {date}}/{@code {datetime}})
 * @param showColumnHeaders Spaltenüberschriften-Zeile schreiben?
 * @param defaultNullText   sheet-weiter Platzhalter für {@code null}-Werte (oder {@code null})
 */
record SheetWriteOptions(
        List<String> headerLines,
        List<String> footerLines,
        Map<String, String> placeholders,
        boolean showColumnHeaders,
        String defaultNullText) {
}
