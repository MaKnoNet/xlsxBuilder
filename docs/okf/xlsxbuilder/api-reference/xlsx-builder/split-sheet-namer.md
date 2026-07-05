---
type: API Reference
title: XlsxBuilder.splitSheetNamer(...)
description: Methode splitSheetNamer von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> splitSheetNamer(SplitSheetNamer namer)`


Optionale Benennung der Folge-Sheets, die durch `splitOnRowLimit(true)` entstehen.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `namer` | `SplitSheetNamer` | **nein** — `Objects.requireNonNull(namer, "namer")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`namer == null`. **Bemerkenswert:** anders als bei `placeholderResolver(Function)` (siehe unten)
gibt es hier keine Möglichkeit, den Namer explizit wieder auf "kein Namer" (`null`)
zurückzusetzen, da ein `null`-Argument sofort abgelehnt wird — konsistent mit der übrigen
`requireNonNull`-Praxis, aber im Unterschied zum internen `null`-Startzustand des Feldes
(`splitSheetNamer` ist intern `null`, bis diese Methode aufgerufen wird).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
