---
type: API Reference
title: SplitSheetNamer
description: Öffentliches funktionales Interface zum Benennen von Folge-Sheets, die bei splitOnRowLimit(true) entstehen.
resource: src/main/java/de/makno/xlsxbuilder/SplitSheetNamer.java
tags: [api-reference, functional-interface, configuration]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`@FunctionalInterface public interface SplitSheetNamer` — benennt die Folge-Sheets, die beim
Splitten eines Sheets am Excel-Zeilenlimit entstehen (siehe
[XlsxBuilder.splitOnRowLimit(boolean)](/api-reference/xlsx-builder/split-on-row-limit.md)). Der Namer wird **nur**
für Folge-Sheets konsultiert — das erste Sheet behält immer den über
`XlsxBuilder.sheetName(String)` konfigurierten Namen (beim Streaming ist ein Split erst bekannt,
sobald das erste Sheet voll ist). Ohne Namer gilt das Default-Schema `"Name (2)"`,
`"Name (3)"`, ...

Der zurückgegebene Name wird Excel-safe gemacht (ungültige Zeichen ersetzt, max. 31 Zeichen),
aber bewusst **nicht** dedupliziert: ein bereits existierender Name schlägt mit
`IllegalStateException` fehl, sodass der Aufrufer die Kontrolle über die tatsächlichen Namen
behält. Näher beschrieben in
[Konfigurationsobjekte](/components/configuration-models.md).

# Felder

Keine Felder — reines funktionales Interface ohne Zustand (eine abstrakte Methode
`partSheetName(...)`).

# Thread-Safety

Kein Vertrag auf Interface-Ebene erzwingbar. Da Aufrufer typischerweise eine zustandslose
Lambda übergeben (reine Namensberechnung aus `baseSheetName`/`partNumber`), ist
Thread-Sicherheit in der Praxis unproblematisch — sofern die konkrete Implementierung keinen
veränderlichen, geteilten Zustand einführt, wofür der Aufrufer verantwortlich ist.

# Serialisierung

Nicht `Serializable` — `SplitSheetNamer` implementiert kein Serialisierungs-Interface
(verifiziert: `@FunctionalInterface public interface SplitSheetNamer`, keine
`extends`-Klausel).

# equals/hashCode/toString

Das Interface deklariert keine dieser Methoden; typischerweise als Lambda implementiert, für
die die JVM eine synthetische, identitätsbasierte Implementierung erzeugt.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `@FunctionalInterface public interface SplitSheetNamer` —
keine `extends`-Klausel, keine Oberklasse.

**Rückwärts:** Keine Klasse im Projekt implementiert dieses Interface namentlich (verifiziert per
Grep: keine Treffer für `implements.*SplitSheetNamer`). Wird als Feld-/Parametertyp in
[SheetWriteOptions](/api-reference/sheet-write-options/sheet-write-options.md) und
`XlsxBuilder.splitSheetNamer(SplitSheetNamer)` verwendet; Aufrufer übergeben typischerweise eine
Lambda ohne benannte Implementierungsklasse.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``String partSheetName(String baseSheetName, int partNumber)``](./part-sheet-name.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SplitSheetNamer.java`
[2] [Konfigurationsobjekte](/components/configuration-models.md)
