---
type: API Reference
title: XlsxBuilder.validatedColumnGroups(...)
description: Methode validatedColumnGroups von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private List<ColumnGroup> validatedColumnGroups()`


Prüft, dass die Summe aller `ColumnGroup.span()`-Werte exakt der Spaltenanzahl entspricht.

Geworfene Exceptions: `IllegalArgumentException("Column groups span " + total + " columns but
there are " + columns.size())`, wenn die Summe nicht passt. Leere `columnGroups` liefern
`List.of()` ohne Prüfung (kein Fehler bei fehlenden Gruppen).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
