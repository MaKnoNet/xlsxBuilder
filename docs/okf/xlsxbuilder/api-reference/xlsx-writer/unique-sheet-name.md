---
type: API Reference
title: XlsxWriter.uniqueSheetName(...)
description: Methode uniqueSheetName von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static String uniqueSheetName(SXSSFWorkbook wb, String sheetName)`


Erzeugt einen im Workbook eindeutigen, gültigen Sheet-Namen. `sheetName == null` oder
`isBlank()` fällt auf `"Sheet1"` zurück (verifiziert). Bei Namenskollision wird ein Suffix
`" (n)"` angehängt, ggf. wird der Basisname gekürzt, um die Excel-Obergrenze von 31 Zeichen
einzuhalten. Keine Exceptions (endlose Schleife theoretisch möglich, aber durch
`wb.getSheet(unique) != null`-Abbruchbedingung und monoton wachsendes `n` praktisch begrenzt
durch die tatsächliche Anzahl bereits existierender kollidierender Namen).

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
