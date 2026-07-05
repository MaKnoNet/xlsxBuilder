---
type: API Reference
title: PrefetchingRowIterator.close(...)
description: Methode close von PrefetchingRowIterator - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/PrefetchingRowIterator.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `void close()`


Keine Parameter, kein Rückgabewert (überschreibt `Closeable#close()` ohne `throws
IOException`, wie bei `CloseableIterator`). Setzt `closed = true`, unterbricht den Producer,
leert die Queue (entsperrt einen ggf. blockierenden `put()` im Producer) und wartet mit
`producer.join(joinTimeoutMillis)`. Ist der Producer danach noch am Leben, wird eine
`LOG.warn(...)` ausgegeben (kein Fehler, best-effort). Ein zuvor aufgetretener, aber nie an den
Konsumenten weitergegebener `failure` wird ebenfalls nur geloggt (`LOG.warn`), nicht
geworfen — Begründung im Kommentar: "a primary exception is likely already in flight".

Geworfene Exceptions: **keine** — auch ein `InterruptedException` beim `producer.join(...)`
wird gefangen und nur der Interrupt-Status des aufrufenden Threads wiederhergestellt
(`Thread.currentThread().interrupt()`), nicht weitergeworfen. Diese Methode wirft also
nachweislich nie eine Exception, obwohl sie potenziell fehlerhafte oder blockierte
Producer-Zustände beobachtet.

# Citations

[1] [PrefetchingRowIterator (Übersicht)](./prefetching-row-iterator.md)
