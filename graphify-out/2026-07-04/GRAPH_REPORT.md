# Graph Report - .  (2026-06-07)

## Corpus Check
- Corpus is ~18,678 words - fits in a single context window. You may not need a graph.

## Summary
- 420 nodes · 1274 edges · 21 communities
- Extraction: 65% EXTRACTED · 35% INFERRED · 0% AMBIGUOUS · INFERRED: 450 edges (avg confidence: 0.8)
- Token cost: 42,102 input · 0 output

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

## God Nodes (most connected - your core abstractions)
1. `ExcelBuilderTest` - 61 edges
2. `Test` - 60 edges
3. `ExcelBuilder` - 46 edges
4. `XlsxWriter` - 27 edges
5. `T` - 26 edges
6. `Grid` - 18 edges
7. `ExternalMergeSort` - 14 edges
8. `String` - 12 edges
9. `BigDecimal` - 12 edges
10. `Column` - 11 edges

## Surprising Connections (you probably didn't know these)
- `ExcelBuilder` --references--> `XlsxWriter`  [INFERRED]
  src/main/java/de/makno/xlsbuilder/builder/ExcelBuilder.java → README.md
- `ExternalMergeSort` --implements--> `External Merge Sort Algorithm`  [EXTRACTED]
  src/main/java/de/makno/xlsbuilder/builder/ExternalMergeSort.java → README.md
- `ExternalMergeSort` --references--> `RowComparator`  [INFERRED]
  src/main/java/de/makno/xlsbuilder/builder/ExternalMergeSort.java → README.md
- `DataProviders (Adapter Factory)` --implements--> `DataProvider`  [INFERRED]
  README.md → src/main/java/de/makno/xlsbuilder/builder/DataProvider.java
- `ExcelBuilder` --implements--> `Fluent Builder API`  [EXTRACTED]
  src/main/java/de/makno/xlsbuilder/builder/ExcelBuilder.java → README.md

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Out-of-core write pipeline** — builder_dataprovider_dataprovider, builder_externalmergesort_externalmergesort, readme_xlsxwriter, readme_outofcore_streaming [EXTRACTED 0.90]
- **Multi-level sort metadata** — readme_rowcomparator, readme_sortkey, readme_sortorder, builder_externalmergesort_externalmergesort [INFERRED 0.75]
- **Sheet configuration model** — builder_excelbuilder_excelbuilder, readme_column, builder_columntype_columntype, builder_dataprovider_dataprovider [EXTRACTED 0.85]

## Communities (21 total, 0 thin omitted)

### Community 0 - "Builder Test Suite"
Cohesion: 0.15
Nodes (6): ExcelBuilderTest, Iterable, String, String, String, Test

### Community 1 - "Fluent ExcelBuilder API"
Cohesion: 0.06
Nodes (33): ColumnType, ExcelBuilder, RowSink, WorkbookBuilder, OutputStream, Predicate, R, Apache POI SXSSF Streaming (+25 more)

### Community 2 - "External Merge Sort"
Cohesion: 0.07
Nodes (26): CloseableIterator, DataProvider, ExternalMergeSort, MergeIterator, MergingIterator, RunReader, Closeable, CloseableIterator (+18 more)

### Community 3 - "XLSX Writer (POI/SXSSF)"
Cohesion: 0.13
Nodes (20): BigDecimal, ColumnWidthEstimator, XlsxWriter, CellStyle, ColumnWidthEstimator, CreationHelper, Cell, Column (+12 more)

### Community 4 - "XLSX Test Reader / Grid"
Cohesion: 0.12
Nodes (11): Grid, XlsxTestReader, CellData, LocalDateTime, List, Path, Cell, List (+3 more)

### Community 5 - "JDBC Adapter & DB Benchmark"
Cohesion: 0.12
Nodes (13): DbBenchmark, DataProviders, ResultSetDataProviderTest, Connection, ResultSetRowMapper, Path, String, DataProvider (+5 more)

### Community 6 - "Row Comparator & Summary"
Cohesion: 0.11
Nodes (14): Row, RowComparator, Serializable, SortKey, ColumnType, SummarySpec, Object, Column (+6 more)

### Community 7 - "Demo & Employee Data"
Cohesion: 0.14
Nodes (10): EmployeeData, ExcelBuilderDemo, Employee, Info, SortOrder, DataProvider, ExcelBuilder, ResultSet (+2 more)

### Community 8 - "Row Codec (Serialization)"
Cohesion: 0.27
Nodes (6): RowCodec, DataInputStream, DataOutputStream, Object, Row, String

### Community 9 - "Column Definition"
Cohesion: 0.20
Nodes (6): Column, ColumnType, Function, Object, String, T

### Community 10 - "Prefetching Pipeline (Parallel)"
Cohesion: 0.33
Nodes (4): PrefetchingRowIterator, Iterator, Override, Row

### Community 11 - "Data Access Exception"
Cohesion: 0.33
Nodes (4): DataAccessException, RuntimeException, String, Throwable

### Community 12 - "Placeholder Resolution"
Cohesion: 0.40
Nodes (3): Placeholders, Function, Map

### Community 13 - "ResultSet Row Mapper"
Cohesion: 0.50
Nodes (3): ResultSetRowMapper, ResultSet, T

## Knowledge Gaps
- **33 isolated node(s):** `String`, `String`, `String`, `Info`, `Override` (+28 more)
  These have ≤1 connection - possible missing edges or undocumented components.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ExcelBuilder` connect `Fluent ExcelBuilder API` to `Builder Test Suite`, `External Merge Sort`, `Row Comparator & Summary`?**
  _High betweenness centrality (0.235) - this node is a cross-community bridge._
- **Why does `ExternalMergeSort` connect `External Merge Sort` to `Fluent ExcelBuilder API`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Why does `DataProvider` connect `External Merge Sort` to `Fluent ExcelBuilder API`?**
  _High betweenness centrality (0.047) - this node is a cross-community bridge._
- **What connects `String`, `String`, `String` to the rest of the system?**
  _33 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Fluent ExcelBuilder API` be split into smaller, more focused modules?**
  _Cohesion score 0.05853174603174603 - nodes in this community are weakly interconnected._
- **Should `External Merge Sort` be split into smaller, more focused modules?**
  _Cohesion score 0.07407407407407407 - nodes in this community are weakly interconnected._
- **Should `XLSX Writer (POI/SXSSF)` be split into smaller, more focused modules?**
  _Cohesion score 0.12745098039215685 - nodes in this community are weakly interconnected._