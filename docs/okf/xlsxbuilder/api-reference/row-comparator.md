---
type: API Reference
title: RowComparator
description: Paketinterner Comparator<Row> aus einer Liste von SortKeys — null-sicher, mehrstufig, ASC/DESC.
resource: src/main/java/de/makno/xlsxbuilder/RowComparator.java
tags: [api-reference, comparator, sorting, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`final class RowComparator implements Comparator<Row>` — paketintern. Baut einen `Comparator`
über projizierte [Row](/api-reference/row.md)s aus den
[SortKey](/api-reference/sort-key.md)s. Vergleicht Zellwerte nach natürlicher Ordnung
(`Comparable`), null-sicher, unterstützt mehrstufige Sortierung sowie `DESC`.

**Null-Ordnung** (Javadoc, verifiziert korrekt): `null`-Werte werden bei `SortOrder.ASC` **zuletzt**
einsortiert. `DESC` negiert den gesamten Vergleich (inklusive der Null-Behandlung), sodass bei
`SortOrder.DESC` `null`-Werte **zuerst** erscheinen — die konventionelle Konsequenz der
Umkehrung eines "nulls-last"-Comparators.

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `final class RowComparator implements Comparator<Row>` —
implementiert das JDK-Funktionsinterface `java.util.Comparator<Row>` direkt; keine eigene
Oberklasse außer `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, daher nicht
erweiterbar, und kein anderer Typ implementiert `RowComparator` (es ist keine Schnittstelle). Wird
in [ExternalMergeSort](/api-reference/external-merge-sort.md) als `Comparator<Row>`-Parameter
verwendet — Verwendung, keine Vererbung.

# Konstruktoren

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

# Methoden

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

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/RowComparator.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
