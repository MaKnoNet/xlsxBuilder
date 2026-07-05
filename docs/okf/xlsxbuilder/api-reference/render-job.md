---
type: API Reference
title: RenderJob
description: Paketinterner, unveränderlicher Record — die vollständige Ausführungsbeschreibung eines Sheets, kompiliert vom XlsxBuilder für den SheetRenderer.
resource: src/main/java/de/makno/xlsxbuilder/RenderJob.java
tags: [api-reference, record, value-object, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`record RenderJob<T>(...)` — paketintern, unveränderlich. Trennt die fluente Konfiguration
([XlsxBuilder](/api-reference/xlsx-builder.md)) von der Ausführung
([SheetRenderer](/api-reference/sheet-renderer.md)). Siehe auch
[DataProvider (Komponente) – Supporting types](/components/data-provider.md).

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `record RenderJob<T>(...)` — erweitert implizit
`java.lang.Record`; keine `implements`-Klausel im Quelltext.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; Records sind implizit `final`.
Wird nur als Rückgabe-/Parametertyp zwischen `XlsxBuilder` und `SheetRenderer` verwendet — keine
Vererbungsbeziehung.

# Konstruktoren

## Kanonischer Record-Konstruktor `RenderJob(String sheetName, List<Column<T>> columns, Predicate<? super T> filter, DataProvider<T> dataProvider, SortSpec sort, SummarySpec summary, SheetWriteOptions layout, boolean parallel)`

Kein kompakter Validierungs-Konstruktor im Quelltext — die Komponenten werden **ungeprüft**
übernommen (kein `Objects.requireNonNull`, keine defensiven Kopien).

| Parameter | Typ | null-erlaubt | Bedeutung |
|---|---|---|---|
| `sheetName` | `String` | nicht geprüft durch den Record selbst; der einzige Aufrufer ([XlsxBuilder.renderInto](/api-reference/xlsx-builder.md)) übergibt stets das über `sheetName(String)` gesetzte, dort bereits `requireNonNull`-geprüfte Feld — praktisch also nie `null`, aber der Record selbst erzwingt das nicht |
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

# Methoden

Als Record werden alle Komponenten-Zugriffsmethoden automatisch generiert (keine expliziten
Methoden im Quelltext): `sheetName()`, `columns()`, `filter()`, `dataProvider()`, `sort()`,
`summary()`, `layout()`, `parallel()`. Jede liefert exakt den im Konstruktor übergebenen Wert
unverändert zurück (kein Klonen) — für `filter()` und `summary()` also potenziell `null`, für
die übrigen praktisch nie `null` (siehe oben), aber ohne Laufzeit-Garantie durch den Record
selbst.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/RenderJob.java`
[2] [DataProvider (Komponente)](/components/data-provider.md)
