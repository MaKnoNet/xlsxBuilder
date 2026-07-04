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
| `XlsxWriter` | writes a sheet via Apache POI SXSSF (streaming) |

Memory usage depends on `sortChunkSize` + the SXSSF window, **not** on the row or sheet
count. Benchmark: 1,000,000 rows x 2 sheets with `-Xmx128m` -> ~140 MB output file, used heap
~17 MB. More sheets/rows cost mainly time and disk space (temp files), barely more heap.

# Operational notes

- Sort runs go under `java.io.tmpdir` by default; redirect via `WorkbookBuilder.tempDir(Path)`
  or per sheet `XlsxBuilder.sortTempDir(Path)`.
- Up to 16 run files are open at once per sort — size `ulimit -n` and disk space according to
  the expected concurrency.
- **Performance logging:** measurement points at DEBUG via the Log4j2 API (logger names under
  `de.makno.xlsxbuilder`); at level >= INFO there is no output and no notable overhead.

# Citations

[1] [README - Out-of-core / benchmark, Architecture overview](https://github.com/MaKnoNet/xlsxBuilder/blob/main/README.md)
