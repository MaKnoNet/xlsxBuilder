---
type: API Reference
title: XlsxBuilder.parallel(...)
description: Methode parallel von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> parallel(boolean enabled)`


Aktiviert die optionale Pipeline-Parallelität für dieses Sheet (Hintergrundthread liest/sortiert,
aufrufender Thread schreibt). Default `false`. Ergebnis ist **identisch** zum sequenziellen
Modus; Speicher bleibt Out-of-Core (begrenzte Queue).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `enabled` | `boolean` | primitiv |

Rückgabewert: `this`, nie `null`. Keine Exceptions.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
