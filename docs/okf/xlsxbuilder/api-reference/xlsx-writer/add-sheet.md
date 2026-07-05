---
type: API Reference
title: XlsxWriter.addSheet(...)
description: Methode addSheet von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `static int addSheet(SXSSFWorkbook wb, String sheetName, List<? extends Column<?>> columns, Iterator<Row> rows, SummarySpec summary, SheetWriteOptions layout)`


Fügt ein Arbeitsblatt zu einem bestehenden Workbook hinzu. Am Zeilenlimit schlägt das Sheet
entweder fehl oder splittet in Teil-Sheets.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `wb` | `SXSSFWorkbook` | siehe Konstruktor — praktisch nicht erlaubt |
| `sheetName` | `String` | faktisch erlaubt (Fallback `"Sheet1"`, siehe oben) |
| `columns` | `List<? extends Column<?>>` | praktisch nicht erlaubt |
| `rows` | `Iterator<Row>` | nicht geprüft — `rows.hasNext()` wird in `writeSheets` aufgerufen; `null` löst `NullPointerException` aus |
| `summary` | `SummarySpec` | **ja** |
| `layout` | `SheetWriteOptions` | praktisch nicht erlaubt |

Rückgabewert: Anzahl der über alle Teil-Sheets hinweg geschriebenen Datenzeilen (ohne
Titel-/Kopf-/Summenzeilen), primitiv `int`, nie `null` — für Performance-Logs.

Geworfene Exceptions: siehe `writeSheets` (delegiert vollständig dorthin nach Konstruktion).

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
