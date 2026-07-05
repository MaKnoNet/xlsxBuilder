---
type: API Reference
title: XlsxBuilder.sumColumn(...)
description: Methode sumColumn von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> sumColumn(String columnName)`


Markiert eine numerische Spalte zur Summierung; aktiviert die optionale Summenzeile.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `columnName` | `String` | **nein** — `Objects.requireNonNull(columnName, "columnName")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException` bei
`columnName == null`. Keine Prüfung an dieser Stelle, ob die Spalte existiert/numerisch ist —
das geschieht erst in `buildSummarySpec()` während `renderInto()`.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
