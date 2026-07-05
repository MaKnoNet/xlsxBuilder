---
type: API Reference
title: XlsxBuilder.splitOnRowLimit(...)
description: Methode splitOnRowLimit von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> splitOnRowLimit(boolean enabled)`


Steuert das Verhalten bei Überschreiten des Excel-Zeilenlimits: `false` (Default) wirft
`RowLimitExceededException` beim Schreiben; `true` setzt die Tabelle auf Folge-Sheets fort.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `enabled` | `boolean` | primitiv |

Rückgabewert: `this`, nie `null`. Diese Methode selbst wirft keine Exception (die
`RowLimitExceededException` fällt erst später beim tatsächlichen Schreiben in
`XlsxWriter.writeSheets`).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
