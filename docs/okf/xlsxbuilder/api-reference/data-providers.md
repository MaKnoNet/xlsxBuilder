---
type: API Reference
title: DataProviders
description: Factory-Klasse mit Adaptern für Iterator/Iterable/Stream/JDBC-ResultSet als DataProvider.
resource: src/main/java/de/makno/xlsxbuilder/DataProviders.java
tags: [api-reference, factory, jdbc, streaming]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`public final class DataProviders` — zustandslose Factory für gängige
[DataProvider](/api-reference/data-provider.md)-Adapter. Nicht instanziierbar (privater
No-Op-Konstruktor). Thread-Safety: die Factory-Methoden selbst sind zustandslos und damit
thread-sicher aufrufbar; die zurückgegebenen `DataProvider`-Instanzen sind jedoch — wie der
gesamte `DataProvider`-Vertrag — vorwärts-lesbar/single-use und **nicht** thread-sicher (siehe
[Concurrency contract](/architecture/concurrency-contract.md)).

# Konstruktoren

## `private DataProviders()`

Leerer privater Konstruktor, verhindert Instanziierung von außen. Keine Parameter, keine
Exceptions.

# Methoden

## `static <T> DataProvider<T> ofIterator(Iterator<? extends T> iterator)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `iterator` | `Iterator<? extends T>` | nein — `Objects.requireNonNull(iterator, "iterator")` |

Rückgabewert: neuer `DataProvider<T>`, delegiert `hasNext()`/`next()` an den übergebenen
Iterator; nie `null`. Geworfene Exceptions: `NullPointerException`, wenn `iterator == null`.
Die zurückgegebene `next()`-Implementierung wirft zusätzlich `NoSuchElementException`, wenn
`hasNext()` zum Aufrufzeitpunkt `false` ist (explizite Prüfung im anonymen `DataProvider`, nicht
nur Weiterreichung des zugrunde liegenden Iterator-Verhaltens). `close()` ist nicht
überschrieben — nutzt die Default-No-Op-Implementierung von `DataProvider`.

## `static <T> DataProvider<T> ofIterable(Iterable<? extends T> iterable)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `iterable` | `Iterable<? extends T>` | nein — `Objects.requireNonNull(iterable, "iterable")` |

Rückgabewert: `DataProvider<T>` über `iterable.iterator()`, nie `null`. Geworfene Exceptions:
`NullPointerException` bei `iterable == null`; ansonsten wie `ofIterator` (delegiert direkt
dorthin).

## `static <T> DataProvider<T> ofStream(Stream<? extends T> stream)`

Adaptiert einen `Stream`; der Stream wird bei `DataProvider.close()` geschlossen.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `stream` | `Stream<? extends T>` | nein — `Objects.requireNonNull(stream, "stream")` |

Rückgabewert: `DataProvider<T>`, nie `null`. Geworfene Exceptions: `NullPointerException` bei
`stream == null`. `next()` wirft `NoSuchElementException`, wenn kein Element mehr vorhanden ist
(explizite Prüfung, wie bei `ofIterator`). `close()` **ist** hier überschrieben und ruft
`stream.close()` — Javadoc-Aussage "the stream is closed on close()" verifiziert korrekt.

## `static <T> DataProvider<T> ofResultSet(ResultSet rs, ResultSetRowMapper<? extends T> mapper)`

Adaptiert ein JDBC `ResultSet` als vorwärts-lesbaren `DataProvider` — für echte
Out-of-Core-Fälle, da die Datenbank die Zeilen gestreamt liefert.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `rs` | `ResultSet` | nein — `Objects.requireNonNull(rs, "rs")` |
| `mapper` | `ResultSetRowMapper<? extends T>` | nein — `Objects.requireNonNull(mapper, "mapper")` |

Rückgabewert: `DataProvider<T>`, nie `null`.

Geworfene Exceptions:
- `NullPointerException`, wenn `rs` oder `mapper` `null` ist.
- Die zurückgegebene `hasNext()`-Implementierung fängt eine `SQLException` aus `rs.next()` ab
  und wirft `DataAccessException("ResultSet.next() failed", e)` (ungechecktes Wrapping,
  verifiziert).
- Die zurückgegebene `next()`-Implementierung wirft `NoSuchElementException`, wenn `hasNext()`
  `false` liefert; fängt eine `SQLException` aus `mapper.map(rs)` ab und wirft
  `DataAccessException("Mapping the ResultSet row failed", e)`.
- Die zurückgegebene `close()`-Implementierung schließt **nur** das `ResultSet` (nicht
  `Statement`/`Connection` — Javadoc-Aussage verifiziert korrekt) und wirft bei einer
  `SQLException` aus `rs.close()` eine `DataAccessException("Closing the ResultSet failed", e)`.

Verhalten bei mehrfachem `hasNext()`-Aufruf: intern per Lookahead (`lookedAhead`/`hasRow`)
implementiert, sodass `rs.next()` pro Zeile nur einmal aufgerufen wird, auch bei mehrfachem
`hasNext()` ohne zwischenzeitlichen `next()`-Aufruf — verifiziert korrekt gegen den
Javadoc-Hinweis "not thread-safe / single-use (like a ResultSet itself)".

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/DataProviders.java`
[2] [DataProvider / DataProviders (Komponente)](/components/data-provider.md)
