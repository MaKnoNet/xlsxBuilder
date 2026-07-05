---
type: API Reference
title: XlsxWriter.writeSheets(...)
description: Methode writeSheets von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private int writeSheets(Iterator<Row> rows)`


Streamt alle Datenzeilen, splittet bei konfiguriertem Zeilenlimit in Teil-Sheets.

Geworfene Exceptions:
- `RowLimitExceededException("Sheet '" + ... + "' exceeds the limit of " + ... + " rows per
  sheet (incl. title/header rows and reserved summary/footer rows). Use splitOnRowLimit(true)
  to continue on follow-up sheets.")`, wenn `rowNum >= firstReservedRowIndex` und
  `layout.splitOnRowLimit() == false` (verifiziert exakt).
- Alle Exceptions aus `startSheet()`/`writePrelude(sheet)` (siehe unten) propagieren
  unverändert.

Rückgabewert: `totalDataRows` (`int`), Anzahl aller geschriebenen Datenzeilen über alle
Teil-Sheets.

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
