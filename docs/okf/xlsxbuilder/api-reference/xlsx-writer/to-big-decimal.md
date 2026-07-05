---
type: API Reference
title: XlsxWriter.toBigDecimal(...)
description: Methode toBigDecimal von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static BigDecimal toBigDecimal(Object value)`


Wandelt einen Zellwert für die Summierung in `BigDecimal` um.

Geworfene Exceptions: `ClassCastException`, wenn `value` kein `Number` ist (finaler Zweig `return
BigDecimal.valueOf(((Number) value).longValue())` ohne vorherige Typprüfung) — nicht
dokumentiert, aber verifiziert; praktisch nur erreichbar, wenn eine als `sum`-markierte Spalte
einen nicht-numerischen Wert liefert (durch die Validierung in `XlsxBuilder.buildSummarySpec()`
eigentlich ausgeschlossen, da nur `INTEGER/LONG/DOUBLE/DECIMAL`-Spalten summierbar markiert
werden können — aber ein individueller Zellwert könnte durch einen Converter theoretisch dennoch
einen Nicht-`Number`-Typ liefern, da `convertToColumnType` laut eigener Doku keine
Laufzeitprüfung vornimmt).

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
