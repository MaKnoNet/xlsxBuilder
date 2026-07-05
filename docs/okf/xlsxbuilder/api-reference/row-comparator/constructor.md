---
type: API Reference
title: RowComparator – Konstruktoren
description: Alle Konstruktoren von RowComparator.
resource: src/main/java/de/makno/xlsxbuilder/RowComparator.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## `RowComparator(List<? extends Column<?>> columns, List<SortKey> sortKeys)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `columns` | `List<? extends Column<?>>` | **nicht explizit geprüft** — kein `requireNonNull`; ein `null`-`columns` führt bei der Suche nach dem Spaltenindex (`indexOf(columns, ...)`, ruft `columns.size()` auf) zu `NullPointerException` |
| `sortKeys` | `List<SortKey>` | **nicht auf `null` geprüft**, aber auf **Leerheit** geprüft — `sortKeys.isEmpty()` wird ohne vorherigen Null-Check aufgerufen, ein `null`-Argument löst daher `NullPointerException` statt der dokumentierten `IllegalArgumentException` aus |

Verhalten bei ungültiger Eingabe:
- `IllegalArgumentException("At least one SortKey is required")`, wenn `sortKeys.isEmpty()`
  (verifiziert — **aber nur, wenn `sortKeys` selbst nicht `null` ist**; bei `sortKeys == null`
  wird stattdessen eine `NullPointerException` bei `.isEmpty()` geworfen, was die Javadoc-freie,
  aber implizit erwartbare Erwartung "sortKeys darf nicht null sein" nicht explizit als
  `IllegalArgumentException` behandelt).
- `IllegalArgumentException("Unknown sort column: " + key.columnName())`, wenn ein
  `SortKey.columnName()` in `columns` nicht gefunden wird (`indexOf(...)` liefert `-1`).

# Citations

[1] [RowComparator (Übersicht)](./row-comparator.md)
