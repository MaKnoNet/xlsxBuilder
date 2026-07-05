---
type: API Reference
title: XlsxWriter.writeColumnGroups(...)
description: Methode writeColumnGroups von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private int writeColumnGroups(SXSSFSheet sheet, int rowNum)`


Schreibt die optionale gruppierte Kopfzeile. Rückgabewert: unveränderter `rowNum`, wenn keine
Gruppen konfiguriert sind, sonst `rowNum + 1`. Keine eigenen Exceptions (die Validierung "Spans
decken alle Spalten ab" ist bereits vorgelagert in `XlsxBuilder.validatedColumnGroups()`
erfolgt).

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
