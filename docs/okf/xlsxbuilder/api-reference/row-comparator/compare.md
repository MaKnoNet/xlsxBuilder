---
type: API Reference
title: RowComparator.compare(...)
description: Methode compare von RowComparator - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/RowComparator.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `int compare(Row a, Row b)`


Aus `Comparator<Row>` überschrieben.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `a` | `Row` | nicht geprüft — `a.get(idx)` löst `NullPointerException` bei `a == null` aus |
| `b` | `Row` | nicht geprüft — analog |

Rückgabewert: `int`, negativ/`0`/positiv nach `Comparator`-Konvention; primitiv, kein `null`
möglich. Iteriert über alle konfigurierten Sortierstufen, verwendet die erste Stufe mit
`c != 0`; sind alle Stufen gleich, wird `0` zurückgegeben (Zeilen als "gleich" behandelt).

Geworfene Exceptions (über die private Hilfsmethode `compareValues`):
- `IllegalArgumentException("Sort column '...' is of type ... and cannot be sorted (not
  Comparable)")`, wenn ein nicht-`null`-Zellwert nicht `Comparable` implementiert.
- `IllegalArgumentException("Sort column '...' contains non-comparable value types (X vs. Y)")`,
  wenn `compareTo` eine `ClassCastException` wirft (z. B. gemischte Werttypen in derselben
  Spalte über verschiedene Datensätze hinweg) — die `ClassCastException` wird abgefangen und in
  diese sprechendere `IllegalArgumentException` übersetzt (verifiziert:
  `catch (ClassCastException e)`-Block).
- `NullPointerException`, wenn `a` oder `b` selbst `null` ist (kein Row-Level-Null-Check).

# Citations

[1] [RowComparator (Übersicht)](./row-comparator.md)
