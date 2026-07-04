# Konzepte

* [Concurrency contract (server operation)](/architecture/concurrency-contract.md) - No shared or static mutable state in the library; builders and DataProviders are not thread-safe and single-use — one fresh instance per request; memory scales with concurrent sorts.
* [Fehlerbehandlung (RowLimitExceededException, DataAccessException)](/architecture/error-handling.md) - Fail-fast bei Zeilenlimit-Ueberschreitung und geprueften SQL-Fehlern in ungecheckten DataProvider-Methoden; dank atomarem write(Path) nie ein Teil-File.
* [Out-of-core pipeline (external merge sort + SXSSF)](/architecture/out-of-core-pipeline.md) - DataProvider -> Row projection -> optional external merge sort (temp-file runs, k-way merge) -> Apache POI SXSSF streaming. Memory depends on sortChunkSize + SXSSF window, not on row count.
