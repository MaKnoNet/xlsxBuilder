---
type: Architecture Concept
title: Out-of-core pipeline (external merge sort + SXSSF)
description: DataProvider -> Row projection -> optional external merge sort (temp-file runs, k-way merge) -> Apache POI SXSSF streaming. Memory depends on sortChunkSize + SXSSF window, not on row count.
resource: src/main/java/de/makno/xlsxbuilder/ExternalMergeSort.java
tags: [architecture, performance, streaming, sorting, poi]
timestamp: '2026-07-04T17:45:00+02:00'
---

# Overview

```
DataProvider<T> -> projection to Row(Object[]) -> [optional] external merge sort -> POI SXSSF -> .xlsx
```

| Class | Responsibility |
|---|---|
| `RowComparator` | compares projected rows (multi-level, null-safe) |
| `ExternalMergeSort` | sorted runs on temp files + k-way merge |
| `RowCodec` | compact, type-tagged (de)serialization of a `Row` for the EMS run files |
| `PrefetchingRowIterator` | overlaps read/sort with write via a bounded `BlockingQueue` |
| `XlsxWriter` | writes a sheet via Apache POI SXSSF (streaming) |

Memory usage depends on `sortChunkSize` + the SXSSF window, **not** on the row or sheet
count. Benchmark: 1,000,000 rows x 2 sheets with `-Xmx128m` -> ~140 MB output file, used heap
~17 MB. More sheets/rows cost mainly time and disk space (temp files), barely more heap.

## RowCodec: compact run-file serialization

Plain Java serialization of a `Row` would be wasteful for the millions of rows an
external merge sort spills to disk. `RowCodec` instead writes each value with an
explicit type tag (avoiding Java serialization's per-object overhead) — notably
floats are encoded compactly rather than boxed/serialized, which matters at EMS scale
where run files are read and written many times during the k-way merge.

## PrefetchingRowIterator: read/sort ∥ write

When `parallel` is enabled (see [RenderJob](/components/data-provider.md)), a single
daemon background thread pulls rows (projection/DB read + k-way merge) into a bounded
`ArrayBlockingQueue` while the consuming (writing) thread drains it — read/sort I/O and
POI writing overlap instead of running strictly sequentially. Only one extra thread per
sheet; the queue stays bounded so this remains out-of-core. `close()` interrupts the
producer and joins with a timeout, called via try-with-resources **before** the
sorter/data provider so nothing leaks.

# Operational notes

- Sort runs go under `java.io.tmpdir` by default; redirect via `WorkbookBuilder.tempDir(Path)`
  or per sheet `XlsxBuilder.sortTempDir(Path)`.
- Up to 16 run files are open at once per sort — size `ulimit -n` and disk space according to
  the expected concurrency.
- **Performance logging:** measurement points at DEBUG via the Log4j2 API (logger names under
  `de.makno.xlsxbuilder`); at level >= INFO there is no output and no notable overhead.

# Citations

[1] [README - Out-of-core / benchmark, Architecture overview](https://github.com/MaKnoNet/xlsxBuilder/blob/main/README.md)
