---
type: API Reference
title: XlsxBuilder.sortBy(...)
description: Methode sortBy von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> sortBy(String columnName, SortOrder order)`


Optionale Sortierstufe. Wiederholter Aufruf ergibt eine mehrstufige Sortierung.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `columnName` | `String` | **an dieser Stelle nicht geprüft** — `sortKeys.add(new SortKey(columnName, order))`; `SortKey`s kompakter Konstruktor prüft selbst `Objects.requireNonNull(columnName, "columnName")`, sodass `NullPointerException` indirekt entsteht |
| `order` | `SortOrder` | analog — indirekt über `SortKey`s `Objects.requireNonNull(order, "order")` nicht erlaubt |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException` (indirekt über
den `SortKey`-Konstruktor), wenn `columnName` oder `order` `null` ist. **Keine** Prüfung an
dieser Stelle, ob `columnName` eine tatsächlich existierende oder sortierbare Spalte bezeichnet
— das geschieht erst in `renderInto()` (`IllegalArgumentException("Unknown sort column: ...")`
bzw. "... cannot be sorted").

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
