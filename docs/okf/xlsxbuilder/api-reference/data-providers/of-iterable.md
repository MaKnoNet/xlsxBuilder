---
type: API Reference
title: DataProviders.ofIterable(...)
description: Methode ofIterable von DataProviders - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/DataProviders.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `static <T> DataProvider<T> ofIterable(Iterable<? extends T> iterable)`


| Parameter | Typ | null-erlaubt |
|---|---|---|
| `iterable` | `Iterable<? extends T>` | nein — `Objects.requireNonNull(iterable, "iterable")` |

Rückgabewert: `DataProvider<T>` über `iterable.iterator()`, nie `null`. Geworfene Exceptions:
`NullPointerException` bei `iterable == null`; ansonsten wie `ofIterator` (delegiert direkt
dorthin).

# Citations

[1] [DataProviders (Übersicht)](./data-providers.md)
