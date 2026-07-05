---
type: API Reference
title: XlsxWriter.startSheet(...)
description: Methode startSheet von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private SXSSFSheet startSheet()`


Startet ein (Teil-)Sheet: eindeutiger Name, Neuberechnungs-Flag, Registrierung für den
abschließenden Breiten-Pass.

Rückgabewert: `SXSSFSheet`, nie `null` (POI's `wb.createSheet(...)` liefert laut POI-Vertrag nie
`null`). Geworfene Exceptions: propagiert aus `partSheetName(partNumber)` (siehe unten) und aus
POI's `wb.createSheet(...)` (z. B. `IllegalArgumentException` bei einem ungültigen/doppelten
Namen — praktisch ausgeschlossen, da `partSheetName` bereits Eindeutigkeit sicherstellt).

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
