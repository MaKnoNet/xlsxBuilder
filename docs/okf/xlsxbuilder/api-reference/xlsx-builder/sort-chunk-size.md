---
type: API Reference
title: XlsxBuilder.sortChunkSize(...)
description: Methode sortChunkSize von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> sortChunkSize(int chunkSize)`


Chunk-Größe (Zeilen pro sortiertem Lauf im Speicher) der External Merge Sort.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `chunkSize` | `int` | primitiv; muss `>= 1` sein |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
`IllegalArgumentException("chunkSize must be >= 1")`, wenn `chunkSize < 1`.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
