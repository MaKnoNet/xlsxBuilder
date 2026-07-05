---
type: API Reference
title: DataProviders.ofIterator(...)
description: Methode ofIterator von DataProviders - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/DataProviders.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `static <T> DataProvider<T> ofIterator(Iterator<? extends T> iterator)`


| Parameter | Typ | null-erlaubt |
|---|---|---|
| `iterator` | `Iterator<? extends T>` | nein — `Objects.requireNonNull(iterator, "iterator")` |

Rückgabewert: neuer `DataProvider<T>`, delegiert `hasNext()`/`next()` an den übergebenen
Iterator; nie `null`. Geworfene Exceptions: `NullPointerException`, wenn `iterator == null`.
Die zurückgegebene `next()`-Implementierung wirft zusätzlich `NoSuchElementException`, wenn
`hasNext()` zum Aufrufzeitpunkt `false` ist (explizite Prüfung im anonymen `DataProvider`, nicht
nur Weiterreichung des zugrunde liegenden Iterator-Verhaltens). `close()` ist nicht
überschrieben — nutzt die Default-No-Op-Implementierung von `DataProvider`.

# Citations

[1] [DataProviders (Übersicht)](./data-providers.md)
