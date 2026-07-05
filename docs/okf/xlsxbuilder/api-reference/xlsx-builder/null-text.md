---
type: API Reference
title: XlsxBuilder.nullText(...)
description: Methode nullText von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> nullText(String text)`


Platzhalter für `null`-Werte der zuletzt definierten Spalte (überschreibt
`defaultNullText(String)`). `""` erzwingt eine leere Textzelle trotz konfiguriertem Default.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `text` | `String` | **ja** — ungeprüft an `Column.withNullText(text)` weitergereicht |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
`IllegalStateException("ofType()/formatForType() requires a preceding column(...)")` (dieselbe
Meldung wie bei `ofType`/`formatForType`, obwohl der Methodenname `nullText` lautet —
verifizierte kleine Ungenauigkeit: die Fehlermeldung nennt nicht die tatsächlich aufrufende
Methode; die Fehlerbedingung selbst — "muss nach column(...) kommen" — ist aber korrekt und
identisch für alle vier Konfiguratoren `ofType`/`formatForType`/`nullText`/
`convertToColumnType`, da sie dieselbe private `lastColumn()`-Hilfsmethode nutzen), wenn noch
keine Spalte definiert wurde.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
