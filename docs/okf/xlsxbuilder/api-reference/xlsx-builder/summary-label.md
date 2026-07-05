---
type: API Reference
title: XlsxBuilder.summaryLabel(...)
description: Methode summaryLabel von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> summaryLabel(String columnName, String text)`


Optionales Label in der Summenzeile.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `columnName` | `String` | **nein** — `Objects.requireNonNull(columnName, "columnName")` |
| `text` | `String` | **nein** — `Objects.requireNonNull(text, "text")` |

**Verifizierte Präzisierung gegenüber einer möglichen Fehlerwartung:** der Javadoc-Kommentar
("Optional label ...") könnte suggerieren, dass `text` frei/optional (auch `null`) sein dürfte —
tatsächlich ist `text` **zwingend nicht-`null`**, sobald diese Methode überhaupt aufgerufen wird
(kein separater "Label entfernen"-Pfad vorhanden). Kein Widerspruch zur Doku, aber ein Detail,
das die Javadoc-Formulierung "Optional" nicht explizit klarstellt — hier ausdrücklich
festgehalten.

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`columnName` oder `text` `null` ist.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
