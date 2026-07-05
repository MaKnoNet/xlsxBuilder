---
type: API Reference
title: XlsxWriter.writeSummaryRow(...)
description: Methode writeSummaryRow von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private int writeSummaryRow(SXSSFSheet sheet, int rowNum, int totalDataRows)`


Schreibt die optionale Summenzeile (vorberechneter Wert oder echte `=SUM(...)`-Formel) auf das
letzte Teil-Sheet; die Summen decken die Datenzeilen **aller** Teil-Sheets ab.

Rückgabewert: unveränderter `rowNum`, wenn `summary == null`, sonst `rowNum + 1`.

Bemerkenswert (verifiziert): `asFormula = summary.useFormula() && totalDataRows > 0` — bei
**null Datenzeilen** wird selbst bei `useFormula(true)` kein `=SUM(...)` geschrieben, sondern der
vorberechnete Wert (der dann `0`/`BigDecimal.ZERO` ist) — eine Feinheit, die weder im Javadoc von
`XlsxBuilder.summaryAsFormula` noch in der Komponenten-Doku explizit erwähnt wird, aber
verhindert, dass eine Formel über einen leeren/nicht existierenden Bereich geschrieben wird.

Keine eigenen Exceptions (die Formel-Erzeugung selbst kann bei extremen Spalten-Indizes
theoretisch scheitern, aber `CellReference.convertNumToColString` ist für die durch
`XlsxBuilder` bereits auf 16.384 begrenzte Spaltenanzahl unproblematisch).

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
