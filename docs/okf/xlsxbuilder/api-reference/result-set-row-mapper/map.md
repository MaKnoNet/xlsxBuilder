---
type: API Reference
title: ResultSetRowMapper.map(...)
description: Methode map von ResultSetRowMapper - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/ResultSetRowMapper.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `T map(ResultSet rs) throws SQLException`


| Parameter | Typ | null-erlaubt |
|---|---|---|
| `rs` | `ResultSet` | nicht durch das Interface selbst geprüft; ob `null` toleriert wird, hängt von der konkreten Lambda-/Implementierung ab. Der einzige Aufrufer in dieser Bibliothek (`DataProviders.ofResultSet`) übergibt stets das zuvor selbst `requireNonNull`-geprüfte `rs`, nie `null` |

Rückgabewert: das gemappte Objekt vom Typ `T`. Ob `null` zurückgegeben werden darf, macht das
Interface **keine** Zusicherung — abhängig von der konkreten Implementierung; der Aufrufer
(`DataProviders.ofResultSet.next()`) reicht das Ergebnis unverändert weiter, ohne selbst auf
`null` zu prüfen.

Geworfene Exceptions: deklariert `throws SQLException` (geprüfte Exception) — wird vom
Interface selbst nicht ausgelöst, sondern ist die erwartete Ausnahme bei einem
JDBC-Zugriffsfehler innerhalb der Implementierung. Der einzige Aufrufer in dieser Bibliothek
fängt eine solche `SQLException` ab und wrappt sie in eine ungechecktes
[DataAccessException](/api-reference/data-access-exception/data-access-exception.md).

# Citations

[1] [ResultSetRowMapper (Übersicht)](./result-set-row-mapper.md)
