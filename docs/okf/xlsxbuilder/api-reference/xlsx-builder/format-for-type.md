---
type: API Reference
title: XlsxBuilder.formatForType(...)
description: Methode formatForType von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> formatForType(String format)`


Setzt den Excel-Formatcode der zuletzt definierten Spalte.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `format` | `String` | **ja** — wird ungeprüft an `Column.withFormat(format)` weitergereicht, das selbst keine Null-Prüfung vornimmt |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
`IllegalStateException("ofType()/formatForType() requires a preceding column(...)")`, wenn noch
keine Spalte definiert wurde. Kein `NullPointerException` bei `format == null` (bewusst erlaubt,
bedeutet Default-Format des Typs).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
