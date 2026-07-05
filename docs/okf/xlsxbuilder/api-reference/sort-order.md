---
type: API Reference
title: SortOrder
description: Öffentliches Enum für die Sortierrichtung — ASC oder DESC.
resource: src/main/java/de/makno/xlsxbuilder/SortOrder.java
tags: [api-reference, enum, sorting]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`public enum SortOrder { ASC, DESC }` — die Sortierrichtung, verwendet von
[SortKey](/api-reference/sort-key.md) und ausgewertet von
[RowComparator](/api-reference/row-comparator.md) (bei `DESC` wird der Gesamtvergleich
inklusive Null-Behandlung negiert). Enum-Konstanten sind implizit unveränderlich und
thread-sicher.

# Konstruktoren

Keine öffentlichen Konstruktoren (Enum ohne eigenen Enum-Konstruktor — anders als
[ColumnType](/api-reference/column-type.md), das einen privaten Enum-Konstruktor mit
`sortable`-Flag besitzt, hat `SortOrder` überhaupt keinen expliziten Konstruktor im Quelltext).

# Methoden

Keine eigenen Methoden — nur die von jedem Enum geerbten (`name()`, `ordinal()`, `valueOf(...)`,
`values()`), keine davon im Quelltext überschrieben.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SortOrder.java`
