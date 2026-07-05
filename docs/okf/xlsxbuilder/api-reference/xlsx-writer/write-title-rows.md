---
type: API Reference
title: XlsxWriter.writeTitleRows(...)
description: Methode writeTitleRows von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private int writeTitleRows(SXSSFSheet sheet)`


Schreibt die optionalen Titelzeilen (jede über die volle Breite verschmolzen). Rückgabewert:
nächste freie Zeile. Löst über `Placeholders.resolve(...)` potenziell eine
`NullPointerException` aus, falls `layout.placeholders()` `null` wäre — praktisch ausgeschlossen,
da `SheetWriteOptions` diese Map im kompakten Konstruktor stets nicht-`null` erzwingt.

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
