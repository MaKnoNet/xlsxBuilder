---
type: Library Component
title: DataProvider<T> und DataProviders
description: Forward-only, single-use data source abstraction with adapters for Iterable, Stream and JDBC ResultSet — the entry point for out-of-core exports.
resource: src/main/java/de/makno/xlsxbuilder/DataProviders.java
tags: [component, data, jdbc, streaming, interface]
timestamp: '2026-07-04T17:45:00+02:00'
---

# Overview

`DataProvider<T>` abstracts the row source; `DataProviders` provides the adapters
(`ofIterable`, `ofStream`, `ofResultSet(rs, mapper)`, ...). With `ofResultSet` the rows are
streamed straight from the database — the basis for constant-memory exports, see
[Out-of-core pipeline](/architecture/out-of-core-pipeline.md). The builder calls `close()`
itself; the caller keeps `Statement`/`Connection` in a `try-with-resources`.

`DataProvider` is also the seam consumed by downstream projects — e.g.
**VaadinExcelExport** (`de.makno.vaadinexcelexport`) feeds its grid exports through
`DataProviders.ofResultSet`.

# Contract

**Forward-only, single-use, do not share** — its own source per request (e.g. a dedicated
JDBC `Connection` from the pool). See
[Concurrency contract](/architecture/concurrency-contract.md).

# Supporting types

| Type | Role |
|---|---|
| `ResultSetRowMapper<T>` | `@FunctionalInterface` used by `DataProviders.ofResultSet`; maps only the columns of the *current* row (`rs.getString(...)` etc.) — the adapter itself calls `rs.next()` |
| `Row` | package-private projected row (already-extracted cell values, one per column); `Serializable` so `ExternalMergeSort` can spill whole runs to temp files; not copied — created once per record on the hot path |
| `CloseableIterator<T>` | `Iterator<T> & Closeable` that closes without a checked exception; holds resources such as open run files |
| `RenderJob<T>` | immutable execution job for one sheet — all building blocks compiled by `XlsxBuilder` (columns, filter, `dataProvider`, `sort`, `summary`, `layout`, `parallel`) that `SheetRenderer` needs to write; separates fluent configuration from execution |

# Citations

[1] [README - Concepts: DataProvider / DataProviders](https://github.com/MaKnoNet/xlsxBuilder/blob/main/README.md)
