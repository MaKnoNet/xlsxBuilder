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

# Konstruktoren

## `private XlsxWriter(SXSSFWorkbook wb, String sheetName, List<? extends Column<?>> columns, SummarySpec summary, SheetWriteOptions layout)`

Privater Konstruktor, nur über die statische Fabrikmethode `addSheet(...)` erreichbar.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `wb` | `SXSSFWorkbook` | nicht geprüft — wird sofort für `wb.getCreationHelper()` und `wb.createCellStyle()` verwendet; `null` löst sofort `NullPointerException` aus |
| `sheetName` | `String` | nicht geprüft im Konstruktor selbst; erst später in `uniqueSheetName(wb, sheetName)` behandelt `sheetName == null` explizit (`(sheetName == null \|\| sheetName.isBlank()) ? "Sheet1" : sheetName`) — also faktisch **erlaubt**, mit Fallback auf `"Sheet1"` |
| `columns` | `List<? extends Column<?>>` | nicht geprüft — `columns.size()` wird sofort in `buildColumnStyles`/`ColumnWidthEstimator` aufgerufen; `null` löst `NullPointerException` aus |
| `summary` | `SummarySpec` | **ja** — `null` bedeutet "keine Summenzeile" (explizit abgefragt in mehreren Methoden) |
| `layout` | `SheetWriteOptions` | nicht geprüft — `layout.showColumnHeaders()`/`layout.footerLines()` etc. werden sofort aufgerufen; `null` löst `NullPointerException` aus |

Initialisiert Spalten-Styles (einmal pro Spalte), Titel-/Fußzeilen-Styles, den
`ColumnWidthEstimator`, die Summen-Akkumulatoren sowie `firstReservedRowIndex` (0-basierter
Index der ersten für Summenzeile/Fußzeilen reservierten Zeile: `maxRowsPerSheet - trailerRows`).
Keine expliziten Exceptions außer den oben genannten impliziten `NullPointerException`n.

# Methoden

## `static int addSheet(SXSSFWorkbook wb, String sheetName, List<? extends Column<?>> columns, Iterator<Row> rows, SummarySpec summary, SheetWriteOptions layout)`

Fügt ein Arbeitsblatt zu einem bestehenden Workbook hinzu. Am Zeilenlimit schlägt das Sheet
entweder fehl oder splittet in Teil-Sheets.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `wb` | `SXSSFWorkbook` | siehe Konstruktor — praktisch nicht erlaubt |
| `sheetName` | `String` | faktisch erlaubt (Fallback `"Sheet1"`, siehe oben) |
| `columns` | `List<? extends Column<?>>` | praktisch nicht erlaubt |
| `rows` | `Iterator<Row>` | nicht geprüft — `rows.hasNext()` wird in `writeSheets` aufgerufen; `null` löst `NullPointerException` aus |
| `summary` | `SummarySpec` | **ja** |
| `layout` | `SheetWriteOptions` | praktisch nicht erlaubt |

Rückgabewert: Anzahl der über alle Teil-Sheets hinweg geschriebenen Datenzeilen (ohne
Titel-/Kopf-/Summenzeilen), primitiv `int`, nie `null` — für Performance-Logs.

Geworfene Exceptions: siehe `writeSheets` (delegiert vollständig dorthin nach Konstruktion).

## `private int writeSheets(Iterator<Row> rows)`

Streamt alle Datenzeilen, splittet bei konfiguriertem Zeilenlimit in Teil-Sheets.

Geworfene Exceptions:
- `RowLimitExceededException("Sheet '" + ... + "' exceeds the limit of " + ... + " rows per
  sheet (incl. title/header rows and reserved summary/footer rows). Use splitOnRowLimit(true)
  to continue on follow-up sheets.")`, wenn `rowNum >= firstReservedRowIndex` und
  `layout.splitOnRowLimit() == false` (verifiziert exakt).
- Alle Exceptions aus `startSheet()`/`writePrelude(sheet)` (siehe unten) propagieren
  unverändert.

Rückgabewert: `totalDataRows` (`int`), Anzahl aller geschriebenen Datenzeilen über alle
Teil-Sheets.

## `private SXSSFSheet startSheet()`

Startet ein (Teil-)Sheet: eindeutiger Name, Neuberechnungs-Flag, Registrierung für den
abschließenden Breiten-Pass.

Rückgabewert: `SXSSFSheet`, nie `null` (POI's `wb.createSheet(...)` liefert laut POI-Vertrag nie
`null`). Geworfene Exceptions: propagiert aus `partSheetName(partNumber)` (siehe unten) und aus
POI's `wb.createSheet(...)` (z. B. `IllegalArgumentException` bei einem ungültigen/doppelten
Namen — praktisch ausgeschlossen, da `partSheetName` bereits Eindeutigkeit sicherstellt).

## `private String partSheetName(int partNumber)`

Name eines Teil-Sheets: Teil 1 und das Default-Schema nutzen `uniqueSheetName` ("Name (2)", ...);
ein konfigurierter `SplitSheetNamer` benennt die Folge-Sheets selbst. Dessen Ergebnis wird
Excel-safe gemacht, aber bewusst **nicht** dedupliziert — ein Duplikat schlägt mit einer klaren
Exception fehl.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `partNumber` | `int` | primitiv, kein `null` möglich |

Rückgabewert: `String`, sheetsicherer Name, nie `null` bei erfolgreichem Rückgabepfad.

Geworfene Exceptions:
- `IllegalStateException("SplitSheetNamer returned no name for part " + partNumber + " of
  sheet '" + baseSheetName + "'")`, wenn der konfigurierte `SplitSheetNamer` `null` oder einen
  leeren/blanken Namen zurückgibt (verifiziert: `if (name == null || name.isBlank())`).
- `IllegalStateException("SplitSheetNamer returned the name '" + safeName + "' for part " +
  partNumber + " of sheet '" + baseSheetName + "', but a sheet with that name already exists in
  the workbook")`, wenn der (Excel-safe gemachte) Name bereits im Workbook existiert.

## `private int writePrelude(SXSSFSheet sheet)`

Schreibt die Zeilen oberhalb der Daten (Titelzeilen, Gruppenkopf, Spaltenköpfe). Schützt vor
einem Limit, das keinen Platz für Daten lässt.

Rückgabewert: `int`, erste Datenzeile (0-basiert).

Geworfene Exceptions: `IllegalStateException("maxRowsPerSheet=" + ... + " leaves no room for
data rows: " + ... + " title/header rows plus " + ... + " reserved summary/footer rows")`, wenn
bereits die Vorspann-Zeilen (Titel/Gruppen/Köpfe) das reservierte Zeilenkontingent erreichen —
laut Kommentar im Code nur mit dem Test-Seam (künstlich kleine Limits) praktisch erreichbar.

## `private static void enableFormulaRecalculationIfNeeded(SXSSFSheet sheet, List<? extends Column<?>> columns, SummarySpec summary)`

Aktiviert `sheet.setForceFormulaRecalculation(true)`, wenn mindestens eine Spalte vom Typ
`FORMULA` ist oder die Summenzeile als Formel geschrieben wird (`summary.useFormula()`), damit
Excel die Werte beim Öffnen neu berechnet (sie sind nicht gecacht). Keine Exceptions, kein
Rückgabewert.

## `private int writeTitleRows(SXSSFSheet sheet)`

Schreibt die optionalen Titelzeilen (jede über die volle Breite verschmolzen). Rückgabewert:
nächste freie Zeile. Löst über `Placeholders.resolve(...)` potenziell eine
`NullPointerException` aus, falls `layout.placeholders()` `null` wäre — praktisch ausgeschlossen,
da `SheetWriteOptions` diese Map im kompakten Konstruktor stets nicht-`null` erzwingt.

## `private int writeColumnGroups(SXSSFSheet sheet, int rowNum)`

Schreibt die optionale gruppierte Kopfzeile. Rückgabewert: unveränderter `rowNum`, wenn keine
Gruppen konfiguriert sind, sonst `rowNum + 1`. Keine eigenen Exceptions (die Validierung "Spans
decken alle Spalten ab" ist bereits vorgelagert in `XlsxBuilder.validatedColumnGroups()`
erfolgt).

## `private int writeColumnHeaders(SXSSFSheet sheet, int rowNum)`

Schreibt die Spaltenköpfe, falls `layout.showColumnHeaders()`. Rückgabewert: unveränderter
`rowNum`, wenn deaktiviert, sonst `rowNum + 1`. Keine Exceptions.

## `private static BigDecimal[] initSums(List<? extends Column<?>> columns, SummarySpec summary)`

Initialisiert die Summen-Akkumulatoren. Rückgabewert: `null`, wenn `summary == null`; sonst ein
`BigDecimal[]` der Größe `columns.size()`, wobei nur die als `sum`-markierten Indizes mit
`BigDecimal.ZERO` vorbelegt sind (übrige Einträge bleiben `null`). Keine Exceptions.

## `private void writeDataRow(SXSSFSheet sheet, Row dataRow, int rowNum)`

Schreibt eine Datenzeile, misst Spaltenbreiten und akkumuliert die Summen. Bei `null`-Zellwert:
spaltenspezifischer Platzhalter vor dem sheet-weiten Default; ohne Platzhalter wird explizit eine
leere Zelle vom Typ `CellType.BLANK` erzeugt (kein Überspringen). Keine eigenen Exceptions außer
denen, die aus `writeCell(...)` propagieren (siehe unten, z. B. bei nicht unterstützten
Datum-/Zeit-Typen).

## `private int writeSummaryRow(SXSSFSheet sheet, int rowNum, int totalDataRows)`

Schreibt die optionale Summenzeile (vorberechneter Wert oder echte `=SUM(...)`-Formel) auf das
letzte Teil-Sheet; die Summen decken die Datenzeilen **aller** Teil-Sheets ab.

Rückgabewert: unveränderter `rowNum`, wenn `summary == null`, sonst `rowNum + 1`.

Bemerkenswert (verifiziert): `asFormula = summary.useFormula() && totalDataRows > 0` — bei
**null Datenzeilen** wird selbst bei `useFormula(true)` kein `=SUM(...)` geschrieben, sondern der
vorberechnete Wert (der dann `0`/`BigDecimal.ZERO` ist) — eine Feinheit, die weder im Javadoc von
`XlsxBuilder.summaryAsFormula` noch in der Komponenten-Doku explizit erwähnt wird, aber
verhindert, dass eine Formel über einen leeren/nicht existierenden Bereich geschrieben wird.

Keine eigenen Exceptions (die Formel-Erzeugung selbst kann bei extremen Spalten-Indizes
theoretisch scheitern, aber `CellReference.convertNumToColString` ist für die durch
`XlsxBuilder` bereits auf 16.384 begrenzte Spaltenanzahl unproblematisch).

## `private String sumFormula(int columnIndex)`

Baut den `SUM(...)`-Ausdruck über alle Datenbereiche. Ohne Split bleibt das der einfache
Bereich auf demselben Sheet (`SUM(B3:B5)`); mit Split wird jeder Bereich mit seinem Sheet-Namen
qualifiziert (in Anführungszeichen, falls nötig), z. B. `SUM('Employees'!B3:B1048570,'Employees
(2)'!B2:B123)`. Keine Exceptions, reine String-Konstruktion.

## `private void writeFooterRows(SXSSFSheet sheet, int rowNum, int totalDataRows)`

Schreibt die optionalen Fußzeilen auf das letzte Teil-Sheet. Löst die dynamischen Platzhalter
`{rowCount}` und `{sum:Column}` mit den Summen über alle Teil-Sheets auf. Kein Rückgabewert,
keine eigenen Exceptions (No-Op, wenn keine Fußzeilen konfiguriert sind).

## `private static String sumAsText(ColumnType type, BigDecimal sum)`

Textrepräsentation einer Summe für `{sum:Column}`-Platzhalter.

Geworfene Exceptions: `ArithmeticException` (aus `BigDecimal.longValueExact()`), wenn `type`
`INTEGER`/`LONG` ist und die Summe den `long`-Wertebereich überschreitet oder einen
Nachkommaanteil hat — laut Kommentar im Code **bewusst** so belassen ("fails honestly instead of
being truncated silently"), nicht in einer separaten Javadoc dokumentiert, aber ein
sicherheitsrelevantes, verifiziertes Verhalten (kein stilles Abschneiden bei Überlauf).

## `private static String uniqueSheetName(SXSSFWorkbook wb, String sheetName)`

Erzeugt einen im Workbook eindeutigen, gültigen Sheet-Namen. `sheetName == null` oder
`isBlank()` fällt auf `"Sheet1"` zurück (verifiziert). Bei Namenskollision wird ein Suffix
`" (n)"` angehängt, ggf. wird der Basisname gekürzt, um die Excel-Obergrenze von 31 Zeichen
einzuhalten. Keine Exceptions (endlose Schleife theoretisch möglich, aber durch
`wb.getSheet(unique) != null`-Abbruchbedingung und monoton wachsendes `n` praktisch begrenzt
durch die tatsächliche Anzahl bereits existierender kollidierender Namen).

## `private static CellStyle[] buildColumnStyles(SXSSFWorkbook wb, CreationHelper helper, List<? extends Column<?>> columns)`

Erzeugt pro Spalte einmalig den Style mit Formatcode (oder `null`, wenn kein Format benötigt
wird). Keine Exceptions; POI selbst kann bei ungültigen Formatcodes intern fehlerhafte, aber
nicht exception-werfende Formate erzeugen (POI validiert Formatstrings nicht strikt).

## `private static void writeCell(org.apache.poi.ss.usermodel.Row row, int col, ColumnType type, Object value, CellStyle style)`

Schreibt einen einzelnen Zellwert je nach `ColumnType`.

Geworfene Exceptions (über `setDateValue`/`setTimeValue`):
- `IllegalArgumentException("Unsupported date type: " + value.getClass().getName())`, wenn
  `type` `DATE`/`DATETIME` ist und `value` weder `LocalDate`, `LocalDateTime` noch
  `java.util.Date` ist.
- `IllegalArgumentException("Unsupported time type: " + value.getClass().getName())`, wenn
  `type` `TIME` ist und `value` weder `LocalTime` noch `LocalDateTime` ist.
- `ClassCastException`, wenn `type` `INTEGER`/`LONG`/`DOUBLE` ist und `value` kein `Number` ist
  (Cast `((Number) value)` ohne vorherige `instanceof`-Prüfung) — **nicht dokumentiert**, aber
  durch den Code verifiziert: anders als bei DATE/TIME gibt es hier **keinen** sprechenden
  `IllegalArgumentException`-Pfad, sondern eine rohe `ClassCastException` mit
  Standard-JDK-Meldung.
- `NumberFormatException` (aus `new BigDecimal(value.toString())`), wenn `type == DECIMAL` und
  `value` weder `BigDecimal` noch in einen gültigen `BigDecimal`-String-Repräsentation
  konvertierbar ist.
- Bei `value == null` wird **keine** Zelle erzeugt (frühzeitiger Return "don't even create an
  empty cell") — abweichend vom Verhalten in `writeDataRow`, das für `null`-Werte explizit eine
  `CellType.BLANK`-Zelle erzeugt, wenn kein Null-Text konfiguriert ist. Diese Methode
  (`writeCell`) wird jedoch nur mit bereits als nicht-`null` bekannten Werten aufgerufen (aus
  `writeDataRow` und `writeSummaryRow`), sodass dieser Zweig in der Praxis nur als defensive
  Absicherung dient.

## `private static void setDateValue(Cell cell, Object value)` / `private static void setTimeValue(Cell cell, Object value)`

Siehe Exceptions oben (Teil von `writeCell`).

## `private static Object summaryValue(ColumnType type, BigDecimal sum)`

Wandelt eine akkumulierte Summe in den zum Spaltentyp passenden Java-Typ für `writeCell`/die
Breitenschätzung um.

Geworfene Exceptions: `ArithmeticException` (aus `BigDecimal.longValueExact()`), wenn `type`
`INTEGER`/`LONG` ist und die Summe nicht exakt in einen `long` passt — laut Kommentar bewusst
("only fails on a true long overflow — honestly, instead of truncating silently").

## `private static BigDecimal toBigDecimal(Object value)`

Wandelt einen Zellwert für die Summierung in `BigDecimal` um.

Geworfene Exceptions: `ClassCastException`, wenn `value` kein `Number` ist (finaler Zweig `return
BigDecimal.valueOf(((Number) value).longValue())` ohne vorherige Typprüfung) — nicht
dokumentiert, aber verifiziert; praktisch nur erreichbar, wenn eine als `sum`-markierte Spalte
einen nicht-numerischen Wert liefert (durch die Validierung in `XlsxBuilder.buildSummarySpec()`
eigentlich ausgeschlossen, da nur `INTEGER/LONG/DOUBLE/DECIMAL`-Spalten summierbar markiert
werden können — aber ein individueller Zellwert könnte durch einen Converter theoretisch dennoch
einen Nicht-`Number`-Typ liefern, da `convertToColumnType` laut eigener Doku keine
Laufzeitprüfung vornimmt).

## Innere Klasse `ColumnWidthEstimator` (private static final)

Schätzt Spaltenbreiten inhaltsbasiert, sodass nichts als `#####` angezeigt wird. Da SXSSF
Autosize gespillte Zeilen nicht sehen kann, wird die Breite während des Streamings gemessen.

### `ColumnWidthEstimator(List<? extends Column<?>> columns, boolean showColumnHeaders)`

Konstruktor, initialisiert Breiten-Arrays je Spalte aus Typ, Format und (falls aktiviert)
Spaltenname. Keine expliziten Null-Prüfungen; `columns == null` löst `NullPointerException`
über `columns.size()` aus.

### `void track(int c, Object value)`

Aktualisiert die geschätzte Breite einer Spalte anhand des konkret geschriebenen Werts. Bei
`value == null`: sofortiger Return, keine Änderung. Für `DATE`/`DATETIME`/`TIME`/`BOOLEAN`/
`FORMULA`: kein Tracking (Basisbreite genügt, `default -> return`). Geworfene Exceptions:
`ClassCastException` bzw. `NumberFormatException` indirekt über `integerDigits(value)`, wenn
`value` bei einem numerischen Typ kein `Number`/`BigDecimal` ist (analog zu `toBigDecimal`).

### `void ensureAtLeast(int c, int chars)`

Stellt sicher, dass eine Spalte mindestens `chars` Zeichen breit ist. Keine Exceptions; bei
`c` außerhalb des gültigen Arrayindex-Bereichs: `ArrayIndexOutOfBoundsException` (kein eigener
Bereichscheck).

### `void applyTo(SXSSFSheet sheet)`

Setzt die ermittelten Breiten auf das Sheet (Zeichen -> POI-Einheiten, mit Padding), begrenzt auf
`MAX_WIDTH_CHARS = 255` Zeichen (Excel-Obergrenze). Keine Exceptions.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/XlsxWriter.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
[3] [XlsxBuilder (Komponente)](/components/xlsx-builder.md)
