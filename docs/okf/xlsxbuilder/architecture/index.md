# Konzepte

* [Concurrency contract (server operation)](/architecture/concurrency-contract.md) - No shared or static mutable state in the library; builders and DataProviders are not thread-safe and single-use — one fresh instance per request; memory scales with concurrent sorts.
* [Out-of-core pipeline (external merge sort + SXSSF)](/architecture/out-of-core-pipeline.md) - DataProvider -> Row projection -> optional external merge sort (temp-file runs, k-way merge) -> Apache POI SXSSF streaming. Memory depends on sortChunkSize + SXSSF window, not on row count.
