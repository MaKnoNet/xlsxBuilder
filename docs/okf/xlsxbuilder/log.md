# Update Log

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
