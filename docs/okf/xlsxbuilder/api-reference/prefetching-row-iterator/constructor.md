---
type: API Reference
title: PrefetchingRowIterator – Konstruktoren
description: Alle Konstruktoren von PrefetchingRowIterator.
resource: src/main/java/de/makno/xlsxbuilder/PrefetchingRowIterator.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


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

# Citations

[1] [PrefetchingRowIterator (Übersicht)](./prefetching-row-iterator.md)
