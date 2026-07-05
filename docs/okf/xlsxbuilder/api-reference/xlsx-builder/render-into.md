---
type: API Reference
title: XlsxBuilder.renderInto(...)
description: Methode renderInto von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `void renderInto(SXSSFWorkbook wb) throws IOException` (paketintern)


Rendert dieses Sheet in ein bestehendes Workbook (aufgerufen vom `WorkbookBuilder`).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `wb` | `SXSSFWorkbook` | nicht geprüft in dieser Methode selbst — propagiert an `SheetRenderer.render(wb, job)`, wo ebenfalls kein Null-Check erfolgt; `null` führt zu einer späten `NullPointerException` innerhalb von `XlsxWriter` |

Rückgabewert: `void`. Geworfene Exceptions (Validierungsreihenfolge verifiziert):
1. `IllegalStateException("XlsxBuilder is single-use: already written - create a new instance
   per job (sheet: " + sheetName + ")")`, wenn `consumed == true` (bereits gerendert).
2. `IllegalStateException("At least one column must be defined")`, wenn `columns.isEmpty()`.
3. `IllegalStateException("No DataProvider set (.data(...)) for sheet: " + sheetName)`, wenn
   `dataProvider == null`.
4. Für jeden `SortKey`: `IllegalArgumentException("Unknown sort column: " +
   sortKey.columnName())`, wenn die Spalte nicht existiert; `IllegalArgumentException("Sort
   column '" + sortKey.columnName() + "' is of type " + type + " and cannot be sorted")`, wenn
   der Spaltentyp nicht sortierbar ist (`FORMULA`).
5. `buildSummarySpec()` (siehe unten) und `buildLayout()`/`validatedColumnGroups()` (siehe
   unten) werden aufgerufen und können `IllegalArgumentException` werfen.
6. **Erst danach** wird `consumed = true` gesetzt — verifizierte wichtige Design-Entscheidung
   (Kommentar im Code: "pure configuration errors must not masquerade as already written on a
   retry"): ein Konfigurationsfehler (Schritte 2–5) lässt die Instanz wiederverwendbar in einem
   neuen `WorkbookBuilder`, während ein Fehler **nach** Schritt 6 (während
   `SheetRenderer.render(...)`) die Instanz dauerhaft als verbraucht markiert.
7. Alle Exceptions aus `SheetRenderer.render(wb, job)` propagieren unverändert (u. a.
   `IOException`, `RowLimitExceededException`).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
