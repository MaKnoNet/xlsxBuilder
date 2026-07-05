---
type: API Reference
title: XlsxWriter.writeFooterRows(...)
description: Methode writeFooterRows von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private void writeFooterRows(SXSSFSheet sheet, int rowNum, int totalDataRows)`


Schreibt die optionalen Fußzeilen auf das letzte Teil-Sheet. Löst die dynamischen Platzhalter
`{rowCount}` und `{sum:Column}` mit den Summen über alle Teil-Sheets auf. Kein Rückgabewert,
keine eigenen Exceptions (No-Op, wenn keine Fußzeilen konfiguriert sind).

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
