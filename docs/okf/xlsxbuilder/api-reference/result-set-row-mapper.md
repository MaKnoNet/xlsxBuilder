---
type: API Reference
title: ResultSetRowMapper
description: Funktionales Interface zum Mappen der aktuellen ResultSet-Zeile auf ein Objekt T, konsumiert von DataProviders.ofResultSet.
resource: src/main/java/de/makno/xlsxbuilder/ResultSetRowMapper.java
tags: [api-reference, functional-interface, jdbc]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`@FunctionalInterface public interface ResultSetRowMapper<T>` — liest ausschließlich die
Spalten der **aktuellen** Zeile (z. B. `rs.getString(...)`) und ruft **nicht** `rs.next()` auf
— das übernimmt der Adapter
([DataProviders.ofResultSet](/api-reference/data-providers.md)). Siehe auch
[DataProvider (Komponente) – Supporting types](/components/data-provider.md).

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `@FunctionalInterface public interface
ResultSetRowMapper<T>` — keine `extends`-Klausel, keine Oberklasse (reines funktionales
Top-Level-Interface).

**Rückwärts:** Keine Klasse im Projekt implementiert dieses Interface namentlich; es wird
ausschließlich als **Lambda-/Parametertyp** in
[DataProviders](/api-reference/data-providers.md)`.ofResultSet(ResultSet,
ResultSetRowMapper<? extends T>)` konsumiert — Aufrufer übergeben typischerweise eine Lambda oder
Methodenreferenz, keine benannte Implementierungsklasse existiert im Quellbaum (verifiziert per
Grep nach `implements.*ResultSetRowMapper`, keine Treffer).

# Konstruktoren

Keine — funktionales Interface ohne Konstruktor.

# Methoden

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
[DataAccessException](/api-reference/data-access-exception.md).

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/ResultSetRowMapper.java`
[2] [DataProvider (Komponente)](/components/data-provider.md)
