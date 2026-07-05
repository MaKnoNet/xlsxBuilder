---
type: API Reference
title: XlsxWriter
description: Paketinterner Schreiber einer .xlsx-Datei mit Apache POI im Streaming-Modus (SXSSF) — Zeilenlimit-Handling mit Split, Summenzeile, Spaltenbreiten-Schätzung.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, poi, streaming, excel, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`final class XlsxWriter` — paketintern. Schreibt eine `.xlsx`-Datei mit Apache POI im
Streaming-Modus (SXSSF): nur ein gleitendes Zeilenfenster im Speicher, Rest wird auf Temp-Dateien
gespillt; mit Inline-Strings (SXSSF-Default) wächst auch keine Shared-Strings-Tabelle. Die Summen
der optionalen Summenzeile werden während des Streamings akkumuliert (kein zweiter Durchlauf).
Näher beschrieben in [Out-of-core pipeline](/architecture/out-of-core-pipeline.md) und
[XlsxBuilder (Komponente)](/components/xlsx-builder.md).

**Zeilenlimit:** ein Worksheet fasst höchstens `maxRowsPerSheet` Zeilen (Excel: 1.048.576),
inklusive Titel-/Gruppen-/Kopfzeilen sowie der für Summenzeile/Fußzeilen reservierten Zeilen.
Überschreitet die Datenmenge das, wird entweder eine `RowLimitExceededException` geworfen
(Default) oder — mit `splitOnRowLimit` — auf Folge-Sheets fortgesetzt.

**Eine Instanz schreibt ein logisches Sheet** (ein oder mehrere Teil-Sheets); die Instanz trägt
den Zustand, der sich über Teil-Sheets erstreckt (Styles, Breiten, Summen, Datenbereiche) —
nicht wiederverwendbar für ein zweites logisches Sheet.

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `LOG` | `private static final Logger` | Log4j2-Logger dieser Klasse. | nein |
| `NANOS_PER_DAY` | `private static final double` | Konstante `86_400d * 1_000_000_000d` — Nanosekunden pro Tag, für die Umrechnung von `LocalTime` in den Excel-Zeitbruchteil. | entfällt (primitiv) |
| `MAX_SHEET_NAME_LENGTH` | `private static final int` | Konstante `31` — maximale Länge eines Excel-Sheet-Namens. | entfällt (primitiv) |
| `wb` | `private final SXSSFWorkbook` | Ziel-Workbook. | nein — Konstruktor verwendet ihn sofort ohne Null-Check; `null` löst `NullPointerException` aus |
| `baseSheetName` | `private final String` | Konfigurierter Basis-Sheet-Name (vor Eindeutigmachung). | ja — faktisch erlaubt, Fallback auf `"Sheet1"` in `uniqueSheetName` |
| `columns` | `private final List<? extends Column<?>>` | Spaltendefinitionen. | nein — `columns.size()` wird sofort aufgerufen |
| `summary` | `private final SummarySpec` | Summenzeilen-Konfiguration. | ja — `null` bedeutet keine Summenzeile |
| `layout` | `private final SheetWriteOptions` | Layout-Parameter. | nein — Felder werden sofort gelesen |
| `columnStyles` | `private final CellStyle[]` | Pro Spalte einmalig erzeugter Zellstil (Formatcode). | Array-Einträge dürfen `null` sein (kein Format nötig) |
| `titleStyle` | `private final CellStyle` | Stil für Titelzeilen (fett, zentriert, 14pt). | nein |
| `footerStyle` | `private final CellStyle` | Stil für Fußzeilen (kursiv, zentriert, 10pt). | nein |
| `groupHeaderStyle` | `private CellStyle` | Stil für die gruppierte Kopfzeile. Lazy initialisiert (Kommentar im Code: "created only when column groups are configured"). | ja — `null`, solange keine Spaltengruppen konfiguriert sind |
| `widths` | `private final ColumnWidthEstimator` | Spaltenbreiten-Schätzer, siehe [ColumnWidthEstimator](./column-width-estimator.md). | nein |
| `sums` | `private final BigDecimal[]` | Summen-Akkumulatoren je Spalte. | ja — Feld selbst `null`, wenn `summary == null`; sonst Array mit `null`-Einträgen für nicht-summierte Spalten |
| `firstReservedRowIndex` | `private final int` | 0-basierter Index der ersten für Summenzeile/Fußzeilen reservierten Zeile. | entfällt (primitiv) |
| `sheets` | `private final List<SXSSFSheet>` | Alle bisher erzeugten Teil-Sheets (für den abschließenden Breiten-Pass). | Feld nie `null` (mit `new ArrayList<>()` initialisiert) |
| `dataRanges` | `private final List<DataRange>` | 1-basierte Excel-Zeilenbereiche je Teil-Sheet (für Cross-Sheet-`SUM`-Formeln). | Feld nie `null` |

`DataRange` ist ein privater, paketinterner `record DataRange(String sheetName, int
firstRowNum, int lastRowNum) {}` ohne eigenes `api-reference/`-Dokument (nested, nicht
separat exportiert) — Felder = Record-Komponenten.

# Thread-Safety

**Nicht thread-sicher, Single-Use** (aus dem Entwurf ersichtlich, nicht explizit im Javadoc
dokumentiert, aber verifiziert): eine Instanz trägt veränderlichen Zustand, der sich über
mehrere Teil-Sheets erstreckt (`sheets`, `dataRanges`, `sums`, `groupHeaderStyle`), ohne
`synchronized` oder sonstige Synchronisation. Für einmalige, sequenzielle Nutzung durch
`writeSheets(...)` (aufgerufen von genau einem Aufrufer-Thread) gedacht; **nicht**
wiederverwendbar für ein zweites logisches Sheet (siehe Überblick).

# Serialisierung

Nicht `Serializable` — `XlsxWriter` implementiert kein Serialisierungs-Interface (verifiziert:
`final class XlsxWriter`, keine `implements`-Klausel).

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben (verifiziert: keine `equals`/`hashCode`/`toString`-
Deklaration im Quellcode) — es gilt die Identitätssemantik von `java.lang.Object`. Als
paketinterne, zustandsbehaftete Single-Use-Klasse ist das die erwartete, unkritische
Konsequenz.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `final class XlsxWriter` — keine
`extends`-/`implements`-Klausel; erweitert implizit nur `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, daher nicht
erweiterbar. Die private Nested Class `ColumnWidthEstimator` ist ein eigenständiges Hilfsobjekt
ohne `extends`/`implements`-Klausel (keine eigene Vererbungsbeziehung, kein separates
`api-reference/`-Dokument, da paketprivat/nested). `XlsxWriter` referenziert
[Column](/api-reference/column/column.md), [Row](/api-reference/row/row.md),
[SummarySpec](/api-reference/summary-spec/summary-spec.md), [SheetWriteOptions](/api-reference/sheet-write-options/sheet-write-options.md)
und [SplitSheetNamer](/api-reference/split-sheet-namer/split-sheet-namer.md) als Feld-/Parametertypen — reine
Verwendung, keine Vererbung. Wirft [RowLimitExceededException](/api-reference/row-limit-exceeded-exception/row-limit-exceeded-exception.md),
das ist ebenfalls Verwendung, keine Vererbungsbeziehung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``static int addSheet(SXSSFWorkbook wb, String sheetName, List<? extends Column<?>> columns, Iterator<Row> rows, SummarySpec summary, SheetWriteOptions layout)``](./add-sheet.md)
- [``private int writeSheets(Iterator<Row> rows)``](./write-sheets.md)
- [``private SXSSFSheet startSheet()``](./start-sheet.md)
- [``private String partSheetName(int partNumber)``](./part-sheet-name.md)
- [``private int writePrelude(SXSSFSheet sheet)``](./write-prelude.md)
- [``private static void enableFormulaRecalculationIfNeeded(SXSSFSheet sheet, List<? extends Column<?>> columns, SummarySpec summary)``](./enable-formula-recalculation-if-needed.md)
- [``private int writeTitleRows(SXSSFSheet sheet)``](./write-title-rows.md)
- [``private int writeColumnGroups(SXSSFSheet sheet, int rowNum)``](./write-column-groups.md)
- [``private int writeColumnHeaders(SXSSFSheet sheet, int rowNum)``](./write-column-headers.md)
- [``private static BigDecimal[] initSums(List<? extends Column<?>> columns, SummarySpec summary)``](./init-sums.md)
- [``private void writeDataRow(SXSSFSheet sheet, Row dataRow, int rowNum)``](./write-data-row.md)
- [``private int writeSummaryRow(SXSSFSheet sheet, int rowNum, int totalDataRows)``](./write-summary-row.md)
- [``private String sumFormula(int columnIndex)``](./sum-formula.md)
- [``private void writeFooterRows(SXSSFSheet sheet, int rowNum, int totalDataRows)``](./write-footer-rows.md)
- [``private static String sumAsText(ColumnType type, BigDecimal sum)``](./sum-as-text.md)
- [``private static String uniqueSheetName(SXSSFWorkbook wb, String sheetName)``](./unique-sheet-name.md)
- [``private static CellStyle[] buildColumnStyles(SXSSFWorkbook wb, CreationHelper helper, List<? extends Column<?>> columns)``](./build-column-styles.md)
- [``private static void writeCell(org.apache.poi.ss.usermodel.Row row, int col, ColumnType type, Object value, CellStyle style)``](./write-cell.md)
- [``private static void setDateValue(Cell cell, Object value)` / `private static void setTimeValue(Cell cell, Object value)``](./set-date-value.md)
- [``private static Object summaryValue(ColumnType type, BigDecimal sum)``](./summary-value.md)
- [``private static BigDecimal toBigDecimal(Object value)``](./to-big-decimal.md)
- [Innere Klasse `ColumnWidthEstimator` (private static final)](./column-width-estimator.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/XlsxWriter.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
[3] [XlsxBuilder (Komponente)](/components/xlsx-builder.md)
