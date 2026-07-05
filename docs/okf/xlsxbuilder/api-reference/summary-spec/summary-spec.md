---
type: API Reference
title: SummarySpec
description: Paketinterner, unveraenderlicher Record - Konfiguration der optionalen Summenzeile, mit defensiver Kopie des veraenderlichen sum-Arrays.
resource: src/main/java/de/makno/xlsxbuilder/SummarySpec.java
tags: [api-reference, record, value-object, immutability, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`record SummarySpec(boolean[] sum, int labelColumnIndex, String labelText, boolean
useFormula)` — paketintern. Konfiguration der optionalen Summenzeile. Näher beschrieben in
[Konfigurationsobjekte](/components/configuration-models.md).

# Felder

Felder = Record-Komponenten (`sum`, `labelColumnIndex`, `labelText`, `useFormula`), siehe
[Konstruktor](./constructor.md) für die defensive Kopie von `sum`.

# Thread-Safety

Strukturell unveränderlich mit defensiver Kopie des veränderlichen `sum`-Arrays: sowohl der
kompakte Konstruktor (`sum = sum.clone()`) als auch der überschriebene Accessor `sum()` (siehe
[sum()](./sum.md)) geben jeweils eine Kopie zurück, sodass weder ein Aufrufer, der das
Original-Array noch referenziert, noch ein Aufrufer des Accessors den internen Zustand dieser
Instanz nachträglich mutieren kann. Beliebig zwischen Threads teilbar.

# Serialisierung

Nicht `Serializable` — `SummarySpec` implementiert kein Serialisierungs-Interface (verifiziert:
`record SummarySpec(...) {...}`, keine `implements`-Klausel).

# equals/hashCode/toString

**Nicht vollständig automatisch generiert** — abweichend vom Standard-Record-Verhalten:
`sum()` ist explizit überschrieben (defensive Kopie, siehe oben), daher basieren die
automatisch generierten `equals()`/`hashCode()` weiterhin auf dem **internen** Feldwert von
`sum` (Array-Identität, nicht `Arrays.equals`-Inhalt — Java-Arrays haben keine
werte-basierte `equals`-Implementierung). Zwei `SummarySpec`-Instanzen mit inhaltsgleichen,
aber unterschiedlichen `sum`-Arrays gelten daher als **ungleich** — ein Java-Records-Fallstrick
bei Array-Komponenten, der hier als verifizierter Befund festgehalten wird (nicht durch einen
eigenen `equals()`-Override behoben). `toString()` ist nicht überschrieben und verwendet daher
ebenfalls das automatisch generierte, array-identitätsbasierte Format für die `sum`-Komponente
(z. B. `[Z@1b6d3586` statt der Array-Inhalte).

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `record SummarySpec(boolean[] sum, int labelColumnIndex,
String labelText, boolean useFormula) {...}` — erweitert implizit `java.lang.Record`; keine
`implements`-Klausel im Quelltext.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; Records sind implizit `final`.
Wird als Feld-/Parametertyp in [RenderJob](/api-reference/render-job/render-job.md) und
[XlsxWriter](/api-reference/xlsx-writer/xlsx-writer.md) verwendet — Komposition, keine Vererbung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``boolean[] sum()` (ueberschriebener Record-Accessor)`](./sum.md)
- [``int labelColumnIndex()` (automatisch generierter Record-Accessor)`](./label-column-index.md)
- [``String labelText()` (automatisch generierter Record-Accessor)`](./label-text.md)
- [``boolean useFormula()` (automatisch generierter Record-Accessor)`](./use-formula.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SummarySpec.java`
[2] [Konfigurationsobjekte](/components/configuration-models.md)
