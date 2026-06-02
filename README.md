# XLSBuilder

Eine schlanke Java-21-Bibliothek zum Erzeugen von **`.xlsx`-Dateien** über ein fluentes
**Builder-Pattern** – mit Sortierung, Summenzeilen, Formaten, Formeln und **mehreren Worksheets**.
Im Mittelpunkt steht die **Out-of-core-Verarbeitung**: Datenmengen, die nicht in den Speicher passen,
werden gestreamt geschrieben und (falls nötig) per External Merge Sort sortiert.

## Highlights

- **Builder-API** – Spalten, Sortierung, Summenzeile und Titel fluent zusammenstecken.
- **Out-of-core** – External Merge Sort (Auslagern auf Temp-Dateien) + Apache POI **SXSSF**-Streaming.
  Millionen Zeilen bei wenigen MB Heap (siehe Benchmark unten).
- **Mehrere Blätter** – ein `WorkbookBuilder` fasst beliebig viele `ExcelBuilder` zusammen; jedes Blatt
  hat seinen **eigenen Datentyp**.
- **Spaltentypen** – `STRING, INTEGER, LONG, DOUBLE, DECIMAL, BOOLEAN, DATE, DATETIME, TIME, FORMULA`.
- **Formate** – frei wählbare Excel-Format-Codes je Spalte (`#,##0.00 "€"`, `dd.mm.yyyy`, `hh:mm`, …).
- **Wert-Konverter** – Rohwerte vor dem Schreiben umwandeln (z. B. `int`-Sekunden → Uhrzeit).
- **Summenzeile** – vorberechnet **oder** als echte `=SUMME(...)`-Formel.
- **Titelzeilen** – optionale, über die Tabellenbreite zusammengeführte Überschriften.
- **Automatische Spaltenbreiten** – inhaltsbasiert, damit nichts als `#####` erscheint.

## Voraussetzungen

- **Java 21** (Gradle Toolchain)
- Abhängigkeiten (werden von Gradle gezogen): **Apache POI 5.4.0** (`poi-ooxml`), `log4j-core`
- Tests: **JUnit 5**

## Schnellstart

```java
import de.makno.xlsbuilder.component.*;
import java.nio.file.Path;
import java.util.List;

record Employee(String name, java.math.BigDecimal salary) {}

var data = List.of(
    new Employee("Alice", new java.math.BigDecimal("4200.00")),
    new Employee("Bob",   new java.math.BigDecimal("3800.50")));

WorkbookBuilder.create()
    .sheet(ExcelBuilder.<Employee>create()
        .sheetName("Mitarbeiter")
        .header("Mitarbeiterbericht")                                    // optionale Titelzeile
        .column("Name", Employee::name)                                  // Default: Text
        .column("Gehalt", Employee::salary)
            .ofType(ColumnType.DECIMAL).formatForType("#,##0.00 \"€\"")
        .sortBy("Gehalt", SortOrder.DESC)
        .sumColumn("Gehalt").summaryLabel("Name", "Summe")
        .summaryAsFormula(true)                                          // =SUMME(...) statt Festwert
        .data(DataProviders.ofIterable(data)))
    .write(Path.of("bericht.xlsx"));
```

## Konzepte

### `WorkbookBuilder`
Container für die Datei. Nimmt ein oder mehrere Blätter auf und schreibt sie gestreamt:

```java
WorkbookBuilder.create()
    .sheet(blattA)   // ExcelBuilder<TypA>
    .sheet(blattB)   // ExcelBuilder<TypB> – anderer Typ möglich
    .write(Path.of("report.xlsx"));   // oder write(OutputStream)
```

### `ExcelBuilder<T>` – ein Blatt
| Methode | Zweck |
|---|---|
| `sheetName(String)` | Blattname (eindeutig erzwungen; Duplikate erhalten ein Suffix) |
| `header(String...)` | optionale Titelzeile(n), je über die volle Breite zusammengeführt + zentriert |
| `column(name, extractor)` | Spalte; Standardtyp **Text** |
| `.ofType(ColumnType)` | Typ der zuletzt definierten Spalte |
| `.formatForType(String)` | Excel-Format-Code der zuletzt definierten Spalte |
| `.convertToColumnType(fn)` | Rohwert der Spalte vor dem Schreiben umwandeln |
| `sortBy(name, SortOrder)` | optionale (mehrstufige) Sortierung |
| `sortChunkSize(int)` | Zeilen pro In-memory-Run des External Merge Sort (Default 100 000) |
| `sumColumn(name)` | numerische Spalte summieren (aktiviert Summenzeile) |
| `summaryLabel(name, text)` | Label in der Summenzeile (z. B. „Summe") |
| `summaryAsFormula(boolean)` | `true` = `=SUMME(...)`-Formel, `false` (Default) = vorberechnet |
| `data(DataProvider<T>)` | Datenquelle des Blatts (erforderlich) |

### `DataProvider<T>` / `DataProviders`
Forward-only Datenquelle (wird genau einmal gelesen → streamingfähig). Adapter:

```java
DataProviders.ofIterable(list);
DataProviders.ofIterator(iterator);
DataProviders.ofStream(stream);     // Stream wird beim Schließen mitgeschlossen
```

Für echte Out-of-core-Fälle einfach `DataProvider<T>` direkt implementieren (z. B. über ein
JDBC-`ResultSet` oder einen Datei-Reader), sodass Datensätze lazy beim Abruf entstehen.

### Spaltentypen & Formate

| Typ | Erwarteter Wert | Beispiel-Format |
|---|---|---|
| `STRING` | beliebig (`toString`) | – |
| `INTEGER` / `LONG` | ganze Zahl | `#,##0` |
| `DOUBLE` / `DECIMAL` | `Number` / `BigDecimal` | `#,##0.00`, `0.00%`, `#,##0.00 "€"` |
| `BOOLEAN` | `boolean` | – |
| `DATE` | `LocalDate` / `LocalDateTime` / `Date` | `dd.mm.yyyy` |
| `DATETIME` | `LocalDateTime` / `Date` | `dd.mm.yyyy hh:mm` |
| `TIME` | `LocalTime` / `LocalDateTime` | `hh:mm:ss` |
| `FORMULA` | Formeltext ohne `=` | `{row}` = aktuelle Zeile, z. B. `"F{row}*0.1"` |

> Formate sind **Excel-Format-Codes** (nicht Java-`DateTimeFormatter`-Muster): `mm` = Monat im
> Datumskontext, `hh:mm:ss` für Zeit, `#`/`0` für Zahlen. Ohne Angabe gelten sinnvolle Standardformate
> für Datums-/Zeittypen; Zahlen erscheinen als „General".

### Wert-Konverter

```java
.column("Start", Task::sekunden).ofType(ColumnType.TIME)
    .convertToColumnType((Integer s) -> java.time.LocalTime.ofSecondOfDay(s))
```

Die Umwandlung greift bei der Projektion – also auch für Sortierung und Summenzeile. Den
Lambda-Parametertyp explizit angeben.

## Bauen & Ausführen

```bash
./gradlew build          # kompiliert + führt alle Tests aus
./gradlew test           # nur Tests (JUnit 5)
./gradlew run            # Demo (erzeugt employees.xlsx)
```

Demo mit Parametern und begrenztem Heap (zeigt Out-of-core):

```bash
./gradlew installDist
java -Xmx128m -cp "build/install/xlsbuilder/lib/*" de.makno.xlsbuilder.app.App 1000000 employees.xlsx
#        ^ Heap-Limit                                                       ^ Zeilen  ^ Ausgabedatei
```

## Out-of-core / Benchmark

Der Speicherbedarf hängt an `sortChunkSize` + dem SXSSF-Fenster, **nicht** an der Zeilen- oder
Blattanzahl. Beispiel (Demo mit 3 Blättern, davon zwei mit je 1 Mio. Zeilen × 11 Spalten):

```
-Xmx128m, 1.000.000 Zeilen × 2 Blätter + Info
→ ~140 MB Ausgabedatei, belegter Heap ~17 MB
```

Mehr Blätter/Zeilen kosten v. a. Zeit und Plattenplatz (Temp-Dateien), kaum mehr Heap.

## Architektur (Kurzüberblick)

```
DataProvider<T> → Projektion zu Row(Object[]) → [optional] External Merge Sort → POI SXSSF → .xlsx
```

| Klasse | Aufgabe |
|---|---|
| `WorkbookBuilder` | Datei-/Workbook-Lifecycle, fügt mehrere Blätter zusammen |
| `ExcelBuilder<T>` | Konfiguration **eines** Blatts + Projektion/Sortier-Orchestrierung |
| `Column<T>` | Name, Typ, Format, Extraktor, optionaler Konverter |
| `ColumnType` / `SortOrder` / `SortKey` | Typ-/Sortier-Metadaten |
| `RowComparator` | Vergleicht projizierte Zeilen (mehrstufig, null-sicher) |
| `ExternalMergeSort` | Sortierte Runs auf Temp-Dateien + k-way-Merge |
| `XlsxWriter` | Schreibt ein Blatt via Apache POI SXSSF (Streaming) |
| `DataProvider` / `DataProviders` | Datenquelle + Adapter |

## Eclipse

Import als **Existing Gradle Project** (Buildship). Encoding und Java-21-Compliance sind über die
versionierten `.settings/`-Dateien vorkonfiguriert (UTF-8 – wichtig wegen der Umlaute im Code).

## Lizenz

Noch nicht festgelegt.
