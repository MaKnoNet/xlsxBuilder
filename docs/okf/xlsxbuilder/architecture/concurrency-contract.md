---
type: Architecture Concept
title: Concurrency contract (server operation)
description: No shared or static mutable state in the library; builders and DataProviders are not thread-safe and single-use — one fresh instance per request; memory scales with concurrent sorts.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [architecture, concurrency, thread-safety, server]
timestamp: '2026-07-04T17:45:00+02:00'
---

# Contract

The library has **no shared or static mutable state** — concurrent jobs run isolated as long
as each thread uses its own instances:

- **Builders are not thread-safe and single-use.** Fresh `WorkbookBuilder.create()` /
  `XlsxBuilder.create()` per request; a second `write` throws `IllegalStateException`.
- **Each `write()` creates its own POI workbook** — never write the same file concurrently.
- **Do not share a `DataProvider`** — forward-only, single-use, its own source per request;
  the builder calls `close()` itself.
- **Memory scales with concurrency:** out-of-core bounds memory *per* sort
  (`sortChunkSize` rows + SXSSF window), but *N* concurrent sorts add up to
  ~*N x sortChunkSize* rows — limit concurrency (thread pool/`Semaphore`) and/or reduce
  `sortChunkSize`.
- **Temp dir & OS limits:** up to 16 open run files per sort — plan `ulimit -n` and disk
  space for the expected concurrency, see
  [Out-of-core pipeline](/architecture/out-of-core-pipeline.md).

# Citations

[1] [README - Concurrency / server operation](https://github.com/MaKnoNet/xlsxBuilder/blob/main/README.md)
