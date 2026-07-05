---
type: API Reference
title: XlsxBuilder.column(...)
description: Methode column von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> column(String name, Function<? super T, ?> extractor)`


Definiert eine Spalte, Default-Typ `STRING`. Excel erlaubt maximal 16.384 Spalten pro Sheet
(`A..XFD`); mehr definierte Spalten schlagen sofort fehl (fail-fast zur Konfigurationszeit,
bevor Daten gelesen/sortiert werden) — verifiziert.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `name` | `String` | **nein** — `Objects.requireNonNull(name, "name")` |
| `extractor` | `Function<? super T, ?>` | **nein** — `Objects.requireNonNull(extractor, "extractor")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
- `NullPointerException`, wenn `name` oder `extractor` `null` ist.
- `IllegalStateException("Column '" + name + "' exceeds Excel's limit of 16384 columns per
  sheet")`, wenn bereits `columns.size() >= SpreadsheetVersion.EXCEL2007.getMaxColumns()`
  Spalten definiert sind (verifiziert exakt).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
