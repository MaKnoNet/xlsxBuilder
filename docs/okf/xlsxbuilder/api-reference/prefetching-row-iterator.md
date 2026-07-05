---
type: API Reference
title: PrefetchingRowIterator
description: Paketinterner CloseableIterator, der Lesen/Sortieren und Schreiben über einen daemon Hintergrundthread und eine begrenzte BlockingQueue überlappt.
resource: src/main/java/de/makno/xlsxbuilder/PrefetchingRowIterator.java
tags: [api-reference, concurrency, streaming, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`final class PrefetchingRowIterator implements CloseableIterator<Row>` — paketintern. Ein
Daemon-Hintergrundthread zieht Zeilen (Projektion/DB-Lesen + k-way Merge) in eine begrenzte
`ArrayBlockingQueue` (Kapazität `2048`), während der konsumierende (schreibende) Thread daraus
liest — Lese/Sortier-I/O und POI-Schreiben laufen dadurch parallel statt streng sequenziell,
siehe [Out-of-core pipeline](/architecture/out-of-core-pipeline.md). Nur ein zusätzlicher
Thread pro Sheet.

**Thread-Safety-Vertrag** (dokumentiert und verifiziert): die zugrunde liegende Quelle (im
Parallel-Modus ein `DataProvider`/Sort-Iterator) muss angemessen begrenzt sein und
Thread-Interrupts respektieren. `close()` unterbricht den Producer und wartet bis zu
`joinTimeoutMillis` (Default `5000` ms); reagiert `next()` nicht auf den Interrupt und läuft
länger, liest der Producer die Quelle unter Umständen weiter, während der Aufrufer sie bereits
schließt — in diesem Fall wird eine Warnung geloggt (best-effort-Stop statt stillem Versagen).

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `final class PrefetchingRowIterator implements
CloseableIterator<Row>` — implementiert das projekteigene Interface
[CloseableIterator](/api-reference/closeable-iterator.md) (das seinerseits `Iterator<T>` und
`Closeable` erweitert); keine eigene Oberklasse außer `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, daher nicht
erweiterbar, und kein anderer Typ implementiert `PrefetchingRowIterator` (es ist keine
Schnittstelle). Siehe [CloseableIterator](/api-reference/closeable-iterator.md) für die
vollständige Implementierer-Liste dieses gemeinsamen Interfaces (u. a. auch
`ExternalMergeSort.MergeIterator`).

# Konstruktoren

## `PrefetchingRowIterator(Iterator<Row> source)`

Delegiert an den Zwei-Parameter-Konstruktor mit `joinTimeoutMillis = 5000`
(`DEFAULT_JOIN_TIMEOUT_MS`).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `source` | `Iterator<Row>` | **nicht explizit geprüft** — kein `requireNonNull`; wird direkt in die Producer-Lambda `() -> produce(source)` eingefangen. Ein `null`-Argument führt nicht sofort im Konstruktor, sondern erst beim ersten `source.hasNext()`-Aufruf im gestarteten Producer-Thread zu einer `NullPointerException`, die dort als `failure` gespeichert und beim nächsten `hasNext()`/`next()`-Aufruf des Konsumenten erneut geworfen wird (siehe `rethrowIfFailed()`) |

Startet sofort einen Daemon-Thread (`setDaemon(true)`, Name `"xlsxbuilder-prefetch"`), der
`produce(source)` ausführt. Keine Exceptions direkt im Konstruktor.

## `PrefetchingRowIterator(Iterator<Row> source, long joinTimeoutMillis)`

Paketinterner Konstruktor mit konfigurierbarem Join-Timeout (laut Kommentar für schnelle
Tests gedacht).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `source` | `Iterator<Row>` | wie oben — nicht geprüft, Fehler tritt asynchron im Producer-Thread auf |
| `joinTimeoutMillis` | `long` | primitiv, keine Prüfung auf negative Werte; ein negativer Wert würde an `Thread.join(long)` durchgereicht, was dort selbst eine `IllegalArgumentException` auslösen würde (nicht in dieser Klasse geprüft) |

Startet den Producer-Thread wie oben. Keine Exceptions direkt im Konstruktor.

# Methoden

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

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/PrefetchingRowIterator.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
