---
type: API Reference
title: CloseableIterator.close(...)
description: Methode close von CloseableIterator - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/CloseableIterator.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `void close()`


Überschreibt `Closeable#close()` **kovariant ohne** `throws IOException`. Parameter: keine.
Rückgabewert: `void`. Geworfene Exceptions: laut Signatur keine geprüfte Exception; ob eine
konkrete Implementierung dennoch eine ungeprüfte Exception werfen kann, hängt von der jeweiligen
Implementierung ab (in dieser Bibliothek: nein — alle bekannten Implementierungen schlucken
I/O-Fehler beim Schließen best-effort).

Da das Interface keine weiteren Methoden deklariert (`hasNext()`/`next()` kommen von
`Iterator<T>`), erschöpft sich die Dokumentation an dieser Stelle in diesem einen Vertrag.

# Citations

[1] [CloseableIterator (Übersicht)](./closeable-iterator.md)
