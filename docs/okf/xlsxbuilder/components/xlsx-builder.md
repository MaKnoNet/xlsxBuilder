---
type: Library Component
title: XlsxBuilder<T>
description: Fluent configuration of one worksheet — columns, types, formats, converters, sorting, summary row, title/footer, grouped headers; execution is delegated to SheetRenderer.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [component, builder, excel, sheet]
timestamp: '2026-07-04T17:45:00+02:00'
---

# Overview

`XlsxBuilder.<T>create()` configures a **single sheet** with its own data type `T`: columns
(name + extractor), [column types & formats](/components/data-provider.md), value converters,
multi-level sorting, summary row (pre-computed **or** real `=SUM(...)` formula), optional
title/footer rows with `{placeholders}` (`{date}`, `{rowCount}`, `{sum:Column}`) and grouped
headers via `columnGroups(...)`. Execution (projection/sort/write) is delegated to
`SheetRenderer` — see [Out-of-core pipeline](/architecture/out-of-core-pipeline.md).

# Schema

| Class | Responsibility |
|---|---|
| `XlsxBuilder<T>` | configuration of one sheet |
| `Column<T>` | name, type, format, extractor, optional converter |
| `ColumnType` / `SortOrder` / `SortKey` / `ColumnGroup` | type/sort/header-group metadata |
| `SheetRenderer` | projection/sort/parallel orchestration + writing a sheet |

Supported `ColumnType`s: `STRING, INTEGER, LONG, DOUBLE, DECIMAL, BOOLEAN, DATE, DATETIME,
TIME, FORMULA`.

**Excel row limit:** a worksheet holds at most 1,048,576 rows. By default exceeding the limit
throws a `RowLimitExceededException` (fail fast, no partial file thanks to the atomic write in
[WorkbookBuilder](/components/workbook-builder.md)); with `splitOnRowLimit(true)` the table
transparently continues on follow-up sheets (`"Name (2)"`, ... — customizable via
`splitSheetNamer`).

# Examples

```java
XlsxBuilder.<Employee>create()
    .sheetName("Employees")
    .header("Employee report")
    .column("Name", Employee::name)
    .column("Salary", Employee::salary)
        .ofType(ColumnType.DECIMAL).formatForType("#,##0.00 \"EUR\"")
    .sortBy("Salary", SortOrder.DESC)
    .sumColumn("Salary").summaryLabel("Name", "Total")
    .summaryAsFormula(true)
    .data(DataProviders.ofIterable(data));
```

# Contract

**Not thread-safe, single-use** — create a fresh instance per request; a second `write` throws
`IllegalStateException`. See [Concurrency contract](/architecture/concurrency-contract.md).

# Citations

[1] [README - Concepts: XlsxBuilder](https://github.com/MaKnoNet/xlsxBuilder/blob/main/README.md)
