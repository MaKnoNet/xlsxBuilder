# Update Log

## 2026-07-06

* **Update**: `CLAUDE.md` routine tightened from "end-of-session" to "pre-commit" —
  affected OKF concepts are now updated before every single commit with code/
  architecture changes instead of batched at session end;
  [developer guide](/conventions/okf-entwicklerdoku.md) adjusted accordingly.
* **Creation**: new concept
  [Entwicklerdoku – OKF-Wissensdatenbank pflegen](/conventions/okf-entwicklerdoku.md) —
  bundle structure, frontmatter convention, step-by-step "add a new concept",
  automation/hybrid strategy, known pitfalls.

## 2026-07-05

* **Update**: semantic gap-fill against graphify-out/GRAPH_REPORT.md —
  [out-of-core-pipeline.md](/architecture/out-of-core-pipeline.md) extended with
  `RowCodec` (compact run-file serialization) and `PrefetchingRowIterator`
  (read/sort ∥ write); [data-provider.md](/components/data-provider.md) extended with
  `ResultSetRowMapper`, `Row`, `CloseableIterator`, `RenderJob`.
* **Creation**: new concept
  [configuration-models.md](/components/configuration-models.md) — the immutable
  value-object group `SheetWriteOptions`/`SortSpec`/`SummarySpec`/`ColumnGroup`/
  `SplitSheetNamer`/`Placeholders`.
* **Creation**: new concept [error-handling.md](/architecture/error-handling.md) —
  `RowLimitExceededException`/`DataAccessException`, tied back to the atomic write.

## 2026-07-04

* **Update**: Rebased onto current `main`; concepts refreshed for the new features —
  atomic `write(Path)` (temp file + move) in
  [WorkbookBuilder](/components/workbook-builder.md), Excel row-limit handling
  (`RowLimitExceededException` / `splitOnRowLimit`) in
  [XlsxBuilder](/components/xlsx-builder.md); knowledge graph rebuilt
  (591 nodes, 1775 edges, 46 communities).
* **Initialization**: OKF bundle created — components
  ([XlsxBuilder](/components/xlsx-builder.md),
  [WorkbookBuilder](/components/workbook-builder.md),
  [DataProvider](/components/data-provider.md)), architecture
  ([out-of-core pipeline](/architecture/out-of-core-pipeline.md),
  [concurrency contract](/architecture/concurrency-contract.md)) and conventions
  ([build & release](/conventions/build-and-release.md)) derived from README.md.
* **Creation**: graphify knowledge graph built (481 nodes, 1444 edges, 32 communities);
  kept current automatically by the pre-commit hook from now on.
