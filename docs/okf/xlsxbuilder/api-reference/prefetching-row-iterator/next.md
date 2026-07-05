---
type: API Reference
title: PrefetchingRowIterator.next(...)
description: Methode next von PrefetchingRowIterator - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/PrefetchingRowIterator.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `Row next()`


Keine Parameter. Rückgabewert: die nächste `Row`; **kann laut Signatur theoretisch `null`
sein, wenn `hasNext()` zuvor eine `null`-Zeile aus der Queue gepuffert hätte** — praktisch aber
ausgeschlossen, da `queue.put(source.next())` in `produce(...)` niemals bewusst `null` einreiht
(die Queue-Implementierung `ArrayBlockingQueue` verbietet `null`-Elemente ohnehin und würde bei
einem `null`-Element aus `source.next()` bereits eine `NullPointerException` im Producer-Thread
auslösen, die als `failure` gespeichert wird). Praktisch also nie `null` bei erfolgreichem
Rückgabepfad.

Geworfene Exceptions: `NoSuchElementException`, wenn `hasNext()` `false` liefert (explizite
Prüfung `if (!hasNext()) throw new NoSuchElementException();`).

# Citations

[1] [PrefetchingRowIterator (Übersicht)](./prefetching-row-iterator.md)
