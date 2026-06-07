package de.makno.xlsbuilder.builder;

import java.util.List;
import java.util.function.Predicate;

/**
 * Unveränderlicher Ausführungsauftrag für ein einzelnes Blatt: alle vom {@link ExcelBuilder}
 * kompilierten Bausteine, die der {@link SheetRenderer} zum Schreiben benötigt. Trennt die fluente
 * Konfiguration ({@link ExcelBuilder}) von der Ausführung ({@link SheetRenderer}).
 *
 * @param sheetName    Blattname
 * @param columns      Spaltendefinitionen (Extraktor/Typ/Format/Null-Text)
 * @param filter       optionaler Datensatzfilter auf dem Rohobjekt (oder {@code null} = alle)
 * @param dataProvider forward-only Datenquelle (wird vom Renderer geschlossen)
 * @param sort         Sortier-Konfiguration (leere {@code sortKeys} = unsortiert)
 * @param summary      Summenzeilen-Konfiguration (oder {@code null})
 * @param layout       Titel-/Footer-/Platzhalter-/Null-Layout
 * @param parallel     Producer/Consumer-Pipeline aktivieren?
 * @param <T>          Typ der Datensätze der Datenquelle
 */
record RenderJob<T>(
        String sheetName,
        List<Column<T>> columns,
        Predicate<? super T> filter,
        DataProvider<T> dataProvider,
        SortSpec sort,
        SummarySpec summary,
        SheetWriteOptions layout,
        boolean parallel) {}
