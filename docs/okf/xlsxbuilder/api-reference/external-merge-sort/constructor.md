---
type: API Reference
title: ExternalMergeSort – Konstruktoren
description: Alle Konstruktoren von ExternalMergeSort.
resource: src/main/java/de/makno/xlsxbuilder/ExternalMergeSort.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## `ExternalMergeSort(Comparator<Row> comparator, int chunkSize)`

Delegiert an den Vollkonstruktor mit `baseTempDir = null`.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `comparator` | `Comparator<Row>` | im Vollkonstruktor **nicht geprüft** — kein `requireNonNull`; ein `null`-Comparator führt erst bei der tatsächlichen Verwendung (`buffer.sort(comparator)` bzw. im `PriorityQueue`-Vergleich) zu einer `NullPointerException`, nicht sofort im Konstruktor |
| `chunkSize` | `int` | primitiv; muss `>= 1` sein |

Verhalten bei ungültiger Eingabe: `IllegalArgumentException("chunkSize must be >= 1")`, wenn
`chunkSize < 1` (Prüfung sitzt im Vollkonstruktor, an den delegiert wird).

## `ExternalMergeSort(Comparator<Row> comparator, int chunkSize, Path baseTempDir)`

Vollkonstruktor.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `comparator` | `Comparator<Row>` | **nicht geprüft** (siehe oben — abweichend von einer naiven Erwartung, dass ein Kern-Kollaborator immer `requireNonNull`-geprüft wird) |
| `chunkSize` | `int` | muss `>= 1` sein, sonst `IllegalArgumentException` |
| `baseTempDir` | `Path` | **ja** — `null` bedeutet System-Temp (`java.io.tmpdir`), laut Javadoc "Created on demand" |

Geworfene Exceptions: `IllegalArgumentException("chunkSize must be >= 1")` bei `chunkSize < 1`.
Kein `NullPointerException` bei `comparator == null` an dieser Stelle (erst bei späterer
Verwendung in `sort(...)`).

# Citations

[1] [ExternalMergeSort (Übersicht)](./external-merge-sort.md)
