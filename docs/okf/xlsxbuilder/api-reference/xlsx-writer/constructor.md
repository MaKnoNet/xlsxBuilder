---
type: API Reference
title: XlsxWriter – Konstruktoren
description: Alle Konstruktoren von XlsxWriter.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## `private XlsxWriter(SXSSFWorkbook wb, String sheetName, List<? extends Column<?>> columns, SummarySpec summary, SheetWriteOptions layout)`

Privater Konstruktor, nur über die statische Fabrikmethode `addSheet(...)` erreichbar.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `wb` | `SXSSFWorkbook` | nicht geprüft — wird sofort für `wb.getCreationHelper()` und `wb.createCellStyle()` verwendet; `null` löst sofort `NullPointerException` aus |
| `sheetName` | `String` | nicht geprüft im Konstruktor selbst; erst später in `uniqueSheetName(wb, sheetName)` behandelt `sheetName == null` explizit (`(sheetName == null \|\| sheetName.isBlank()) ? "Sheet1" : sheetName`) — also faktisch **erlaubt**, mit Fallback auf `"Sheet1"` |
| `columns` | `List<? extends Column<?>>` | nicht geprüft — `columns.size()` wird sofort in `buildColumnStyles`/`ColumnWidthEstimator` aufgerufen; `null` löst `NullPointerException` aus |
| `summary` | `SummarySpec` | **ja** — `null` bedeutet "keine Summenzeile" (explizit abgefragt in mehreren Methoden) |
| `layout` | `SheetWriteOptions` | nicht geprüft — `layout.showColumnHeaders()`/`layout.footerLines()` etc. werden sofort aufgerufen; `null` löst `NullPointerException` aus |

Initialisiert Spalten-Styles (einmal pro Spalte), Titel-/Fußzeilen-Styles, den
`ColumnWidthEstimator`, die Summen-Akkumulatoren sowie `firstReservedRowIndex` (0-basierter
Index der ersten für Summenzeile/Fußzeilen reservierten Zeile: `maxRowsPerSheet - trailerRows`).
Keine expliziten Exceptions außer den oben genannten impliziten `NullPointerException`n.

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
