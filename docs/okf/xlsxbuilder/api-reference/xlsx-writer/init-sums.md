---
type: API Reference
title: XlsxWriter.initSums(...)
description: Methode initSums von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static BigDecimal[] initSums(List<? extends Column<?>> columns, SummarySpec summary)`


Initialisiert die Summen-Akkumulatoren. Rückgabewert: `null`, wenn `summary == null`; sonst ein
`BigDecimal[]` der Größe `columns.size()`, wobei nur die als `sum`-markierten Indizes mit
`BigDecimal.ZERO` vorbelegt sind (übrige Einträge bleiben `null`). Keine Exceptions.

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
