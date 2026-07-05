---
type: API Reference
title: DataProviders.ofStream(...)
description: Methode ofStream von DataProviders - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/DataProviders.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `static <T> DataProvider<T> ofStream(Stream<? extends T> stream)`


Adaptiert einen `Stream`; der Stream wird bei `DataProvider.close()` geschlossen.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `stream` | `Stream<? extends T>` | nein — `Objects.requireNonNull(stream, "stream")` |

Rückgabewert: `DataProvider<T>`, nie `null`. Geworfene Exceptions: `NullPointerException` bei
`stream == null`. `next()` wirft `NoSuchElementException`, wenn kein Element mehr vorhanden ist
(explizite Prüfung, wie bei `ofIterator`). `close()` **ist** hier überschrieben und ruft
`stream.close()` — Javadoc-Aussage "the stream is closed on close()" verifiziert korrekt.

# Citations

[1] [DataProviders (Übersicht)](./data-providers.md)
