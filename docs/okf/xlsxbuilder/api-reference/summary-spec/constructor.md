---
type: API Reference
title: SummarySpec – Konstruktoren
description: Alle Konstruktoren von SummarySpec.
resource: src/main/java/de/makno/xlsxbuilder/SummarySpec.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## Kompakter Konstruktor `SummarySpec { sum = sum.clone(); }`

Defensive Kopie beim Konstruieren: `sum` ist ein veraenderliches Array, ohne diese Kopie koennte
ein vom Aufrufer weiterhin referenziertes Array das Value Object nachtraeglich mutieren.

| Parameter | Typ | null-erlaubt | Verhalten bei ungueltiger Eingabe |
|---|---|---|---|
| `sum` | `boolean[]` | nein - nicht per requireNonNull, sondern implizit ueber `sum.clone()`, das bei `sum == null` eine `NullPointerException` ausloest | `NullPointerException` bei `sum == null` |
| `labelColumnIndex` | `int` | primitiv; laut Javadoc `-1`, wenn keine Label-Spalte gewuenscht ist; kein Bereichscheck in diesem Record | keine eigene Validierung |
| `labelText` | `String` | ja - nur relevant, wenn `labelColumnIndex >= 0`; nicht auf `null` geprueft | keine |
| `useFormula` | `boolean` | primitiv | keine |

# Citations

[1] [SummarySpec (Übersicht)](./summary-spec.md)
