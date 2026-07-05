---
type: API Reference
title: SortKey – Konstruktoren
description: Alle Konstruktoren von SortKey.
resource: src/main/java/de/makno/xlsxbuilder/SortKey.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## Kompakter Konstruktor `SortKey { ... }`

| Parameter | Typ | null-erlaubt | Verhalten bei ungültiger Eingabe |
|---|---|---|---|
| `columnName` | `String` | **nein** — `Objects.requireNonNull(columnName, "columnName")` | wirft `NullPointerException` |
| `order` | `SortOrder` | **nein** — `Objects.requireNonNull(order, "order")` | wirft `NullPointerException` |

Keine sonstige Validierung (z. B. ob `columnName` eine tatsächlich existierende Spalte
bezeichnet) — das wird erst später, beim Aufbau des `RowComparator` bzw. in
[XlsxBuilder.renderInto](/api-reference/xlsx-builder/render-into.md), gegen die konkrete Spaltenliste geprüft
(`IllegalArgumentException("Unknown sort column: ...")`, nicht Teil dieser Klasse).

# Citations

[1] [SortKey (Übersicht)](./sort-key.md)
