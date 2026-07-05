---
type: API Reference
title: XlsxWriter.writeDataRow(...)
description: Methode writeDataRow von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private void writeDataRow(SXSSFSheet sheet, Row dataRow, int rowNum)`


Schreibt eine Datenzeile, misst Spaltenbreiten und akkumuliert die Summen. Bei `null`-Zellwert:
spaltenspezifischer Platzhalter vor dem sheet-weiten Default; ohne Platzhalter wird explizit eine
leere Zelle vom Typ `CellType.BLANK` erzeugt (kein Überspringen). Keine eigenen Exceptions außer
denen, die aus `writeCell(...)` propagieren (siehe unten, z. B. bei nicht unterstützten
Datum-/Zeit-Typen).

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
