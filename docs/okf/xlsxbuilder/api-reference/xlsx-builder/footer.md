---
type: API Reference
title: XlsxBuilder.footer(...)
description: Methode footer von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> footer(String... lines)`


Optionale Fußzeile(n) unterhalb der Daten, jede über die volle Breite verschmolzen.
Unterstützt Platzhalter inkl. dynamischer `{rowCount}` und `{sum:Column}`.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `lines` | `String...` | jedes Element: **nein** — `Objects.requireNonNull(line, "line")` pro Element |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn ein
Element von `lines` `null` ist.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
