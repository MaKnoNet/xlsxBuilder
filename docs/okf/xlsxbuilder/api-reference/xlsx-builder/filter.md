---
type: API Reference
title: XlsxBuilder.filter(...)
description: Methode filter von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> filter(Predicate<? super T> predicate)`


Optionaler Filter auf die Rohdatensätze; nur Objekte, für die das Prädikat `true` liefert,
werden geschrieben. Wird **vor** Projektion, Sortierung und Summierung angewendet. Wiederholter
Aufruf kombiniert die Prädikate mit UND.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `predicate` | `Predicate<? super T>` | **nein** — `Objects.requireNonNull(predicate, "predicate")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`predicate == null`.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
