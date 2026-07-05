---
type: API Reference
title: Column.withType(...)
description: Methode withType von Column - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/Column.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `Column<T> withType(ColumnType type)`


| Parameter | Typ | null-erlaubt |
|---|---|---|
| `type` | `ColumnType` | nein — `Objects.requireNonNull(type, "type")` |

Rückgabewert: neue `Column<T>`-Instanz mit geändertem Typ, nie `null`. Geworfene Exceptions:
`NullPointerException` bei `type == null`.

# Citations

[1] [Column (Übersicht)](./column.md)
