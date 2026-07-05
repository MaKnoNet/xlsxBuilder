---
type: API Reference
title: CloseableIterator
description: Iterator, das Ressourcen (z. B. offene Run-Dateien) hält und ohne geprüfte Exception schließbar ist.
resource: src/main/java/de/makno/xlsxbuilder/CloseableIterator.java
tags: [api-reference, interface, iterator, closeable]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`CloseableIterator<T>` ist ein öffentliches Marker-Interface: `Iterator<T> & Closeable`, wobei
`close()` **ohne** geprüfte `IOException` deklariert ist (Override von `Closeable#close()` ohne
`throws`). Damit lassen sich Ressourcen-haltige Iteratoren (offene Run-Dateien beim k-way Merge,
siehe [ExternalMergeSort](/api-reference/external-merge-sort.md)) bequem in
try-with-resources verwenden, ohne dass Aufrufer eine geprüfte Exception behandeln müssen.
Konkrete Implementierungen: `ExternalMergeSort.MergeIterator`,
[PrefetchingRowIterator](/api-reference/prefetching-row-iterator.md).

Kein eigener Thread-Safety-Vertrag dokumentiert; wie die meisten Iteratoren dieser Bibliothek
ist von Single-Thread-, Single-Use-Gebrauch auszugehen (ein Durchlauf, ein Thread).

# Methoden

## `void close()`

Überschreibt `Closeable#close()` **kovariant ohne** `throws IOException`. Parameter: keine.
Rückgabewert: `void`. Geworfene Exceptions: laut Signatur keine geprüfte Exception; ob eine
konkrete Implementierung dennoch eine ungeprüfte Exception werfen kann, hängt von der jeweiligen
Implementierung ab (in dieser Bibliothek: nein — alle bekannten Implementierungen schlucken
I/O-Fehler beim Schließen best-effort).

Da das Interface keine weiteren Methoden deklariert (`hasNext()`/`next()` kommen von
`Iterator<T>`), erschöpft sich die Dokumentation an dieser Stelle in diesem einen Vertrag.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/CloseableIterator.java`
