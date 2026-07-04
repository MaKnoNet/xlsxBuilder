# Graph Report - XLSBuilder  (2026-07-04)

## Corpus Check
- 37 files · ~22,493 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 481 nodes · 1444 edges · 32 communities (29 shown, 3 thin omitted)
- Extraction: 62% EXTRACTED · 38% INFERRED · 0% AMBIGUOUS · INFERRED: 542 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b630decd`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- [[_COMMUNITY_Builder Test Suite|Builder Test Suite]]
- [[_COMMUNITY_Fluent ExcelBuilder API|Fluent ExcelBuilder API]]
- [[_COMMUNITY_External Merge Sort|External Merge Sort]]
- [[_COMMUNITY_XLSX Writer (POISXSSF)|XLSX Writer (POI/SXSSF)]]
- [[_COMMUNITY_XLSX Test Reader  Grid|XLSX Test Reader / Grid]]
- [[_COMMUNITY_JDBC Adapter & DB Benchmark|JDBC Adapter & DB Benchmark]]
- [[_COMMUNITY_Row Comparator & Summary|Row Comparator & Summary]]
- [[_COMMUNITY_Demo & Employee Data|Demo & Employee Data]]
- [[_COMMUNITY_Row Codec (Serialization)|Row Codec (Serialization)]]
- [[_COMMUNITY_Column Definition|Column Definition]]
- [[_COMMUNITY_Prefetching Pipeline (Parallel)|Prefetching Pipeline (Parallel)]]
- [[_COMMUNITY_Data Access Exception|Data Access Exception]]
- [[_COMMUNITY_Placeholder Resolution|Placeholder Resolution]]
- [[_COMMUNITY_ResultSet Row Mapper|ResultSet Row Mapper]]
- [[_COMMUNITY_Column Type Enum|Column Type Enum]]
- [[_COMMUNITY_Sheet Write Options|Sheet Write Options]]
- [[_COMMUNITY_Sort Key|Sort Key]]
- [[_COMMUNITY_Sort Order|Sort Order]]
- [[_COMMUNITY_Summary Spec|Summary Spec]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 24|Community 24]]
- [[_COMMUNITY_Community 25|Community 25]]

## God Nodes (most connected - your core abstractions)
1. `XlsxBuilderTest` - 67 edges
2. `Test` - 65 edges
3. `XlsxBuilder` - 34 edges
4. `XlsxWriter` - 29 edges
5. `T` - 25 edges
6. `Grid` - 18 edges
7. `String` - 12 edges
8. `List` - 12 edges
9. `BigDecimal` - 12 edges
10. `Column` - 11 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Out-of-core write pipeline** — builder_dataprovider_dataprovider, builder_externalmergesort_externalmergesort, readme_xlsxwriter, readme_outofcore_streaming [EXTRACTED 0.90]
- **Multi-level sort metadata** — readme_rowcomparator, readme_sortkey, readme_sortorder, builder_externalmergesort_externalmergesort [INFERRED 0.75]
- **Sheet configuration model** — builder_excelbuilder_excelbuilder, readme_column, builder_columntype_columntype, builder_dataprovider_dataprovider [EXTRACTED 0.85]

## Communities (32 total, 3 thin omitted)

### Community 0 - "Builder Test Suite"
Cohesion: 0.15
Nodes (8): Iterable, String, DataProvider, String, Test, XlsxBuilder, TempItem, XlsxBuilderTest

### Community 1 - "Fluent ExcelBuilder API"
Cohesion: 0.18
Nodes (11): Apache POI SXSSF Streaming, DataAccessException, DataProviders (Adapter Factory), External Merge Sort Algorithm, JDBC ResultSet Streaming, Out-of-core Streaming, Pipeline Parallelism, ResultSetRowMapper (+3 more)

### Community 2 - "External Merge Sort"
Cohesion: 0.09
Nodes (19): Closeable, CloseableIterator, Comparator, Node, PriorityQueue, Override, Override, T (+11 more)

### Community 3 - "XLSX Writer (POI/SXSSF)"
Cohesion: 0.09
Nodes (26): BigDecimal, CellStyle, ColumnWidthEstimator, CreationHelper, Serializable, Object, SummarySpec, Cell (+18 more)

### Community 4 - "XLSX Test Reader / Grid"
Cohesion: 0.12
Nodes (11): CellData, LocalDateTime, List, Path, Cell, List, Path, String (+3 more)

### Community 5 - "JDBC Adapter & DB Benchmark"
Cohesion: 0.11
Nodes (13): DbBenchmark, Connection, ResultSetRowMapper, Path, String, DataProvider, Iterator, ResultSet (+5 more)

### Community 6 - "Row Comparator & Summary"
Cohesion: 0.19
Nodes (9): SortKey, Column, List, Object, Override, Row, String, SuppressWarnings (+1 more)

### Community 7 - "Demo & Employee Data"
Cohesion: 0.07
Nodes (22): EmployeeData, Employee, Predicate, R, SortOrder, DataProvider, ResultSet, String (+14 more)

### Community 8 - "Row Codec (Serialization)"
Cohesion: 0.29
Nodes (6): DataInputStream, DataOutputStream, Object, Row, String, RowCodec

### Community 9 - "Column Definition"
Cohesion: 0.10
Nodes (19): API documentation, Architecture (overview), Build & run, Column types & formats, Concepts, Concurrency / server operation, `DataProvider<T>` / `DataProviders`, Eclipse (+11 more)

### Community 10 - "Prefetching Pipeline (Parallel)"
Cohesion: 0.21
Nodes (6): ColumnType, Function, Object, String, T, Column

### Community 11 - "Data Access Exception"
Cohesion: 0.33
Nodes (4): RuntimeException, String, Throwable, DataAccessException

### Community 12 - "Placeholder Resolution"
Cohesion: 0.31
Nodes (7): RenderJob, DataProvider, Iterator, Row, SXSSFWorkbook, T, SheetRenderer

### Community 13 - "ResultSet Row Mapper"
Cohesion: 0.28
Nodes (12): bundle_relative_link(), check_conformance(), main(), Warn-only OKF-Checks: type-Pflichtfeld, keine relativen ../-Links., Liest title/description/type aus dem YAML-Frontmatter (naiver Zeilen-Parser)., Bundle-root-absoluter Link gemaess OKF-Spec (Abschnitt 5.1)., Erzeugt den index.md-Inhalt fuer ein Verzeichnis (deterministisch sortiert)., Schreibt nur bei Aenderung (haelt Hook-Ausgabe und git status ruhig). (+4 more)

### Community 15 - "Column Type Enum"
Cohesion: 0.33
Nodes (4): Iterator, Override, Row, PrefetchingRowIterator

### Community 16 - "Sheet Write Options"
Cohesion: 0.22
Nodes (4): OutputStream, Path, XlsxBuilder, WorkbookBuilder

### Community 17 - "Sort Key"
Cohesion: 0.32
Nodes (4): XlsxBuilderDemo, Info, String, XlsxBuilder

### Community 18 - "Sort Order"
Cohesion: 0.40
Nodes (3): Function, Map, Placeholders

### Community 19 - "Summary Spec"
Cohesion: 0.50
Nodes (3): ResultSet, T, ResultSetRowMapper

### Community 21 - "Community 21"
Cohesion: 0.67
Nodes (3): ColumnType, Column, Value Converter (projection-time)

### Community 22 - "Community 22"
Cohesion: 0.67
Nodes (3): RowComparator, SortKey, SortOrder

## Knowledge Gaps
- **58 isolated node(s):** `String`, `String`, `String`, `Info`, `Override` (+53 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **3 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `XlsxBuilder` connect `Demo & Employee Data` to `Builder Test Suite`, `XLSX Writer (POI/SXSSF)`, `Placeholder Resolution`?**
  _High betweenness centrality (0.043) - this node is a cross-community bridge._
- **What connects `String`, `String`, `String` to the rest of the system?**
  _65 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Builder Test Suite` be split into smaller, more focused modules?**
  _Cohesion score 0.14773379231210557 - nodes in this community are weakly interconnected._
- **Should `External Merge Sort` be split into smaller, more focused modules?**
  _Cohesion score 0.09158186864014801 - nodes in this community are weakly interconnected._
- **Should `XLSX Writer (POI/SXSSF)` be split into smaller, more focused modules?**
  _Cohesion score 0.08737060041407868 - nodes in this community are weakly interconnected._
- **Should `XLSX Test Reader / Grid` be split into smaller, more focused modules?**
  _Cohesion score 0.11895161290322581 - nodes in this community are weakly interconnected._
- **Should `JDBC Adapter & DB Benchmark` be split into smaller, more focused modules?**
  _Cohesion score 0.11375661375661375 - nodes in this community are weakly interconnected._