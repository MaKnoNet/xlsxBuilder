---
type: API Reference
title: SummarySpec
description: Paketinterner, unveraenderlicher Record - Konfiguration der optionalen Summenzeile, mit defensiver Kopie des veraenderlichen sum-Arrays.
resource: src/main/java/de/makno/xlsxbuilder/SummarySpec.java
tags: [api-reference, record, value-object, immutability, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Ueberblick

`record SummarySpec(boolean[] sum, int labelColumnIndex, String labelText, boolean
useFormula)` - paketintern. Konfiguration der optionalen Summenzeile. Naeher beschrieben in
[Konfigurationsobjekte](/components/configuration-models.md).

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `record SummarySpec(boolean[] sum, int labelColumnIndex,
String labelText, boolean useFormula) {...}` — erweitert implizit `java.lang.Record`; keine
`implements`-Klausel im Quelltext.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; Records sind implizit `final`.
Wird als Feld-/Parametertyp in [RenderJob](/api-reference/render-job.md) und
[XlsxWriter](/api-reference/xlsx-writer.md) verwendet — Komposition, keine Vererbung.

# Konstruktoren

## Kompakter Konstruktor `SummarySpec { sum = sum.clone(); }`

Defensive Kopie beim Konstruieren: `sum` ist ein veraenderliches Array, ohne diese Kopie koennte
ein vom Aufrufer weiterhin referenziertes Array das Value Object nachtraeglich mutieren.

| Parameter | Typ | null-erlaubt | Verhalten bei ungueltiger Eingabe |
|---|---|---|---|
| `sum` | `boolean[]` | nein - nicht per requireNonNull, sondern implizit ueber `sum.clone()`, das bei `sum == null` eine `NullPointerException` ausloest | `NullPointerException` bei `sum == null` |
| `labelColumnIndex` | `int` | primitiv; laut Javadoc `-1`, wenn keine Label-Spalte gewuenscht ist; kein Bereichscheck in diesem Record | keine eigene Validierung |
| `labelText` | `String` | ja - nur relevant, wenn `labelColumnIndex >= 0`; nicht auf `null` geprueft | keine |
| `useFormula` | `boolean` | primitiv | keine |

# Methoden

## `boolean[] sum()` (ueberschriebener Record-Accessor)

Explizit ueberschrieben (nicht der automatisch generierte Accessor): gibt eine Kopie
(`sum.clone()`) zurueck, damit Aufrufer die internen Summen-Flags des Value Objects nicht
nachtraeglich mutieren koennen.

Keine Parameter. Rueckgabewert: `boolean[]`, nie `null` (da das Feld selbst nie `null` sein
kann - der kompakte Konstruktor haette sonst bereits eine `NullPointerException` geworfen), stets
eine frische Kopie bei jedem Aufruf. Keine Exceptions.

## `int labelColumnIndex()` (automatisch generierter Record-Accessor)

Keine Parameter. Rueckgabewert: Spaltenindex fuer ein Label, oder `-1`, wenn keine Label-Spalte
konfiguriert ist. Primitiv, nie `null`. Keine Exceptions.

## `String labelText()` (automatisch generierter Record-Accessor)

Keine Parameter. Rueckgabewert: der Label-Text - kann `null` sein, insbesondere wenn
`labelColumnIndex() == -1` (dann ist der Wert laut Vertrag ohnehin irrelevant), aber der Record
erzwingt das nicht als Invariante. Keine Exceptions.

## `boolean useFormula()` (automatisch generierter Record-Accessor)

Keine Parameter. Rueckgabewert: `true` = Summenzeile als Excel-Formel `=SUM(...)`, `false` =
vorberechneter Wert. Primitiv, nie `null`. Keine Exceptions.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SummarySpec.java`
[2] [Konfigurationsobjekte](/components/configuration-models.md)
