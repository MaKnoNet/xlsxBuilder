---
type: API Reference
title: SortSpec
description: Paketinterner, unveränderlicher Record — die Sortier-Konfiguration eines Sheets (mehrstufige Sortierschlüssel + Out-of-Core-Parameter der External Merge Sort).
resource: src/main/java/de/makno/xlsxbuilder/SortSpec.java
tags: [api-reference, record, value-object, sorting, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`record SortSpec(List<SortKey> sortKeys, int sortChunkSize, Path sortTempDir)` — paketintern.
Unveränderliche Sortier-Konfiguration: leere `sortKeys` bedeutet "nicht sortieren". Näher
beschrieben in [Konfigurationsobjekte](/components/configuration-models.md).

# Felder

Felder = Record-Komponenten (`sortKeys`, `sortChunkSize`, `sortTempDir`), siehe
[Konstruktor](./constructor.md). Anders als bei [SheetWriteOptions](/api-reference/sheet-write-options/sheet-write-options.md)
oder [SummarySpec](/api-reference/summary-spec/summary-spec.md) gibt es **keinen** kompakten
Konstruktor — `sortKeys` wird nicht defensiv kopiert (verifiziert: `record SortSpec(...) {}`
ohne Konstruktor-Body).

# Thread-Safety

Strukturell unveränderlich (alle Komponenten `final`), aber **ohne** defensive Kopie von
`sortKeys` — ein Aufrufer, der die übergebene `List<SortKey>` weiterhin referenziert und
mutiert, kann den Zustand dieser Instanz nachträglich verändern (anders als bei
[SheetWriteOptions](/api-reference/sheet-write-options/sheet-write-options.md), das
`List.copyOf(...)` verwendet). In der aktuellen internen Nutzung wird `SortSpec` nur mit bereits
nicht mehr mutierten Listen konstruiert, sodass das praktisch unkritisch ist.

# Serialisierung

Nicht `Serializable` — `SortSpec` implementiert kein Serialisierungs-Interface (verifiziert:
`record SortSpec(...) {}`, keine `implements`-Klausel).

# equals/hashCode/toString

Automatisch generiert (Record): komponentenbasiert über `sortKeys`, `sortChunkSize` und
`sortTempDir`, keine eigenen Overrides im Quellcode.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `record SortSpec(List<SortKey> sortKeys, int sortChunkSize,
Path sortTempDir) {}` — erweitert implizit `java.lang.Record`; keine `implements`-Klausel im
Quelltext.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; Records sind implizit `final`.
Referenziert [SortKey](/api-reference/sort-key/sort-key.md) als Komponenten-Typ (Komposition, keine
Vererbung).

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden


# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SortSpec.java`
[2] [Konfigurationsobjekte](/components/configuration-models.md)
