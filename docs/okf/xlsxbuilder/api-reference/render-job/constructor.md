---
type: API Reference
title: RenderJob – Konstruktoren
description: Alle Konstruktoren von RenderJob.
resource: src/main/java/de/makno/xlsxbuilder/RenderJob.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## Kanonischer Record-Konstruktor `RenderJob(String sheetName, List<Column<T>> columns, Predicate<? super T> filter, DataProvider<T> dataProvider, SortSpec sort, SummarySpec summary, SheetWriteOptions layout, boolean parallel)`

Kein kompakter Validierungs-Konstruktor im Quelltext — die Komponenten werden **ungeprüft**
übernommen (kein `Objects.requireNonNull`, keine defensiven Kopien).

| Parameter | Typ | null-erlaubt | Bedeutung |
|---|---|---|---|
| `sheetName` | `String` | nicht geprüft durch den Record selbst; der einzige Aufrufer ([XlsxBuilder.renderInto](/api-reference/xlsx-builder/render-into.md)) übergibt stets das über `sheetName(String)` gesetzte, dort bereits `requireNonNull`-geprüfte Feld — praktisch also nie `null`, aber der Record selbst erzwingt das nicht |
| `columns` | `List<Column<T>>` | nicht geprüft; der Aufrufer übergibt `List.copyOf(columns)`, das bei einer leeren Liste **nicht** fehlschlägt (`XlsxBuilder.renderInto` validiert `columns.isEmpty()` bereits vorher separat) |
| `filter` | `Predicate<? super T>` | **ja** — `null` bedeutet laut Doku "alle Datensätze" (kein Filter) |
| `dataProvider` | `DataProvider<T>` | nicht geprüft durch den Record; der Aufrufer stellt sicher, dass er nicht `null` ist (`XlsxBuilder.renderInto` wirft vorher `IllegalStateException`, falls kein Provider gesetzt wurde) |
| `sort` | `SortSpec` | nicht geprüft; leere `sortKeys()` bedeuten "unsortiert" |
| `summary` | `SummarySpec` | **ja** — `null` bedeutet "keine Summenzeile" |
| `layout` | `SheetWriteOptions` | nicht geprüft |
| `parallel` | `boolean` | primitiv, kein `null` möglich |

Verhalten bei ungültiger Eingabe: **keine eigene Validierung** — dieser Record ist ein reiner
Transportbehälter; jegliche Validierung ist bereits vorgelagert in `XlsxBuilder.renderInto()`
erfolgt (Spalten nicht leer, Sortierspalten bekannt/sortierbar, Summenspalten bekannt/numerisch,
Column-Groups-Summe passend). Keine Exceptions werden vom Konstruktor selbst geworfen.

# Citations

[1] [RenderJob (Übersicht)](./render-job.md)
