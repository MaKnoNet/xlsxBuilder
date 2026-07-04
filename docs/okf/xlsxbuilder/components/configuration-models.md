---
type: Library Component
title: Konfigurationsobjekte (SheetWriteOptions, SortSpec, SummarySpec, ColumnGroup, SplitSheetNamer, Placeholders)
description: Unveraenderliche Value-Objects, die die fluente XlsxBuilder-Konfiguration von der Ausfuehrung (SheetRenderer) trennen.
resource: src/main/java/de/makno/xlsxbuilder/SheetWriteOptions.java
tags: [component, immutability, value-object, configuration]
timestamp: '2026-07-05T09:30:00+02:00'
---

# Überblick

Diese Records/Interfaces bündeln die fluente Konfiguration von
[XlsxBuilder](/components/xlsx-builder.md) als **unveränderliche Value-Objects**, bevor
sie an [RenderJob](/components/xlsx-builder.md)/`SheetRenderer` zur Ausführung übergeben
werden — passend zum Immutability-Prinzip aus den globalen Konventionen.

# Schema

| Typ | Zweck |
|---|---|
| `SheetWriteOptions` | bündelt alle Layout-Parameter (Titel-/Fußzeilen, `columnGroups`, `placeholders`/`placeholderResolver`, `showColumnHeaders`, `defaultNullText`, `splitOnRowLimit`, `splitSheetNamer`, `maxRowsPerSheet`) — hält die Writer-Signaturen schlank |
| `SortSpec` | unveränderliche Sortier-Konfiguration: mehrstufige `sortKeys` (leer = keine Sortierung) + EMS-Parameter `sortChunkSize`/`sortTempDir` |
| `SummarySpec` | Summenzeilen-Konfiguration: `sum`-Flags je Spalte (defensiv geklont, da `boolean[]` veränderlich ist), Label-Spalte/-Text, `useFormula` (`=SUM(...)` vs. vorberechnet) |
| `ColumnGroup` | eine Zelle der optionalen gruppierten Kopfzeile: `label` + `span` (validiert `>= 1`) |
| `SplitSheetNamer` | `@FunctionalInterface`, benennt Folge-Sheets bei `splitOnRowLimit(true)`; Ergebnis wird Excel-safe gemacht, aber bewusst nicht dedupliziert (Namenskollision → `IllegalStateException`) |
| `Placeholders` | ersetzt `{key}`-Tokens in Titel-/Kopf-/Fußzeilen; unbekannte Tokens bleiben sichtbar stehen statt still verschluckt zu werden |

# Citations

[1] [README - XlsxBuilder<T>: Methodentabelle](https://github.com/MaKnoNet/xlsxBuilder/blob/main/README.md)
