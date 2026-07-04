# Graph Report - XLSBuilder  (2026-07-04)

## Corpus Check
- 51 files · ~30,005 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 591 nodes · 1775 edges · 46 communities (35 shown, 11 thin omitted)
- Extraction: 62% EXTRACTED · 38% INFERRED · 0% AMBIGUOUS · INFERRED: 676 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3850bc35`
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
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 39|Community 39]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]

## God Nodes (most connected - your core abstractions)
1. `XlsxBuilderTest` - 92 edges
2. `Test` - 86 edges
3. `XlsxBuilder` - 39 edges
4. `XlsxWriter` - 35 edges
5. `T` - 29 edges
6. `Grid` - 18 edges
7. `Column` - 15 edges
8. `List` - 13 edges
9. `String` - 12 edges
10. `String` - 12 edges

## Surprising Connections (you probably didn't know these)
- None detected - all connections are within the same source files.

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Out-of-core write pipeline** — builder_dataprovider_dataprovider, builder_externalmergesort_externalmergesort, readme_xlsxwriter, readme_outofcore_streaming [EXTRACTED 0.90]
- **Multi-level sort metadata** — readme_rowcomparator, readme_sortkey, readme_sortorder, builder_externalmergesort_externalmergesort [INFERRED 0.75]
- **Sheet configuration model** — builder_excelbuilder_excelbuilder, readme_column, builder_columntype_columntype, builder_dataprovider_dataprovider [EXTRACTED 0.85]

## Communities (46 total, 11 thin omitted)

### Community 0 - "Builder Test Suite"
Cohesion: 0.12
Nodes (7): Iterable, String, DataProvider, Test, XlsxBuilder, TempItem, XlsxBuilderTest

### Community 1 - "Fluent ExcelBuilder API"
Cohesion: 0.18
Nodes (11): Apache POI SXSSF Streaming, DataAccessException, DataProviders (Adapter Factory), External Merge Sort Algorithm, JDBC ResultSet Streaming, Out-of-core Streaming, Pipeline Parallelism, ResultSetRowMapper (+3 more)

### Community 2 - "External Merge Sort"
Cohesion: 0.09
Nodes (19): Closeable, CloseableIterator, Comparator, Node, PriorityQueue, Override, Override, T (+11 more)

### Community 3 - "XLSX Writer (POI/SXSSF)"
Cohesion: 0.12
Nodes (21): BigDecimal, CellStyle, ColumnWidthEstimator, CreationHelper, Cell, Column, ColumnGroup, ColumnType (+13 more)

### Community 4 - "XLSX Test Reader / Grid"
Cohesion: 0.10
Nodes (14): CellData, LocalDateTime, Runnable, DataProvider, List, Path, String, Cell (+6 more)

### Community 5 - "JDBC Adapter & DB Benchmark"
Cohesion: 0.14
Nodes (12): ResultSetRowMapper, DataProvider, Iterator, ResultSet, T, Predicate, ResultSet, String (+4 more)

### Community 6 - "Row Comparator & Summary"
Cohesion: 0.19
Nodes (9): SortKey, Column, List, Object, Override, Row, String, SuppressWarnings (+1 more)

### Community 7 - "Demo & Employee Data"
Cohesion: 0.06
Nodes (25): ColumnGroup, Predicate, R, Serializable, SplitSheetNamer, String, Object, Column (+17 more)

### Community 8 - "Row Codec (Serialization)"
Cohesion: 0.23
Nodes (6): DataInputStream, DataOutputStream, Object, Row, String, RowCodec

### Community 9 - "Column Definition"
Cohesion: 0.10
Nodes (20): API documentation, Architecture (overview), Build & run, Column types & formats, Concepts, Concurrency / server operation, `DataProvider<T>` / `DataProviders`, Eclipse (+12 more)

### Community 10 - "Prefetching Pipeline (Parallel)"
Cohesion: 0.20
Nodes (6): ColumnType, Function, Object, String, T, Column

### Community 11 - "Data Access Exception"
Cohesion: 0.33
Nodes (4): RuntimeException, String, Throwable, DataAccessException

### Community 12 - "Placeholder Resolution"
Cohesion: 0.16
Nodes (11): DbBenchmark, Connection, RenderJob, Path, String, DataProvider, Iterator, Row (+3 more)

### Community 13 - "ResultSet Row Mapper"
Cohesion: 0.28
Nodes (12): bundle_relative_link(), check_conformance(), main(), Warn-only OKF-Checks: type-Pflichtfeld, keine relativen ../-Links., Liest title/description/type aus dem YAML-Frontmatter (naiver Zeilen-Parser)., Bundle-root-absoluter Link gemaess OKF-Spec (Abschnitt 5.1)., Erzeugt den index.md-Inhalt fuer ein Verzeichnis (deterministisch sortiert)., Schreibt nur bei Aenderung (haelt Hook-Ausgabe und git status ruhig). (+4 more)

### Community 15 - "Column Type Enum"
Cohesion: 0.33
Nodes (4): Iterator, Override, Row, PrefetchingRowIterator

### Community 16 - "Sheet Write Options"
Cohesion: 0.21
Nodes (4): OutputStream, Path, XlsxBuilder, WorkbookBuilder

### Community 17 - "Sort Key"
Cohesion: 0.14
Nodes (10): EmployeeData, XlsxBuilderDemo, Employee, Info, SortOrder, DataProvider, ResultSet, XlsxBuilder (+2 more)

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

### Community 32 - "Community 32"
Cohesion: 0.33
Nodes (5): Citations, Contract, Examples, Overview, Schema

### Community 33 - "Community 33"
Cohesion: 0.40
Nodes (4): End-of-Session-Routine (Pflicht bei Code-/Architekturänderungen), Graph-First-Regel, Knowledge Base (graphify + OKF), Projekt-Konventionen - xlsxBuilder

### Community 34 - "Community 34"
Cohesion: 0.40
Nodes (4): Citations, Contract, Examples, Overview

### Community 35 - "Community 35"
Cohesion: 0.40
Nodes (3): IllegalStateException, String, RowLimitExceededException

### Community 36 - "Community 36"
Cohesion: 0.50
Nodes (3): Citations, Operational notes, Overview

### Community 37 - "Community 37"
Cohesion: 0.50
Nodes (3): Citations, Contract, Overview

### Community 38 - "Community 38"
Cohesion: 0.50
Nodes (3): Citations, Commands, Rules

## Knowledge Gaps
- **92 isolated node(s):** `String`, `String`, `String`, `Info`, `Override` (+87 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **11 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `XlsxBuilderTest` connect `Builder Test Suite` to `XLSX Test Reader / Grid`, `JDBC Adapter & DB Benchmark`, `Row Comparator & Summary`, `Demo & Employee Data`, `Row Codec (Serialization)`, `Prefetching Pipeline (Parallel)`?**
  _High betweenness centrality (0.041) - this node is a cross-community bridge._
- **Why does `XlsxBuilder` connect `Demo & Employee Data` to `Builder Test Suite`, `Placeholder Resolution`?**
  _High betweenness centrality (0.038) - this node is a cross-community bridge._
- **What connects `String`, `String`, `String` to the rest of the system?**
  _99 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Builder Test Suite` be split into smaller, more focused modules?**
  _Cohesion score 0.12191807416035721 - nodes in this community are weakly interconnected._
- **Should `External Merge Sort` be split into smaller, more focused modules?**
  _Cohesion score 0.09158186864014801 - nodes in this community are weakly interconnected._
- **Should `XLSX Writer (POI/SXSSF)` be split into smaller, more focused modules?**
  _Cohesion score 0.12109994711792703 - nodes in this community are weakly interconnected._
- **Should `XLSX Test Reader / Grid` be split into smaller, more focused modules?**
  _Cohesion score 0.10158730158730159 - nodes in this community are weakly interconnected._