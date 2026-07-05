---
type: API Reference
title: Column.extract(...)
description: Methode extract von Column - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/Column.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `Object extract(T record)`


Liefert den Zellwert für den Datensatz.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `record` | `T` | wird direkt an `extractor.apply(record)` durchgereicht; ob `null` erlaubt ist, hängt vom konkreten Extractor ab — die Methode selbst prüft nicht auf `null` |

Rückgabewert: extrahierter (ggf. konvertierter) Zellwert, **kann `null` sein** — wenn der
Extractor `null` liefert, wird der Converter (falls gesetzt) **nicht** aufgerufen und `null`
direkt zurückgegeben (verifiziert: `if (value == null || converter == null) return value;`).
Ist ein Converter gesetzt und der extrahierte Wert nicht `null`, wird `converter.apply(value)`
angewendet und dessen Ergebnis zurückgegeben (kann theoretisch wieder `null` sein, falls der
Converter das tut). Geworfene Exceptions: keine eigenen; eine `RuntimeException` aus
`extractor.apply(...)` oder `converter.apply(...)` propagiert ungefangen.

# Citations

[1] [Column (Übersicht)](./column.md)
