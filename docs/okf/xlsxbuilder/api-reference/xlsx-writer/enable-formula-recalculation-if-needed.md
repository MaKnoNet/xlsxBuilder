---
type: API Reference
title: XlsxWriter.enableFormulaRecalculationIfNeeded(...)
description: Methode enableFormulaRecalculationIfNeeded von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static void enableFormulaRecalculationIfNeeded(SXSSFSheet sheet, List<? extends Column<?>> columns, SummarySpec summary)`


Aktiviert `sheet.setForceFormulaRecalculation(true)`, wenn mindestens eine Spalte vom Typ
`FORMULA` ist oder die Summenzeile als Formel geschrieben wird (`summary.useFormula()`), damit
Excel die Werte beim Öffnen neu berechnet (sie sind nicht gecacht). Keine Exceptions, kein
Rückgabewert.

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
