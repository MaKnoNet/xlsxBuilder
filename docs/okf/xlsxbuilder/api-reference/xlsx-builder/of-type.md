---
type: API Reference
title: XlsxBuilder.ofType(...)
description: Methode ofType von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> ofType(ColumnType type)`


Setzt den Typ der zuletzt definierten Spalte.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `type` | `ColumnType` | wird an `Column.withType(type)` weitergereicht, welches selbst `Objects.requireNonNull(type, "type")` prüft — **also indirekt nicht erlaubt** |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
- `IllegalStateException("ofType()/formatForType() requires a preceding column(...)")` über die
  private Hilfsmethode `lastColumn()`, wenn noch keine Spalte definiert wurde (`columns.isEmpty()`).
- `NullPointerException`, wenn `type == null` (aus `Column.withType`, nicht aus `XlsxBuilder`
  selbst — aber für den Aufrufer beobachtbar identisch).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
