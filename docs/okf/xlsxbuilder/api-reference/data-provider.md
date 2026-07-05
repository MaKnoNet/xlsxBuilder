---
type: API Reference
title: DataProvider
description: Vorwärts-lesbare, einmal durchlaufbare Datenquellen-Abstraktion für XlsxBuilder mit Default-close().
resource: src/main/java/de/makno/xlsxbuilder/DataProvider.java
tags: [api-reference, interface, streaming, data]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`DataProvider<T> extends Closeable` — abstrahiert die Datenquelle für
[XlsxBuilder](/api-reference/xlsx-builder.md). Wird genau einmal durchlaufen, sodass auch nicht
vollständig in den Speicher passende Datenmengen verarbeitet werden können (z. B. ein JDBC
`ResultSet` oder ein gepufferter Datei-Reader). Vertrag laut
[Konzeptdokumentation](/components/data-provider.md): **vorwärts-lesbar, single-use, nicht
teilen** — eigene Quelle pro Request. Fabrikmethoden dafür: siehe
[DataProviders](/api-reference/data-providers.md).

# Konstruktoren

Keine — Interface ohne Konstruktor.

# Methoden

## `boolean hasNext()`

Keine Parameter. Rückgabewert: `true`, solange ein weiterer Datensatz verfügbar ist; primitiv,
nie `null`. Keine Exceptions in der Interface-Deklaration; konkrete Implementierungen (z. B.
`DataProviders.ofResultSet`) können ungecheckte Exceptions werfen (dort:
`DataAccessException`, siehe [DataProviders](/api-reference/data-providers.md)).

## `T next()`

Keine Parameter. Rückgabewert: der nächste Datensatz vom Typ `T`. Ob `null` zurückgegeben
werden kann, hängt von der konkreten Implementierung und dem Datentyp `T` ab — das Interface
selbst macht keine Zusicherung. Keine Exceptions in der Interface-Deklaration; konkrete
Implementierungen werfen typischerweise `NoSuchElementException`, wenn kein Element mehr
vorhanden ist (siehe `DataProviders`), oder `DataAccessException` bei einem zugrunde liegenden
SQL-Fehler.

## `default void close()`

Keine Parameter. Rückgabewert: `void`. Default-Implementierung: leerer Methodenrumpf (nichts zu
schließen) — Quellen, die Ressourcen halten (DB, Datei), überschreiben diese Methode. Keine
Exceptions (Override von `Closeable#close()` ohne `throws IOException` in der Default-Variante;
überschreibende Implementierungen könnten theoretisch eine ungechecktes Pendant werfen, tun das
in dieser Bibliothek aber nicht — sie wrappen `SQLException` stattdessen in
`DataAccessException`).

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/DataProvider.java`
[2] [DataProvider / DataProviders (Komponente)](/components/data-provider.md)
