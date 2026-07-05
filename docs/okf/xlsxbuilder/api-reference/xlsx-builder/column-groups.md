---
type: API Reference
title: XlsxBuilder.columnGroups(...)
description: Methode columnGroups von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> columnGroups(List<ColumnGroup> groups)`


Optionale gruppierte Kopfzeile über den Spaltenköpfen. Wiederholter Aufruf **ersetzt** die
Gruppen (nicht additiv — `columnGroups.clear()` vor dem Befüllen, verifiziert).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `groups` | `List<ColumnGroup>` | **nein** — `Objects.requireNonNull(groups, "groups")` |
| jedes Element von `groups` | `ColumnGroup` | **nein** — `Objects.requireNonNull(group, "group")` pro Element |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn `groups`
oder ein Element davon `null` ist. **Nicht** an dieser Stelle geprüft: ob die Summe der Spans
der Spaltenanzahl entspricht — das geschieht erst später in `validatedColumnGroups()` beim
Rendern (`IllegalArgumentException`, siehe `renderInto`).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
