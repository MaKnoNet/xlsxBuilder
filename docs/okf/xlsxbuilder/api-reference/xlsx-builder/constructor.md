---
type: API Reference
title: XlsxBuilder – Konstruktor
description: Einziger (privater) Konstruktor von XlsxBuilder; Instanzen entstehen nur ueber die statische Fabrikmethode create().
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, builder, excel, sheet, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private XlsxBuilder()`

Leerer privater Konstruktor — Instanzen werden ausschließlich über die statische Fabrikmethode
`create()` erzeugt. Keine Parameter, keine Exceptions. Alle Felder werden mit Defaults
initialisiert, u. a. `sheetName = "Sheet1"`, `sortChunkSize = 100_000`,
`maxRowsPerSheet = SpreadsheetVersion.EXCEL2007.getMaxRows()` (1.048.576), `showColumnHeaders =
true`, `parallel = false`, `splitOnRowLimit = false`. Vollständige Feldliste mit Bedeutung:
[XlsxBuilder – Felder](./xlsx-builder.md#felder).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
