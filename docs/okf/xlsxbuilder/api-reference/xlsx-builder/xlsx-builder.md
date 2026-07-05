---
type: API Reference
title: XlsxBuilder
description: Oeffentlicher, fluenter, nicht thread-sicherer Single-Use-Builder fuer genau ein Sheet mit eigenem Datentyp T - Spalten, Sortierung, Summenzeile, Layout, Split-Handling und Parallelitaet.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, builder, excel, sheet]
timestamp: '2026-07-08T09:00:00+02:00'
---

# Überblick

`public final class XlsxBuilder<T>` — beschreibt genau **ein** Sheet inkl. seiner Datenquelle
(`.data(DataProvider)`). Narrative Gesamtbeschreibung, Feature-Übersicht und Beispielcode
bereits in [XlsxBuilder (Komponente)](/components/xlsx-builder.md) sowie
[Konfigurationsobjekte](/components/configuration-models.md) — diese Datei fokussiert auf die
vollständige, verifizierte Methoden-Ebene jeder einzelnen Konfigurationsmethode.

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `DEFAULT_CHUNK_SIZE` | `static final int` | Konstante (`100_000`) — Default-Chunk-Größe der External Merge Sort, überschreibbar via `sortChunkSize(int)`. | entfällt (primitiv `int`) |
| `sheetName` | `String` | Name des Excel-Sheets, Default `"Sheet1"`. | nein — Setter erzwingt `requireNonNull`; Feld ist durch den Default-Wert nie `null` |
| `headerLines` | `final List<String>` | Titelzeilen über den Spaltenköpfen, gefüllt via `header(String...)`. | Feld selbst nie `null` (final, mit `new ArrayList<>()` initialisiert); Elemente nie `null` (Setter prüft pro Element) |
| `footerLines` | `final List<String>` | Fußzeilen unterhalb der Daten, gefüllt via `footer(String...)`. | wie `headerLines` |
| `columnGroups` | `final List<ColumnGroup>` | Gruppierte Kopfzeile, gefüllt/ersetzt via `columnGroups(List)`. | Feld nie `null`; Elemente nie `null` |
| `columns` | `final List<Column<T>>` | Definierte Spalten in Deklarationsreihenfolge. | Feld nie `null`; Elemente nie `null` (nur über `column(...)` befüllt) |
| `sortKeys` | `final List<SortKey>` | Sortierstufen, gefüllt via `sortBy(String, SortOrder)`. | Feld nie `null`; Elemente nie `null` |
| `sumColumnNames` | `final List<String>` | Namen der zu summierenden Spalten, gefüllt via `sumColumn(String)`. | Feld nie `null`; Elemente nie `null` |
| `placeholders` | `final Map<String, String>` | Statische `{key}`-Platzhalter, gefüllt via `placeholder(...)`/`placeholders(...)`. | Feld nie `null`; Keys/Values nie `null` (Setter prüft) |
| `placeholderResolver` | `Function<String, String>` | Optionaler Resolver für lazy/berechnete Platzhalter. | ja — Default `null` bedeutet "nur statische Platzhalter" (Kommentar im Code verifiziert) |
| `summaryLabelColumn` | `String` | Spaltenname für das Summenzeilen-Label. | ja — Default `null` bedeutet "kein Label" |
| `summaryLabelText` | `String` | Text des Summenzeilen-Labels. | ja — Default `null`; wird nur zusammen mit `summaryLabelColumn` gesetzt (beide via `summaryLabel(String, String)`, dort beide `requireNonNull`) |
| `summaryAsFormula` | `boolean` | `true` = Summenzeile als Excel-Formel statt vorberechnetem Wert. Default `false`. | entfällt (primitiv) |
| `showColumnHeaders` | `boolean` | Ob die Spaltenkopfzeile geschrieben wird. Default `true`. | entfällt (primitiv) |
| `sortChunkSize` | `int` | Chunk-Größe der External Merge Sort. Default `DEFAULT_CHUNK_SIZE` (100.000). | entfällt (primitiv); Setter erzwingt `>= 1` |
| `sortTempDir` | `Path` | Basisverzeichnis für Sortier-Temp-Dateien. | ja — Default `null` bedeutet System-Temp (`java.io.tmpdir`, Kommentar im Code verifiziert) |
| `filter` | `Predicate<? super T>` | Optionaler Filter auf Rohdatensätze. | ja — Default `null` bedeutet "keine Filterung (alle Objekte)" (Kommentar im Code verifiziert) |
| `defaultNullText` | `String` | Sheet-weiter Platzhalter für `null`-Zellwerte. | ja — Default `null` bedeutet leere Zelle (Kommentar im Code verifiziert) |
| `parallel` | `boolean` | Pipeline-Parallelität ein/aus. Default `false`. | entfällt (primitiv) |
| `splitOnRowLimit` | `boolean` | `false` (Default) = Exception bei Zeilenlimit; `true` = Split auf Folge-Sheets. | entfällt (primitiv) |
| `splitSheetNamer` | `SplitSheetNamer` | Optionale Benennung der Folge-Sheets. | ja — Default `null` bedeutet Standard-Namensschema `"Name (2)"`, `"Name (3)"`, … (Kommentar im Code verifiziert) |
| `maxRowsPerSheet` | `int` | Zeilenlimit pro Sheet, Default `SpreadsheetVersion.EXCEL2007.getMaxRows()` (1.048.576). Nur über den paketinternen Test-Seam `maxRowsPerSheet(int)` änderbar. | entfällt (primitiv) |
| `consumed` | `boolean` | Single-Use-Flag: `true`, sobald `renderInto(...)` zu schreiben begonnen hat. Default `false`. | entfällt (primitiv) |
| `dataProvider` | `DataProvider<T>` | Datenquelle dieses Sheets, gesetzt via `data(DataProvider)`. | ja bis zum Aufruf von `data(...)` (Default `null`); `renderInto()` wirft `IllegalStateException`, falls beim Rendern noch `null` |

# Thread-Safety

**Nicht thread-sicher, Single-Use** (verifiziert): für einmalige Nutzung gedacht — neue
Instanz pro Job/Request, nicht zwischen Threads teilen. Ein zweiter Render-/Schreibversuch
(`renderInto`, intern über `WorkbookBuilder.write`) wirft `IllegalStateException`, weil die
Datenquelle vorwärts-lesbar/single-use ist. Die External Merge Sort puffert
`sortChunkSize(int)` Zeilen pro Sortierung im Speicher — bei vielen gleichzeitigen Jobs
summiert sich das (Dokumentation im Code verifiziert). Der übergebene `DataProvider` darf
ebenfalls nicht zwischen Threads geteilt werden. Siehe
[Concurrency contract](/architecture/concurrency-contract.md).

# Serialisierung

Nicht `Serializable` — `XlsxBuilder<T>` implementiert kein Serialisierungs-Interface
(verifiziert gegen die Klassendeklaration `public final class XlsxBuilder<T>`). Kein
Serialisierungs-Vertrag.

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben (verifiziert: keine `equals`/`hashCode`/`toString`-
Deklaration im Quellcode) — es gilt die **Identitätssemantik von `java.lang.Object`**
(`==`-Vergleich, identitätsbasierter Hashcode, `toString()` liefert
Klassenname+Hashcode). Da die Klasse ohnehin Single-Use/nicht für Sammlungen mit
Werte-Gleichheit gedacht ist, hat das in der Praxis geringe Relevanz, wird hier aber
explizit als verifizierter Befund festgehalten.

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `public final class XlsxBuilder<T>` — keine
`extends`-/`implements`-Klausel; erweitert implizit nur `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, daher nicht
erweiterbar. Wird von [WorkbookBuilder](/api-reference/workbook-builder/workbook-builder.md)
`.sheet(XlsxBuilder<?>)` als Feldtyp (`List<XlsxBuilder<?>>`) referenziert —
Komposition/Aggregation, keine Vererbungsbeziehung.

# Konstruktoren

- [`private XlsxBuilder()`](./constructor.md) — leerer privater Konstruktor, Instanzen nur über `create()`.

# Methoden

- [`static <T> XlsxBuilder<T> create()`](./create.md) — Fabrikmethode, einziger Weg eine Instanz zu erzeugen.
- [`sheetName(String name)`](./sheet-name.md) — setzt den Sheet-Namen.
- [`header(String... lines)`](./header.md) — Titelzeile(n) über den Spaltenköpfen.
- [`columnGroups(List<ColumnGroup> groups)`](./column-groups.md) — gruppierte Kopfzeile.
- [`column(String name, Function<? super T, ?> extractor)`](./column.md) — definiert eine Spalte.
- [`ofType(ColumnType type)`](./of-type.md) — Typ der zuletzt definierten Spalte.
- [`formatForType(String format)`](./format-for-type.md) — Excel-Formatcode der zuletzt definierten Spalte.
- [`nullText(String text)`](./null-text.md) — `null`-Platzhalter der zuletzt definierten Spalte.
- [`convertToColumnType(Function<R, ?> converter)`](./convert-to-column-type.md) — Wertkonverter der zuletzt definierten Spalte.
- [`sortBy(String columnName, SortOrder order)`](./sort-by.md) — Sortierstufe.
- [`sumColumn(String columnName)`](./sum-column.md) — markiert Spalte zur Summierung.
- [`summaryLabel(String columnName, String text)`](./summary-label.md) — Label der Summenzeile.
- [`summaryAsFormula(boolean useFormula)`](./summary-as-formula.md) — Summenzeile als Formel oder Wert.
- [`columnHeaders(boolean show)`](./column-headers.md) — Spaltenkopfzeile ein/aus.
- [`splitOnRowLimit(boolean enabled)`](./split-on-row-limit.md) — Verhalten bei Zeilenlimit.
- [`splitSheetNamer(SplitSheetNamer namer)`](./split-sheet-namer.md) — Benennung der Folge-Sheets.
- [`maxRowsPerSheet(int maxRows)`](./max-rows-per-sheet.md) — paketinterner Test-Seam für das Zeilenlimit.
- [`sortChunkSize(int chunkSize)`](./sort-chunk-size.md) — Chunk-Größe der External Merge Sort.
- [`sortTempDir(Path dir)`](./sort-temp-dir.md) — Basisverzeichnis für Sortier-Temp-Dateien.
- [`applyDefaultTempDir(Path defaultDir)`](./apply-default-temp-dir.md) — paketintern, Default-Temp-Verzeichnis vom `WorkbookBuilder`.
- [`filter(Predicate<? super T> predicate)`](./filter.md) — Filter auf Rohdatensätze.
- [`defaultNullText(String text)`](./default-null-text.md) — Sheet-weiter `null`-Platzhalter.
- [`footer(String... lines)`](./footer.md) — Fußzeile(n).
- [`placeholder(String key, String value)`](./placeholder.md) — einzelner Platzhalter.
- [`placeholderResolver(Function<String, String> resolver)`](./placeholder-resolver.md) — lazy/berechnete Platzhalter.
- [`placeholders(Map<String, String> values)`](./placeholders.md) — mehrere Platzhalter auf einmal.
- [`parallel(boolean enabled)`](./parallel.md) — Pipeline-Parallelität ein/aus.
- [`data(DataProvider<T> provider)`](./data.md) — setzt die Datenquelle.
- [`renderInto(SXSSFWorkbook wb)`](./render-into.md) — paketintern, rendert das Sheet.
- [`closeUnconsumedProvider()`](./close-unconsumed-provider.md) — paketintern, schließt nie konsumierte Datenquelle.
- [`buildLayout()`](./build-layout.md) — privat, baut Layout-Optionen.
- [`validatedColumnGroups()`](./validated-column-groups.md) — privat, prüft Spaltengruppen-Spans.
- [`buildSummarySpec()`](./build-summary-spec.md) — privat, baut Summenzeilen-Konfiguration.
- [`indexOf(String columnName)`](./index-of.md) — privat, Spaltenindex-Lookup.
- [`isNumeric(ColumnType type)`](./is-numeric.md) — privat, Typ-Klassifizierung für Summierbarkeit.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java`
[2] [XlsxBuilder (Komponente)](/components/xlsx-builder.md)
[3] [Konfigurationsobjekte](/components/configuration-models.md)
[4] [Concurrency contract](/architecture/concurrency-contract.md)
