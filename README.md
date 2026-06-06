# XLSBuilder

Eine schlanke Java-21-Bibliothek zum Erzeugen von **`.xlsx`-Dateien** über ein fluentes
**Builder-Pattern** – mit Sortierung, Summenzeilen, Formaten, Formeln und **mehreren Worksheets**.
Im Mittelpunkt steht die **Out-of-core-Verarbeitung**: Datenmengen, die nicht in den Speicher passen,
werden gestreamt geschrieben und (falls nötig) per External Merge Sort sortiert.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)

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
- **Titel-/Fußzeilen** – optionale, über die Tabellenbreite zusammengeführte Kopf-/Footer-Texte mit
  `{platzhaltern}` (inkl. `{date}`, `{rowCount}`, `{sum:Spalte}`).
- **CSV-Export** – dasselbe Blatt streamend als CSV (RFC 4180, konfigurierbar) statt xlsx.
- **Automatische Spaltenbreiten** – inhaltsbasiert, damit nichts als `#####` erscheint.

## Voraussetzungen

- **Java 21** (Gradle Toolchain)
- Abhängigkeiten (werden von Gradle gezogen): **Apache POI 5.4.0** (`poi-ooxml`), `log4j-core`
- Tests: **JUnit 5**

## Schnellstart

```java
import de.makno.xlsbuilder.builder.*;
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
| `footer(String...)` | optionale Fußzeile(n) unter Daten/Summe, je über die volle Breite zusammengeführt |
| `column(name, extractor)` | Spalte; Standardtyp **Text** |
| `.ofType(ColumnType)` | Typ der zuletzt definierten Spalte |
| `.formatForType(String)` | Excel-Format-Code der zuletzt definierten Spalte |
| `.convertToColumnType(fn)` | Rohwert der Spalte vor dem Schreiben umwandeln |
| `.nullText(String)` | Platzhalter der zuletzt definierten Spalte für `null`-Werte (überschreibt den Default) |
| `filter(Predicate<? super T>)` | nur passende Objekte schreiben (mehrfach = UND); vor Sortierung/Summe |
| `defaultNullText(String)` | sheet-weiter Platzhalter für `null`-Zellen (z. B. `"-"`); ohne Angabe leere Zelle |
| `sortBy(name, SortOrder)` | optionale (mehrstufige) Sortierung |
| `sortChunkSize(int)` | Zeilen pro In-memory-Run des External Merge Sort (Default 100 000) |
| `sortTempDir(Path)` | Basisverzeichnis für die Sortier-Temp-Dateien (Default `java.io.tmpdir`) |
| `columnHeaders(boolean)` | Spaltenüberschriften-Zeile schreiben (Default `true`) |
| `sumColumn(name)` | numerische Spalte summieren (aktiviert Summenzeile) |
| `summaryLabel(name, text)` | Label in der Summenzeile (z. B. „Summe") |
| `summaryAsFormula(boolean)` | `true` = `=SUMME(...)`-Formel, `false` (Default) = vorberechnet |
| `placeholder(key, value)` / `placeholders(Map)` | `{key}`-Platzhalter in Titel/Kopf/Footer |
| `placeholderResolver(Function<String,String>)` | Fallback für lazy/berechnete Platzhalter (statische Map hat Vorrang) |
| `parallel(boolean)` | Pipeline-Parallelität (lesen/sortieren ∥ schreiben); Default `false` |
| `data(DataProvider<T>)` | Datenquelle des Blatts (erforderlich) |
| `writeCsv(Path[, CsvOptions])` | dieses Blatt als CSV schreiben (statt über `WorkbookBuilder` als xlsx) |

**Platzhalter:** In `header(...)`/`footer(...)`-Texten werden `{key}` ersetzt – benutzerdefiniert via
`placeholder(...)`, eingebaut `{date}`/`{datetime}` (überschreibbar) und – nur im Footer –
`{rowCount}` sowie `{sum:Spaltenname}`. Unbekannte Tokens bleiben unverändert. Für lazy/berechnete
Werte (z. B. Versionsnummer, Benutzername) ergänzt `placeholderResolver(key -> ...)` einen Fallback,
der nur konsultiert wird, wenn die statische Map den Schlüssel nicht kennt (`null` ⇒ Token bleibt
stehen); die Auflösung erfolgt zur Schreibzeit nur für Titel/Kopf/Footer (out-of-core-neutral).

**Pipeline-Parallelität (`parallel(true)`):** Ein Hintergrund-Thread liest/sortiert, während der
aufrufende Thread schreibt (beschränkte Queue → weiterhin out-of-core). Lohnt nur, wenn die
Producer-Seite (langsame Remote-DB, schwere Konvertierungen) der Flaschenhals ist; bei
POI-dominierten Lasten bringt es nichts (POI schreibt single-threaded). Auf einem Multiuser-Server
besser **zwischen** Requests parallelisieren als hier einzuschalten.

### `DataProvider<T>` / `DataProviders`
Forward-only Datenquelle (wird genau einmal gelesen → streamingfähig). Adapter:

```java
DataProviders.ofIterable(list);
DataProviders.ofIterator(iterator);
DataProviders.ofStream(stream);     // Stream wird beim Schließen mitgeschlossen
DataProviders.ofResultSet(rs, mapper);  // JDBC-ResultSet streamend, ideal für große DB-Exporte
```

**JDBC:** `ofResultSet(ResultSet, ResultSetRowMapper<T>)` liest die Datenbank zeilenweise (forward-only)
und mappt jede Zeile via `mapper` auf `T`. `close()` schließt **nur das `ResultSet`** – `Statement`
und `Connection` verwaltet der Aufrufer (try-with-resources). `SQLException`s werden in eine
`DataAccessException` verpackt.

```java
try (Connection conn = dataSource.getConnection();
     Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
    st.setFetchSize(1_000);
    ResultSet rs = st.executeQuery("SELECT id, name, salary FROM employee");
    WorkbookBuilder.create()
        .sheet(ExcelBuilder.<Employee>create()
            .column("ID", Employee::id).ofType(ColumnType.LONG)
            .column("Name", Employee::name)
            .column("Gehalt", Employee::salary).ofType(ColumnType.DECIMAL)
            .data(DataProviders.ofResultSet(rs, r -> new Employee(
                r.getLong("id"), r.getString("name"), r.getBigDecimal("salary")))))
        .write(Path.of("export.xlsx"));   // schließt das ResultSet
}
```

Für andere Quellen einfach `DataProvider<T>` direkt implementieren (z. B. einen Datei-Reader), sodass
Datensätze lazy beim Abruf entstehen.

> **Tipp:** Kann die DB selbst sortieren (`ORDER BY`), das dort tun und `.sortBy()` weglassen – dann
> entfällt der External Merge Sort im Builder (kein Temp-File, weniger I/O).

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

### CSV-Export

Ein einzelnes Blatt lässt sich statt als xlsx auch als CSV schreiben (streamend, out-of-core; Filter,
Sortierung, Summenzeile, Footer und Platzhalter gelten ebenso):

```java
ExcelBuilder.<Employee>create()
    .column("Name", Employee::name)
    .column("Gehalt", Employee::salary).ofType(ColumnType.DECIMAL)
    .sortBy("Gehalt", SortOrder.DESC)
    .data(provider)
    .writeCsv(Path.of("export.csv"));                 // RFC 4180: Komma, UTF-8, CRLF

// konfigurierbar:
.writeCsv(Path.of("export.csv"), CsvOptions.excelGerman());            // ; + UTF-8-BOM
.writeCsv(out, CsvOptions.DEFAULT.withDelimiter('\t').withBom(true));  // frei kombinierbar
```

Hinweise: CSV ist einblättrig (für mehrere Blätter je Blatt eine Datei). Werte werden als Text
gerendert (Excel-Format-Codes greifen nicht); `FORMULA`-Spalten bleiben leer; die Summenzeile ist
immer vorberechnet. Felder mit Trennzeichen/Quote/Zeilenumbruch werden RFC-4180-konform gequotet.

## Bauen & Ausführen

```bash
./gradlew build          # kompiliert + führt alle Tests aus
./gradlew test           # nur Tests (JUnit 5)
./gradlew run            # Demo (erzeugt employees.xlsx)
./gradlew dbBenchmark    # SQL-Benchmark: H2 mit 1 Mio. Zeilen befüllen + streamend exportieren
./gradlew javadoc        # generiert die API-Dokumentation
```

### SQL-Benchmark (H2)

`dbBenchmark` befüllt eine eingebettete H2-Datenbank (`build/benchdb/`) einmalig mit Testdaten und
exportiert sie streamend über `DataProviders.ofResultSet` nach `.xlsx` – misst also DB-Streaming +
External Merge Sort + SXSSF zusammen. Läuft mit `-Xmx256m` (im Task gesetzt), um Out-of-core zu zeigen:

```bash
./gradlew dbBenchmark --args="1000000 build/employees-db.xlsx"
# Beispielmessung: 1.000.000 Zeilen DB -> 70 MB xlsx in ~17s, belegter Heap ~78 MB (max 256 MB)
```

Ein zweiter Lauf überspringt das Seeding (idempotent). H2 wird beim ersten Build einmalig geladen.

Demo mit Parametern und begrenztem Heap (zeigt Out-of-core):

```bash
./gradlew installDist
java -Xmx128m -cp "build/install/xlsbuilder/lib/*" de.makno.xlsbuilder.app.ExcelBuilderDemo 1000000 employees.xlsx
#        ^ Heap-Limit                                                       ^ Zeilen  ^ Ausgabedatei
```

### API-Dokumentation

Nach dem Bauen findet sich die Javadoc-Dokumentation unter:
```
build/docs/javadoc/index.html
```

Oder direkt generieren:
```bash
./gradlew javadoc
```

## Out-of-core / Benchmark

Der Speicherbedarf hängt an `sortChunkSize` + dem SXSSF-Fenster, **nicht** an der Zeilen- oder
Blattanzahl. Beispiel (Demo mit 3 Blättern, davon zwei mit je 1 Mio. Zeilen × 11 Spalten):

```
-Xmx128m, 1.000.000 Zeilen × 2 Blätter + Info
→ ~140 MB Ausgabedatei, belegter Heap ~17 MB
```

Mehr Blätter/Zeilen kosten v. a. Zeit und Plattenplatz (Temp-Dateien), kaum mehr Heap.

### Performance-Logging (für Entwickler)

Der Builder schreibt Messpunkte auf **DEBUG** über die Log4j2-API (Logger-Namen unter
`de.makno.xlsbuilder.builder`): je Blatt Zeilenzahl + Sortier-/Schreibphase, die External-Merge-Sort-
Kennzahlen (Zeilen, Runs, Vormerge-Pässe, Zeit, Temp-Verzeichnis) und die Gesamtzeit des Workbooks.
Im Normalbetrieb (Level ≥ INFO) entstehen **keine Ausgaben und kein nennenswerter Overhead**. Zum
Aktivieren das Log-Level der Anwendung für dieses Package auf `DEBUG` setzen, z. B. in `log4j2.xml`:

```xml
<Logger name="de.makno.xlsbuilder.builder" level="debug"/>
```

## Nebenläufigkeit / Server-Betrieb

Die Bibliothek hat **keinen geteilten oder statischen veränderlichen Zustand**. Nebenläufige Aufträge
laufen daher isoliert, solange jeder Thread **eigene** Builder-Instanzen verwendet:

- **Builder sind nicht thread-safe und single-use.** Pro Request `WorkbookBuilder.create()` /
  `ExcelBuilder.create()` neu erzeugen; eine Instanz nicht zwischen Threads teilen. Ein zweites
  Schreiben derselben Instanz (`write`/`writeCsv`) wirft eine `IllegalStateException` – die
  Datenquelle ist forward-only/einmalig.
- **Pro `write()` entsteht ein eigenes POI-Workbook.** Zwei Aufträge dürfen nicht gleichzeitig in
  dieselbe Datei schreiben (jeweils eigener `OutputStream`/`Path`).
- **`DataProvider` nicht teilen.** Forward-only, einmalig, pro Request eigene Quelle (z. B. eine
  eigene JDBC-`Connection` aus dem Pool); `close()` ruft der Builder selbst auf.
- **Speicher skaliert mit der Nebenläufigkeit.** Out-of-core begrenzt den Speicher *pro* Sortierung
  (`sortChunkSize` Zeilen + SXSSF-Fenster), aber bei *N* gleichzeitigen Sortierungen summiert sich
  das auf ~*N × sortChunkSize* Zeilen. Daher die Nebenläufigkeit begrenzen (Thread-Pool/`Semaphore`)
  und/oder `sortChunkSize` kleiner wählen.
- **Temp-Verzeichnis & OS-Limits.** Sortier-Runs liegen standardmäßig unter `java.io.tmpdir`; mit
  `ExcelBuilder.sortTempDir(Path)` lässt sich eine dedizierte Platte wählen. Je Sortierung sind bis
  zu 16 Run-Dateien gleichzeitig offen – `ulimit -n` und freien Plattenplatz entsprechend der
  erwarteten Nebenläufigkeit dimensionieren.

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

Dieses Projekt ist unter der [Apache License 2.0](LICENSE) lizenziert.
