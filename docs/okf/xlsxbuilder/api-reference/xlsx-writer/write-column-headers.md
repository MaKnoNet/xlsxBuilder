---
type: API Reference
title: XlsxWriter.writeColumnHeaders(...)
description: Methode writeColumnHeaders von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private int writeColumnHeaders(SXSSFSheet sheet, int rowNum)`


Schreibt die Spaltenköpfe, falls `layout.showColumnHeaders()`. Rückgabewert: unveränderter
`rowNum`, wenn deaktiviert, sonst `rowNum + 1`. Keine Exceptions.

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
