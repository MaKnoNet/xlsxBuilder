---
type: API Reference
title: ExternalMergeSort.close(...)
description: Methode close von ExternalMergeSort - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/ExternalMergeSort.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `void close()`


Aus `Closeable`, hier **ohne** `throws IOException` überschrieben (wie
[CloseableIterator](/api-reference/closeable-iterator/closeable-iterator.md)). Löscht alle registrierten
Run-Dateien (`runFiles`) sowie das Temp-Verzeichnis (`tempDir`), jeweils best-effort — I/O-Fehler
beim Löschen werden verschluckt (`catch (IOException ignored)`), nie propagiert. Keine
Parameter, kein Rückgabewert, keine Exceptions. Mehrfacher Aufruf ist sicher (leert `runFiles`
und setzt `tempDir = null`, daher idempotent).

# Citations

[1] [ExternalMergeSort (Übersicht)](./external-merge-sort.md)
