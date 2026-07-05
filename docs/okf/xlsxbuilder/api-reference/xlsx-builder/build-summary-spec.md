---
type: API Reference
title: XlsxBuilder.buildSummarySpec(...)
description: Methode buildSummarySpec von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private SummarySpec buildSummarySpec()`


Baut die Summenzeilen-Konfiguration, oder `null`, wenn weder `sumColumn(...)` noch
`summaryLabel(...)` aufgerufen wurden.

Geworfene Exceptions:
- `IllegalArgumentException("Unknown sum column: " + name)`, wenn eine per `sumColumn(...)`
  benannte Spalte nicht existiert.
- `IllegalArgumentException("Sum column is not numeric: " + name)`, wenn die Spalte nicht vom
  Typ `INTEGER`/`LONG`/`DOUBLE`/`DECIMAL` ist (verifiziert gegen `isNumeric(ColumnType)`).
- `IllegalArgumentException("Unknown label column: " + summaryLabelColumn)`, wenn die per
  `summaryLabel(...)` benannte Spalte nicht existiert.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
