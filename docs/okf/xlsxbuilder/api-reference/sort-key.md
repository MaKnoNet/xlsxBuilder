---
type: API Reference
title: SortKey
description: Öffentlicher, unveränderlicher Record — eine Sortierstufe (Spaltenname + Richtung); mehrere Keys ergeben eine mehrstufige Sortierung.
resource: src/main/java/de/makno/xlsxbuilder/SortKey.java
tags: [api-reference, record, value-object, sorting]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`public record SortKey(String columnName, SortOrder order)` — eine Sortierstufe. Mehrere Keys
in einer Liste (siehe [XlsxBuilder.sortBy](/api-reference/xlsx-builder.md)) ergeben eine
mehrstufige Sortierung, ausgewertet von
[RowComparator](/api-reference/row-comparator.md).

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `public record SortKey(String columnName, SortOrder order)` —
erweitert implizit `java.lang.Record`; keine `implements`-Klausel im Quelltext.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; Records sind implizit `final`.
Referenziert [SortOrder](/api-reference/sort-order.md) als Komponenten-Typ (Komposition, keine
Vererbung). Wird als Element-/Parametertyp in `XlsxBuilder.sortBy(...)` und
[RowComparator](/api-reference/row-comparator.md) verwendet — ebenfalls reine Verwendung.

# Konstruktoren

## Kompakter Konstruktor `SortKey { ... }`

| Parameter | Typ | null-erlaubt | Verhalten bei ungültiger Eingabe |
|---|---|---|---|
| `columnName` | `String` | **nein** — `Objects.requireNonNull(columnName, "columnName")` | wirft `NullPointerException` |
| `order` | `SortOrder` | **nein** — `Objects.requireNonNull(order, "order")` | wirft `NullPointerException` |

Keine sonstige Validierung (z. B. ob `columnName` eine tatsächlich existierende Spalte
bezeichnet) — das wird erst später, beim Aufbau des `RowComparator` bzw. in
`XlsxBuilder.renderInto`, gegen die konkrete Spaltenliste geprüft
(`IllegalArgumentException("Unknown sort column: ...")`, nicht Teil dieser Klasse).

# Methoden

Als Record automatisch generiert:

## `String columnName()`

Keine Parameter. Rückgabewert: der Spaltenname, **nie `null`** (Konstruktor-Invariante).

## `SortOrder order()`

Keine Parameter. Rückgabewert: die Sortierrichtung, **nie `null`** (Konstruktor-Invariante).

Keine der beiden Methoden wirft eine Exception.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SortKey.java`
