---
type: API Reference
title: XlsxBuilder
description: Oeffentlicher, fluenter, nicht thread-sicherer Single-Use-Builder fuer genau ein Sheet mit eigenem Datentyp T - Spalten, Sortierung, Summenzeile, Layout, Split-Handling und Parallelitaet.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, builder, excel, sheet]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`public final class XlsxBuilder<T>` — beschreibt genau **ein** Sheet inkl. seiner Datenquelle
(`.data(DataProvider)`). Narrative Gesamtbeschreibung, Feature-Übersicht und Beispielcode
bereits in [XlsxBuilder (Komponente)](/components/xlsx-builder.md) sowie
[Konfigurationsobjekte](/components/configuration-models.md) — diese Datei fokussiert auf die
vollständige, verifizierte Methoden-Ebene jeder einzelnen Konfigurationsmethode.

**Thread-Safety / Single-Use** (verifiziert): nicht thread-sicher, für einmalige Nutzung
gedacht — neue Instanz pro Job/Request, nicht zwischen Threads teilen. Ein zweiter
Render-/Schreibversuch (`renderInto`, intern über `WorkbookBuilder.write`) wirft
`IllegalStateException`, weil die Datenquelle vorwärts-lesbar/single-use ist. Siehe
[Concurrency contract](/architecture/concurrency-contract.md).

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `public final class XlsxBuilder<T>` — keine
`extends`-/`implements`-Klausel; erweitert implizit nur `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, daher nicht
erweiterbar. Wird von [WorkbookBuilder](/api-reference/workbook-builder.md)`.sheet(XlsxBuilder<?>)`
als Feldtyp (`List<XlsxBuilder<?>>`) referenziert — Komposition/Aggregation, keine
Vererbungsbeziehung.

# Konstruktoren

## `private XlsxBuilder()`

Leerer privater Konstruktor — Instanzen werden ausschließlich über die statische Fabrikmethode
`create()` erzeugt. Keine Parameter, keine Exceptions. Alle Felder werden mit Defaults
initialisiert, u. a. `sheetName = "Sheet1"`, `sortChunkSize = 100_000`,
`maxRowsPerSheet = SpreadsheetVersion.EXCEL2007.getMaxRows()` (1.048.576), `showColumnHeaders =
true`, `parallel = false`, `splitOnRowLimit = false`.

# Methoden

## `static <T> XlsxBuilder<T> create()`

Keine Parameter. Rückgabewert: neue `XlsxBuilder<T>`-Instanz, nie `null`. Keine Exceptions.

## `XlsxBuilder<T> sheetName(String name)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `name` | `String` | **nein** — `Objects.requireNonNull(name, "name")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException` bei
`name == null`.

## `XlsxBuilder<T> header(String... lines)`

Optionale Titelzeile(n) über den Spaltenköpfen; jede Zeile wird über die volle Tabellenbreite
verschmolzen und zentriert dargestellt. Wiederholter Aufruf hängt weitere Titelzeilen an.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `lines` | `String...` | ein explizit `null` übergebenes Array würde in der `for`-Schleife eine `NullPointerException` auslösen (Randfall bei Varargs); **jedes einzelne Element** `line` ist nicht erlaubt: `Objects.requireNonNull(line, "line")` pro Element |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn ein
Element von `lines` `null` ist.

## `XlsxBuilder<T> columnGroups(List<ColumnGroup> groups)`

Optionale gruppierte Kopfzeile über den Spaltenköpfen. Wiederholter Aufruf **ersetzt** die
Gruppen (nicht additiv — `columnGroups.clear()` vor dem Befüllen, verifiziert).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `groups` | `List<ColumnGroup>` | **nein** — `Objects.requireNonNull(groups, "groups")` |
| jedes Element von `groups` | `ColumnGroup` | **nein** — `Objects.requireNonNull(group, "group")` pro Element |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn `groups`
oder ein Element davon `null` ist. **Nicht** an dieser Stelle geprüft: ob die Summe der Spans
der Spaltenanzahl entspricht — das geschieht erst später in `validatedColumnGroups()` beim
Rendern (`IllegalArgumentException`, siehe `renderInto`).

## `XlsxBuilder<T> column(String name, Function<? super T, ?> extractor)`

Definiert eine Spalte, Default-Typ `STRING`. Excel erlaubt maximal 16.384 Spalten pro Sheet
(`A..XFD`); mehr definierte Spalten schlagen sofort fehl (fail-fast zur Konfigurationszeit,
bevor Daten gelesen/sortiert werden) — verifiziert.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `name` | `String` | **nein** — `Objects.requireNonNull(name, "name")` |
| `extractor` | `Function<? super T, ?>` | **nein** — `Objects.requireNonNull(extractor, "extractor")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
- `NullPointerException`, wenn `name` oder `extractor` `null` ist.
- `IllegalStateException("Column '" + name + "' exceeds Excel's limit of 16384 columns per
  sheet")`, wenn bereits `columns.size() >= SpreadsheetVersion.EXCEL2007.getMaxColumns()`
  Spalten definiert sind (verifiziert exakt).

## `XlsxBuilder<T> ofType(ColumnType type)`

Setzt den Typ der zuletzt definierten Spalte.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `type` | `ColumnType` | wird an `Column.withType(type)` weitergereicht, welches selbst `Objects.requireNonNull(type, "type")` prüft — **also indirekt nicht erlaubt** |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
- `IllegalStateException("ofType()/formatForType() requires a preceding column(...)")` über die
  private Hilfsmethode `lastColumn()`, wenn noch keine Spalte definiert wurde (`columns.isEmpty()`).
- `NullPointerException`, wenn `type == null` (aus `Column.withType`, nicht aus `XlsxBuilder`
  selbst — aber für den Aufrufer beobachtbar identisch).

## `XlsxBuilder<T> formatForType(String format)`

Setzt den Excel-Formatcode der zuletzt definierten Spalte.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `format` | `String` | **ja** — wird ungeprüft an `Column.withFormat(format)` weitergereicht, das selbst keine Null-Prüfung vornimmt |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
`IllegalStateException("ofType()/formatForType() requires a preceding column(...)")`, wenn noch
keine Spalte definiert wurde. Kein `NullPointerException` bei `format == null` (bewusst erlaubt,
bedeutet Default-Format des Typs).

## `XlsxBuilder<T> nullText(String text)`

Platzhalter für `null`-Werte der zuletzt definierten Spalte (überschreibt
`defaultNullText(String)`). `""` erzwingt eine leere Textzelle trotz konfiguriertem Default.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `text` | `String` | **ja** — ungeprüft an `Column.withNullText(text)` weitergereicht |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
`IllegalStateException("ofType()/formatForType() requires a preceding column(...)")` (dieselbe
Meldung wie bei `ofType`/`formatForType`, obwohl der Methodenname `nullText` lautet —
verifizierte kleine Ungenauigkeit: die Fehlermeldung nennt nicht die tatsächlich aufrufende
Methode; die Fehlerbedingung selbst — "muss nach column(...) kommen" — ist aber korrekt und
identisch für alle vier Konfiguratoren `ofType`/`formatForType`/`nullText`/
`convertToColumnType`, da sie dieselbe private `lastColumn()`-Hilfsmethode nutzen), wenn noch
keine Spalte definiert wurde.

## `<R> XlsxBuilder<T> convertToColumnType(Function<R, ?> converter)`

Optionaler Converter, der den extrahierten Rohwert der zuletzt definierten Spalte vor dem
Schreiben in die Ziel-Repräsentation transformiert. Wirkt sich auch auf Sortierung und
Summenzeile aus, da bereits zur Projektionszeit angewendet.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `converter` | `Function<R, ?>` | **nein** — `Objects.requireNonNull(converter, "converter")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
- `NullPointerException`, wenn `converter == null`.
- `IllegalStateException("ofType()/formatForType() requires a preceding column(...)")`, wenn
  noch keine Spalte definiert wurde.

**Wichtiger Javadoc-Hinweis, verifiziert korrekt:** es gibt bewusst **keine** automatische
Laufzeitprüfung auf verlustbehaftete Konvertierungen (z. B. `BigDecimal -> double`) —
Performance-Entscheidung auf dem Hot Path; der Aufrufer trägt die Verantwortung für
Präzisionsverlust.

## `XlsxBuilder<T> sortBy(String columnName, SortOrder order)`

Optionale Sortierstufe. Wiederholter Aufruf ergibt eine mehrstufige Sortierung.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `columnName` | `String` | **an dieser Stelle nicht geprüft** — `sortKeys.add(new SortKey(columnName, order))`; `SortKey`s kompakter Konstruktor prüft selbst `Objects.requireNonNull(columnName, "columnName")`, sodass `NullPointerException` indirekt entsteht |
| `order` | `SortOrder` | analog — indirekt über `SortKey`s `Objects.requireNonNull(order, "order")` nicht erlaubt |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException` (indirekt über
den `SortKey`-Konstruktor), wenn `columnName` oder `order` `null` ist. **Keine** Prüfung an
dieser Stelle, ob `columnName` eine tatsächlich existierende oder sortierbare Spalte bezeichnet
— das geschieht erst in `renderInto()` (`IllegalArgumentException("Unknown sort column: ...")`
bzw. "... cannot be sorted").

## `XlsxBuilder<T> sumColumn(String columnName)`

Markiert eine numerische Spalte zur Summierung; aktiviert die optionale Summenzeile.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `columnName` | `String` | **nein** — `Objects.requireNonNull(columnName, "columnName")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException` bei
`columnName == null`. Keine Prüfung an dieser Stelle, ob die Spalte existiert/numerisch ist —
das geschieht erst in `buildSummarySpec()` während `renderInto()`.

## `XlsxBuilder<T> summaryLabel(String columnName, String text)`

Optionales Label in der Summenzeile.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `columnName` | `String` | **nein** — `Objects.requireNonNull(columnName, "columnName")` |
| `text` | `String` | **nein** — `Objects.requireNonNull(text, "text")` |

**Verifizierte Präzisierung gegenüber einer möglichen Fehlerwartung:** der Javadoc-Kommentar
("Optional label ...") könnte suggerieren, dass `text` frei/optional (auch `null`) sein dürfte —
tatsächlich ist `text` **zwingend nicht-`null`**, sobald diese Methode überhaupt aufgerufen wird
(kein separater "Label entfernen"-Pfad vorhanden). Kein Widerspruch zur Doku, aber ein Detail,
das die Javadoc-Formulierung "Optional" nicht explizit klarstellt — hier ausdrücklich
festgehalten.

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`columnName` oder `text` `null` ist.

## `XlsxBuilder<T> summaryAsFormula(boolean useFormula)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `useFormula` | `boolean` | primitiv, kein `null` möglich |

Rückgabewert: `this`, nie `null`. Keine Exceptions.

## `XlsxBuilder<T> columnHeaders(boolean show)`

Steuert, ob die Spaltenkopfzeile geschrieben wird. Default `true`.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `show` | `boolean` | primitiv |

Rückgabewert: `this`, nie `null`. Keine Exceptions.

## `XlsxBuilder<T> splitOnRowLimit(boolean enabled)`

Steuert das Verhalten bei Überschreiten des Excel-Zeilenlimits: `false` (Default) wirft
`RowLimitExceededException` beim Schreiben; `true` setzt die Tabelle auf Folge-Sheets fort.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `enabled` | `boolean` | primitiv |

Rückgabewert: `this`, nie `null`. Diese Methode selbst wirft keine Exception (die
`RowLimitExceededException` fällt erst später beim tatsächlichen Schreiben in
`XlsxWriter.writeSheets`).

## `XlsxBuilder<T> splitSheetNamer(SplitSheetNamer namer)`

Optionale Benennung der Folge-Sheets, die durch `splitOnRowLimit(true)` entstehen.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `namer` | `SplitSheetNamer` | **nein** — `Objects.requireNonNull(namer, "namer")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`namer == null`. **Bemerkenswert:** anders als bei `placeholderResolver(Function)` (siehe unten)
gibt es hier keine Möglichkeit, den Namer explizit wieder auf "kein Namer" (`null`)
zurückzusetzen, da ein `null`-Argument sofort abgelehnt wird — konsistent mit der übrigen
`requireNonNull`-Praxis, aber im Unterschied zum internen `null`-Startzustand des Feldes
(`splitSheetNamer` ist intern `null`, bis diese Methode aufgerufen wird).

## `XlsxBuilder<T> sortChunkSize(int chunkSize)`

Chunk-Größe (Zeilen pro sortiertem Lauf im Speicher) der External Merge Sort.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `chunkSize` | `int` | primitiv; muss `>= 1` sein |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
`IllegalArgumentException("chunkSize must be >= 1")`, wenn `chunkSize < 1`.

## `XlsxBuilder<T> sortTempDir(Path dir)`

Optionales Basisverzeichnis für die temporären Sortierdateien. Nur wirksam bei aktiver
Sortierung.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `dir` | `Path` | **ja** — `null` (Default) = System-Temp; ungeprüft direkt übernommen |

Rückgabewert: `this`, nie `null`. Keine Exceptions.

## `void applyDefaultTempDir(Path defaultDir)` (paketintern)

Wendet ein workbook-weites Default-Sort-Temp-Verzeichnis an, sofern dieses Sheet noch kein
eigenes hat. Wird vom `WorkbookBuilder` vor dem Rendern aufgerufen.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `defaultDir` | `Path` | **ja** — bei `null` passiert nichts (Bedingung `defaultDir != null && sortTempDir == null`) |

Rückgabewert: `void`. Keine Exceptions.

## `XlsxBuilder<T> filter(Predicate<? super T> predicate)`

Optionaler Filter auf die Rohdatensätze; nur Objekte, für die das Prädikat `true` liefert,
werden geschrieben. Wird **vor** Projektion, Sortierung und Summierung angewendet. Wiederholter
Aufruf kombiniert die Prädikate mit UND.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `predicate` | `Predicate<? super T>` | **nein** — `Objects.requireNonNull(predicate, "predicate")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`predicate == null`.

## `XlsxBuilder<T> defaultNullText(String text)`

Sheet-weiter Platzhalter für `null`-Zellwerte.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `text` | `String` | **ja** — ungeprüft übernommen; `null` (Default) bedeutet: `null`-Zellen bleiben leer |

Rückgabewert: `this`, nie `null`. Keine Exceptions.

## `XlsxBuilder<T> footer(String... lines)`

Optionale Fußzeile(n) unterhalb der Daten, jede über die volle Breite verschmolzen.
Unterstützt Platzhalter inkl. dynamischer `{rowCount}` und `{sum:Column}`.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `lines` | `String...` | jedes Element: **nein** — `Objects.requireNonNull(line, "line")` pro Element |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn ein
Element von `lines` `null` ist.

## `XlsxBuilder<T> placeholder(String key, String value)`

Definiert einen Platzhalter `{key}`, der in Titel-, Kopf- und Fußzeilentexten ersetzt wird.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `key` | `String` | **nein** — `Objects.requireNonNull(key, "key")` |
| `value` | `String` | **nein** — `Objects.requireNonNull(value, "value")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn `key` oder
`value` `null` ist. **Bemerkenswert:** anders als der `placeholderResolver` (siehe unten), der
`null` als "kein Ersatzwert gefunden" nutzt, kann ein statischer Platzhalterwert hier **nicht**
`null` sein — konsistent mit `Placeholders.resolve`, das einen `null`-Ersatzwert als "nicht
gefunden" interpretieren würde (die Map speichert daher nur nicht-`null`-Werte).

## `XlsxBuilder<T> placeholderResolver(Function<String, String> resolver)`

Optionaler Resolver für lazy/berechnete Platzhalter (z. B. Versionsnummer, Benutzername),
konsultiert **nur**, wenn weder die statische Platzhalter-Map noch die eingebauten Platzhalter
den Schlüssel kennen.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `resolver` | `Function<String, String>` | **nein** — `Objects.requireNonNull(resolver, "resolver")` |

**Verifizierte Präzisierung:** der Parameter der `resolver`-Funktion selbst kann `null`
zurückgeben (dokumentiert: "If the resolver returns null, the token stays visible unchanged");
aber der `resolver`-**Funktionswert** (das `Function`-Objekt) darf beim Setzen nicht `null` sein
— zwei unterschiedliche Ebenen von "null-erlaubt", die in der Javadoc klar getrennt sind und
hier bestätigt werden.

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`resolver == null`. Wiederholter Aufruf ersetzt den vorherigen Resolver (kein additives
Verhalten, da nur ein Feld).

## `XlsxBuilder<T> placeholders(Map<String, String> values)`

Fügt mehrere Platzhalter auf einmal hinzu (delegiert intern an `placeholder(String, String)`
pro Eintrag über `values.forEach(this::placeholder)`).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `values` | `Map<String, String>` | **nein** — `Objects.requireNonNull(values, "values")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
- `NullPointerException`, wenn `values == null`.
- `NullPointerException`, wenn ein Schlüssel oder Wert in `values` selbst `null` ist (da jeder
  Eintrag durch `placeholder(key, value)` läuft, welches beide Parameter `requireNonNull`
  prüft) — nicht explizit im Javadoc dieser Methode erwähnt, aber durch die Delegation
  verifiziert.

## `XlsxBuilder<T> parallel(boolean enabled)`

Aktiviert die optionale Pipeline-Parallelität für dieses Sheet (Hintergrundthread liest/sortiert,
aufrufender Thread schreibt). Default `false`. Ergebnis ist **identisch** zum sequenziellen
Modus; Speicher bleibt Out-of-Core (begrenzte Queue).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `enabled` | `boolean` | primitiv |

Rückgabewert: `this`, nie `null`. Keine Exceptions.

## `XlsxBuilder<T> data(DataProvider<T> provider)`

Setzt die Datenquelle dieses Sheets. Erforderlich, bevor das Sheet geschrieben werden kann.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `provider` | `DataProvider<T>` | **nein** — `Objects.requireNonNull(provider, "provider")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`provider == null`.

## `void renderInto(SXSSFWorkbook wb) throws IOException` (paketintern)

Rendert dieses Sheet in ein bestehendes Workbook (aufgerufen vom `WorkbookBuilder`).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `wb` | `SXSSFWorkbook` | nicht geprüft in dieser Methode selbst — propagiert an `SheetRenderer.render(wb, job)`, wo ebenfalls kein Null-Check erfolgt; `null` führt zu einer späten `NullPointerException` innerhalb von `XlsxWriter` |

Rückgabewert: `void`. Geworfene Exceptions (Validierungsreihenfolge verifiziert):
1. `IllegalStateException("XlsxBuilder is single-use: already written - create a new instance
   per job (sheet: " + sheetName + ")")`, wenn `consumed == true` (bereits gerendert).
2. `IllegalStateException("At least one column must be defined")`, wenn `columns.isEmpty()`.
3. `IllegalStateException("No DataProvider set (.data(...)) for sheet: " + sheetName)`, wenn
   `dataProvider == null`.
4. Für jeden `SortKey`: `IllegalArgumentException("Unknown sort column: " +
   sortKey.columnName())`, wenn die Spalte nicht existiert; `IllegalArgumentException("Sort
   column '" + sortKey.columnName() + "' is of type " + type + " and cannot be sorted")`, wenn
   der Spaltentyp nicht sortierbar ist (`FORMULA`).
5. `buildSummarySpec()` (siehe unten) und `buildLayout()`/`validatedColumnGroups()` (siehe
   unten) werden aufgerufen und können `IllegalArgumentException` werfen.
6. **Erst danach** wird `consumed = true` gesetzt — verifizierte wichtige Design-Entscheidung
   (Kommentar im Code: "pure configuration errors must not masquerade as already written on a
   retry"): ein Konfigurationsfehler (Schritte 2–5) lässt die Instanz wiederverwendbar in einem
   neuen `WorkbookBuilder`, während ein Fehler **nach** Schritt 6 (während
   `SheetRenderer.render(...)`) die Instanz dauerhaft als verbraucht markiert.
7. Alle Exceptions aus `SheetRenderer.render(wb, job)` propagieren unverändert (u. a.
   `IOException`, `RowLimitExceededException`).

## `void closeUnconsumedProvider()` (paketintern)

Schließt die Datenquelle dieses Sheets, falls sie nie konsumiert wurde. Aufgerufen vom
`WorkbookBuilder` im Fehlerpfad, damit Provider von Sheets, die wegen eines früheren Fehlers nie
gerendert (und damit nie vom `SheetRenderer` geschlossen) wurden, nicht lecken.

Keine Parameter. Rückgabewert: `void`. No-Op, wenn `consumed == true` oder
`dataProvider == null`. Setzt **bewusst nicht** `consumed` (ein reiner Konfigurationsfehler soll
das Sheet in einem frischen `WorkbookBuilder` wiederverwendbar lassen). Geworfene Exceptions:
**keine** — ein `RuntimeException` aus `dataProvider.close()` wird gefangen und verschluckt
(`catch (RuntimeException ignored)`), best-effort, um eine bereits in Flug befindliche primäre
Exception nicht zu überdecken.

## `private SheetWriteOptions buildLayout()`

Baut die Layout-Optionen inkl. der statisch auflösbaren Platzhalter `{date}`/`{datetime}`
(`putIfAbsent`, überschreibt also keinen bereits vom Aufrufer gesetzten gleichnamigen
Platzhalter — verifiziert). Nicht von außen aufrufbar; hier dokumentiert, weil sie das
Verhalten von `renderInto` erklärt.

## `private List<ColumnGroup> validatedColumnGroups()`

Prüft, dass die Summe aller `ColumnGroup.span()`-Werte exakt der Spaltenanzahl entspricht.

Geworfene Exceptions: `IllegalArgumentException("Column groups span " + total + " columns but
there are " + columns.size())`, wenn die Summe nicht passt. Leere `columnGroups` liefern
`List.of()` ohne Prüfung (kein Fehler bei fehlenden Gruppen).

## `private SummarySpec buildSummarySpec()`

Baut die Summenzeilen-Konfiguration, oder `null`, wenn weder `sumColumn(...)` noch
`summaryLabel(...)` aufgerufen wurden.

Geworfene Exceptions:
- `IllegalArgumentException("Unknown sum column: " + name)`, wenn eine per `sumColumn(...)`
  benannte Spalte nicht existiert.
- `IllegalArgumentException("Sum column is not numeric: " + name)`, wenn die Spalte nicht vom
  Typ `INTEGER`/`LONG`/`DOUBLE`/`DECIMAL` ist (verifiziert gegen `isNumeric(ColumnType)`).
- `IllegalArgumentException("Unknown label column: " + summaryLabelColumn)`, wenn die per
  `summaryLabel(...)` benannte Spalte nicht existiert.

## `private int indexOf(String columnName)` / `private static boolean isNumeric(ColumnType type)`

Reine interne Hilfsmethoden ohne eigene Validierung; `indexOf` liefert `-1` bei keinem Treffer
(kein Werfen einer Exception), `isNumeric` liefert `true` für `INTEGER, LONG, DOUBLE, DECIMAL`,
sonst `false` (`switch`-Ausdruck mit `default -> false`).

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java`
[2] [XlsxBuilder (Komponente)](/components/xlsx-builder.md)
[3] [Konfigurationsobjekte](/components/configuration-models.md)
[4] [Concurrency contract](/architecture/concurrency-contract.md)
