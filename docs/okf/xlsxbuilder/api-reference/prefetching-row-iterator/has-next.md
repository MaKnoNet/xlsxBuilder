---
type: API Reference
title: PrefetchingRowIterator.hasNext(...)
description: Methode hasNext von PrefetchingRowIterator - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/PrefetchingRowIterator.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `boolean hasNext()`


Keine Parameter. Rückgabewert: `true`, wenn eine gepufferte oder aus der Queue geholte Zeile
verfügbar ist; primitiv, nie `null`. Blockiert (`queue.take()`) bis ein Element oder das
End-Sentinel verfügbar ist, falls noch keine Zeile gepuffert ist und der Stream nicht als
beendet markiert wurde.

Geworfene Exceptions:
- `IllegalStateException("Interrupted while waiting for the next data row", e)`, wenn der
  wartende Thread während `queue.take()` unterbrochen wird (der Interrupt-Status wird vorher
  wiederhergestellt: `Thread.currentThread().interrupt()`).
- Über `rethrowIfFailed()`: eine im Producer aufgetretene `RuntimeException` wird unverändert
  erneut geworfen; ein `Error` wird unverändert erneut geworfen; jeder andere `Throwable`-Typ
  wird in `IllegalStateException("Error while reading the data source", t)` gewrappt. Dies
  passiert, sobald das End-Sentinel erreicht wird (`item == END`) und eine `failure` aus dem
  Producer-Thread vorliegt.

# Citations

[1] [PrefetchingRowIterator (Übersicht)](./prefetching-row-iterator.md)
