---
type: API Reference
title: DataProviders.ofResultSet(...)
description: Methode ofResultSet von DataProviders - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/DataProviders.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

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

[1] [DataProviders (Übersicht)](./data-providers.md)
