---
type: API Reference
title: XlsxWriter.summaryValue(...)
description: Methode summaryValue von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static Object summaryValue(ColumnType type, BigDecimal sum)`


Wandelt eine akkumulierte Summe in den zum Spaltentyp passenden Java-Typ für `writeCell`/die
Breitenschätzung um.

Geworfene Exceptions: `ArithmeticException` (aus `BigDecimal.longValueExact()`), wenn `type`
`INTEGER`/`LONG` ist und die Summe nicht exakt in einen `long` passt — laut Kommentar bewusst
("only fails on a true long overflow — honestly, instead of truncating silently").

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
