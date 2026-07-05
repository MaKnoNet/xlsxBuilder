---
type: API Reference
title: XlsxBuilder.closeUnconsumedProvider(...)
description: Methode closeUnconsumedProvider von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `void closeUnconsumedProvider()` (paketintern)


Schließt die Datenquelle dieses Sheets, falls sie nie konsumiert wurde. Aufgerufen vom
`WorkbookBuilder` im Fehlerpfad, damit Provider von Sheets, die wegen eines früheren Fehlers nie
gerendert (und damit nie vom `SheetRenderer` geschlossen) wurden, nicht lecken.

Keine Parameter. Rückgabewert: `void`. No-Op, wenn `consumed == true` oder
`dataProvider == null`. Setzt **bewusst nicht** `consumed` (ein reiner Konfigurationsfehler soll
das Sheet in einem frischen `WorkbookBuilder` wiederverwendbar lassen). Geworfene Exceptions:
**keine** — ein `RuntimeException` aus `dataProvider.close()` wird gefangen und verschluckt
(`catch (RuntimeException ignored)`), best-effort, um eine bereits in Flug befindliche primäre
Exception nicht zu überdecken.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
