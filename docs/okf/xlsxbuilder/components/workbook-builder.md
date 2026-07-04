---
type: Library Component
title: WorkbookBuilder
description: Combines any number of XlsxBuilder sheets (each with its own data type) into one .xlsx file and owns the file/workbook lifecycle.
resource: src/main/java/de/makno/xlsxbuilder/WorkbookBuilder.java
tags: [component, builder, excel, workbook]
timestamp: '2026-07-04T17:45:00+02:00'
---

# Overview

`WorkbookBuilder.create()` owns the file/workbook lifecycle and combines multiple
[XlsxBuilder](/components/xlsx-builder.md) sheets — each sheet has its **own data type**.
`write(Path)` / `write(OutputStream)` renders all sheets. `tempDir(Path)` redirects the
external-merge-sort temp files for all sheets (per-sheet override:
`XlsxBuilder.sortTempDir(Path)`).

**Atomic write:** `write(Path)` first writes to a temp file (`*.part`) in the target directory
and moves it onto the target path only after a successful write — a failed export never leaves
a partial `.xlsx` behind and never clobbers a previously valid file. Note: POI's SXSSF row
spill lives process-wide under `java.io.tmpdir`; only the library's own sort runs are
relocatable via `tempDir`/`sortTempDir`.

# Examples

```java
WorkbookBuilder.create()
    .sheet(employeesSheet)   // XlsxBuilder<Employee>
    .sheet(ordersSheet)      // XlsxBuilder<Order>
    .write(Path.of("report.xlsx"));
```

# Contract

**Not thread-safe, single-use** (like all builders in this library); each `write()` creates
its own POI workbook — two jobs must never write to the same file concurrently. See
[Concurrency contract](/architecture/concurrency-contract.md).

# Citations

[1] [README - Concepts: WorkbookBuilder](https://github.com/MaKnoNet/xlsxBuilder/blob/main/README.md)
