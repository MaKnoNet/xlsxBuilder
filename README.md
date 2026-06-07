# xlsxBuilder

A lean Java 21 library for generating **`.xlsx` files** through a fluent **builder pattern** – with
sorting, summary rows, formats, formulas and **multiple worksheets**. Its focus is **out-of-core
processing**: data sets that do not fit in memory are written streamed and (if needed) sorted via an
external merge sort.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)

## Highlights

- **Builder API** – assemble columns, sorting, summary row and titles fluently.
- **Out-of-core** – external merge sort (spilling to temp files) + Apache POI **SXSSF** streaming.
  Millions of rows with a few MB of heap (see the benchmark below).
- **Multiple sheets** – a `WorkbookBuilder` combines any number of `XlsxBuilder`s; each sheet has its
  **own data type**.
- **Column types** – `STRING, INTEGER, LONG, DOUBLE, DECIMAL, BOOLEAN, DATE, DATETIME, TIME, FORMULA`.
- **Formats** – freely choosable Excel format codes per column (`#,##0.00 "€"`, `dd.mm.yyyy`, `hh:mm`, …).
- **Value converters** – transform raw values before writing (e.g. `int` seconds → time of day).
- **Summary row** – pre-computed **or** as a real `=SUM(...)` formula.
- **Title/footer rows** – optional header/footer texts merged across the table width, with
  `{placeholders}` (incl. `{date}`, `{rowCount}`, `{sum:Column}`).
- **Automatic column widths** – content-based, so nothing shows up as `#####`.

## Requirements

- **Java 21** (Gradle toolchain)
- Dependencies (pulled by Gradle): **Apache POI 5.4.0** (`poi-ooxml`), `log4j-core`
- Tests: **JUnit 5**

## Quick start

```java
import de.makno.xlsxbuilder.builder.*;
import java.nio.file.Path;
import java.util.List;

record Employee(String name, java.math.BigDecimal salary) {}

var data = List.of(
    new Employee("Alice", new java.math.BigDecimal("4200.00")),
    new Employee("Bob",   new java.math.BigDecimal("3800.50")));

WorkbookBuilder.create()
    .sheet(XlsxBuilder.<Employee>create()
        .sheetName("Employees")
        .header("Employee report")                                       // optional title row
        .column("Name", Employee::name)                                  // default: text
        .column("Salary", Employee::salary)
            .ofType(ColumnType.DECIMAL).formatForType("#,##0.00 \"€\"")
        .sortBy("Salary", SortOrder.DESC)
        .sumColumn("Salary").summaryLabel("Name", "Total")
        .summaryAsFormula(true)                                          // =SUM(...) instead of a fixed value
        .data(DataProviders.ofIterable(data)))
    .write(Path.of("report.xlsx"));
```

## Concepts

### `WorkbookBuilder`
Container for the file. Takes one or more sheets and writes them streamed:

```java
WorkbookBuilder.create()
    .sheet(sheetA)   // XlsxBuilder<TypeA>
    .sheet(sheetB)   // XlsxBuilder<TypeB> – a different type is allowed
    .write(Path.of("report.xlsx"));   // or write(OutputStream)
```

**Temp/junk files (location):** the sort temp files (external merge sort) can be directed centrally
via `WorkbookBuilder.tempDir(Path)` for all sheets; a per-sheet `XlsxBuilder.sortTempDir(Path)` still
overrides this default. Both affect only the library's **own** sort runs. Apache POI's SXSSF temp
files (the row spill) live process-wide under `java.io.tmpdir`; POI offers no per-workbook,
multi-user-safe way to relocate them.

```java
WorkbookBuilder.create()
    .tempDir(Path.of("/fast-disk/xlsx-tmp"))   // default base directory for all sort runs
    .sheet(sheetA)
    .write(Path.of("report.xlsx"));
```

### `XlsxBuilder<T>` – a single sheet
| Method | Purpose |
|---|---|
| `sheetName(String)` | sheet name (forced unique; duplicates get a suffix) |
| `header(String...)` | optional title row(s), each merged across the full width + centered |
| `footer(String...)` | optional footer row(s) below data/summary, each merged across the full width |
| `column(name, extractor)` | column; default type **text** |
| `.ofType(ColumnType)` | type of the most recently defined column |
| `.formatForType(String)` | Excel format code of the most recently defined column |
| `.convertToColumnType(fn)` | transform the column's raw value before writing |
| `.nullText(String)` | placeholder of the most recently defined column for `null` values (overrides the default) |
| `filter(Predicate<? super T>)` | write only matching objects (repeated = AND); before sorting/summary |
| `defaultNullText(String)` | sheet-wide placeholder for `null` cells (e.g. `"-"`); empty cell if omitted |
| `sortBy(name, SortOrder)` | optional (multi-level) sorting |
| `sortChunkSize(int)` | rows per in-memory run of the external merge sort (default 100,000) |
| `sortTempDir(Path)` | base directory for the sort temp files (default `java.io.tmpdir`) |
| `columnHeaders(boolean)` | write the column-header row (default `true`) |
| `sumColumn(name)` | sum a numeric column (enables the summary row) |
| `summaryLabel(name, text)` | label in the summary row (e.g. "Total") |
| `summaryAsFormula(boolean)` | `true` = `=SUM(...)` formula, `false` (default) = pre-computed |
| `placeholder(key, value)` / `placeholders(Map)` | `{key}` placeholders in title/header/footer |
| `placeholderResolver(Function<String,String>)` | fallback for lazy/computed placeholders (static map takes precedence) |
| `parallel(boolean)` | pipeline parallelism (read/sort ∥ write); default `false` |
| `data(DataProvider<T>)` | the sheet's data source (required) |

**Placeholders:** in `header(...)`/`footer(...)` texts `{key}` is replaced – custom via
`placeholder(...)`, built-in `{date}`/`{datetime}` (overridable) and – only in the footer –
`{rowCount}` plus `{sum:ColumnName}`. Unknown tokens are left unchanged. For lazy/computed values
(e.g. a version number, a user name) `placeholderResolver(key -> ...)` adds a fallback that is
consulted only when the static map does not know the key (`null` ⇒ the token stays); resolution
happens at write time only for title/header/footer (out-of-core-neutral).

**Pipeline parallelism (`parallel(true)`):** a background thread reads/sorts while the calling thread
writes (a bounded queue → still out-of-core). Worth it only when the producer side (a slow remote DB,
heavy conversions) is the bottleneck; for POI-dominated workloads it brings nothing (POI writes
single-threaded). On a multi-user server, prefer parallelizing **between** requests rather than
enabling this individually.

### `DataProvider<T>` / `DataProviders`
Forward-only data source (read exactly once → streamable). Adapters:

```java
DataProviders.ofIterable(list);
DataProviders.ofIterator(iterator);
DataProviders.ofStream(stream);     // the stream is closed on close()
DataProviders.ofResultSet(rs, mapper);  // JDBC ResultSet streamed, ideal for large DB exports
```

**JDBC:** `ofResultSet(ResultSet, ResultSetRowMapper<T>)` reads the database row by row (forward-only)
and maps each row via `mapper` to `T`. `close()` closes **only the `ResultSet`** – the `Statement`
and `Connection` are managed by the caller (try-with-resources). `SQLException`s are wrapped in a
`DataAccessException`.

```java
try (Connection conn = dataSource.getConnection();
     Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
    st.setFetchSize(1_000);
    ResultSet rs = st.executeQuery("SELECT id, name, salary FROM employee");
    WorkbookBuilder.create()
        .sheet(XlsxBuilder.<Employee>create()
            .column("ID", Employee::id).ofType(ColumnType.LONG)
            .column("Name", Employee::name)
            .column("Salary", Employee::salary).ofType(ColumnType.DECIMAL)
            .data(DataProviders.ofResultSet(rs, r -> new Employee(
                r.getLong("id"), r.getString("name"), r.getBigDecimal("salary")))))
        .write(Path.of("export.xlsx"));   // closes the ResultSet
}
```

For other sources, simply implement `DataProvider<T>` directly (e.g. a file reader) so that records
are produced lazily on demand.

> **Tip:** if the DB can sort itself (`ORDER BY`), do it there and drop `.sortBy()` – then the external
> merge sort in the builder is skipped (no temp file, less I/O).

### Column types & formats

| Type | Expected value | Example format |
|---|---|---|
| `STRING` | anything (`toString`) | – |
| `INTEGER` / `LONG` | whole number | `#,##0` |
| `DOUBLE` / `DECIMAL` | `Number` / `BigDecimal` | `#,##0.00`, `0.00%`, `#,##0.00 "€"` |
| `BOOLEAN` | `boolean` | – |
| `DATE` | `LocalDate` / `LocalDateTime` / `Date` | `dd.mm.yyyy` |
| `DATETIME` | `LocalDateTime` / `Date` | `dd.mm.yyyy hh:mm` |
| `TIME` | `LocalTime` / `LocalDateTime` | `hh:mm:ss` |
| `FORMULA` | formula text without `=` | `{row}` = current row, e.g. `"F{row}*0.1"` |

> Formats are **Excel format codes** (not Java `DateTimeFormatter` patterns): `mm` = month in a date
> context, `hh:mm:ss` for time, `#`/`0` for numbers. Without one, sensible default formats apply to
> date/time types; numbers appear as "General".

### Value converters

```java
.column("Start", Task::seconds).ofType(ColumnType.TIME)
    .convertToColumnType((Integer s) -> java.time.LocalTime.ofSecondOfDay(s))
```

The conversion is applied at projection time – so it also affects sorting and the summary row. State
the lambda parameter type explicitly.

## Build & run

```bash
./gradlew build          # compiles + runs all tests
./gradlew test           # tests only (JUnit 5)
./gradlew run            # demo (creates employees.xlsx)
./gradlew dbBenchmark    # SQL benchmark: fill H2 with 1M rows + export streamed
./gradlew javadoc        # generates the API documentation
```

### SQL benchmark (H2)

`dbBenchmark` fills an embedded H2 database (`build/benchdb/`) once with test data and exports it
streamed via `DataProviders.ofResultSet` to `.xlsx` – i.e. it measures DB streaming + external merge
sort + SXSSF together. It runs with `-Xmx256m` (set in the task) to demonstrate out-of-core operation:

```bash
./gradlew dbBenchmark --args="1000000 build/employees-db.xlsx"
# Example measurement: 1,000,000 DB rows -> 70 MB xlsx in ~17s, used heap ~78 MB (max 256 MB)
```

A second run skips the seeding (idempotent). H2 is downloaded once on the first build.

Demo with parameters and a limited heap (demonstrates out-of-core):

```bash
./gradlew installDist
java -Xmx128m -cp "build/install/xlsxbuilder/lib/*" de.makno.xlsxbuilder.app.XlsxBuilderDemo 1000000 employees.xlsx
#        ^ heap limit                                                       ^ rows    ^ output file
```

### API documentation

After building, the Javadoc documentation is at:
```
build/docs/javadoc/index.html
```

Or generate it directly:
```bash
./gradlew javadoc
```

## Out-of-core / benchmark

Memory usage depends on `sortChunkSize` + the SXSSF window, **not** on the row or sheet count. Example
(demo with 3 sheets, two of them with 1M rows × 11 columns each):

```
-Xmx128m, 1,000,000 rows × 2 sheets + info
→ ~140 MB output file, used heap ~17 MB
```

More sheets/rows cost mainly time and disk space (temp files), barely more heap.

### Performance logging (for developers)

The builder writes measurement points at **DEBUG** via the Log4j2 API (logger names under
`de.makno.xlsxbuilder.builder`): per sheet the row count + sort/write phase, the external-merge-sort
metrics (rows, runs, pre-merge passes, time, temp directory) and the workbook's total time. In normal
operation (level ≥ INFO) there is **no output and no notable overhead**. To enable it, set the
application's log level for this package to `DEBUG`, e.g. in `log4j2.xml`:

```xml
<Logger name="de.makno.xlsxbuilder.builder" level="debug"/>
```

## Concurrency / server operation

The library has **no shared or static mutable state**. Concurrent jobs therefore run isolated, as long
as each thread uses its **own** builder instances:

- **Builders are not thread-safe and single-use.** Create `WorkbookBuilder.create()` /
  `XlsxBuilder.create()` fresh per request; do not share an instance between threads. Writing the same
  instance a second time (`write`) throws an `IllegalStateException` – the data source is
  forward-only/single-use.
- **Each `write()` creates its own POI workbook.** Two jobs must not write to the same file
  concurrently (each its own `OutputStream`/`Path`).
- **Do not share a `DataProvider`.** Forward-only, single-use, its own source per request (e.g. a
  dedicated JDBC `Connection` from the pool); the builder calls `close()` itself.
- **Memory scales with concurrency.** Out-of-core bounds the memory *per* sort (`sortChunkSize` rows +
  SXSSF window), but with *N* concurrent sorts this adds up to ~*N × sortChunkSize* rows. So limit
  concurrency (thread pool/`Semaphore`) and/or choose a smaller `sortChunkSize`.
- **Temp directory & OS limits.** Sort runs go under `java.io.tmpdir` by default; with
  `WorkbookBuilder.tempDir(Path)` (all sheets) or `XlsxBuilder.sortTempDir(Path)` (per sheet) you can
  pick a dedicated disk. Up to 16 run files are open at once per sort – size `ulimit -n` and free disk
  space according to the expected concurrency.

## Architecture (overview)

```
DataProvider<T> → projection to Row(Object[]) → [optional] external merge sort → POI SXSSF → .xlsx
```

| Class | Responsibility |
|---|---|
| `WorkbookBuilder` | file/workbook lifecycle, combines multiple sheets |
| `XlsxBuilder<T>` | configuration of **one** sheet (delegates execution to `SheetRenderer`) |
| `SheetRenderer` | projection/sort/parallel orchestration + writing a sheet |
| `Column<T>` | name, type, format, extractor, optional converter |
| `ColumnType` / `SortOrder` / `SortKey` | type/sort metadata |
| `RowComparator` | compares projected rows (multi-level, null-safe) |
| `ExternalMergeSort` | sorted runs on temp files + k-way merge |
| `XlsxWriter` | writes a sheet via Apache POI SXSSF (streaming) |
| `DataProvider` / `DataProviders` | data source + adapters |

## Eclipse

Import as an **Existing Gradle Project** (Buildship). Encoding and Java 21 compliance are preconfigured
via the versioned `.settings/` files (UTF-8).

## License

This project is licensed under the [Apache License 2.0](LICENSE).
