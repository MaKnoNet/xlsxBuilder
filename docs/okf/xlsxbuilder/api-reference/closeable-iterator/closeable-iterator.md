---
type: API Reference
title: CloseableIterator
description: Iterator, das Ressourcen (z. B. offene Run-Dateien) hält und ohne geprüfte Exception schließbar ist.
resource: src/main/java/de/makno/xlsxbuilder/CloseableIterator.java
tags: [api-reference, interface, iterator, closeable]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`CloseableIterator<T>` ist ein öffentliches Marker-Interface: `Iterator<T> & Closeable`, wobei
`close()` **ohne** geprüfte `IOException` deklariert ist (Override von `Closeable#close()` ohne
`throws`). Damit lassen sich Ressourcen-haltige Iteratoren (offene Run-Dateien beim k-way Merge,
siehe [ExternalMergeSort](/api-reference/external-merge-sort/external-merge-sort.md)) bequem in
try-with-resources verwenden, ohne dass Aufrufer eine geprüfte Exception behandeln müssen.
Konkrete Implementierungen: `ExternalMergeSort.MergeIterator`,
[PrefetchingRowIterator](/api-reference/prefetching-row-iterator/prefetching-row-iterator.md).

Kein eigener Thread-Safety-Vertrag dokumentiert; wie die meisten Iteratoren dieser Bibliothek
ist von Single-Thread-, Single-Use-Gebrauch auszugehen (ein Durchlauf, ein Thread).

# Felder

Keine Felder — reines Interface ohne Zustand (nur eine eigene Methodendeklaration
`void close()`; alle anderen Methoden sind von `Iterator<T>`/`Closeable` geerbt).

# Thread-Safety

Kein eigener Vertrag auf Interface-Ebene (Interfaces können keinen Zustand/keine
Synchronisation erzwingen). Wie bei den meisten Iteratoren dieser Bibliothek ist
Single-Thread-, Single-Use-Gebrauch die Erwartung — jede konkrete Implementierung
(`PrefetchingRowIterator`, `ExternalMergeSort.MergeIterator`) dokumentiert ihren eigenen
Vertrag separat.

# Serialisierung

Nicht `Serializable` — `CloseableIterator<T>` implementiert kein Serialisierungs-Interface
(verifiziert gegen die Deklaration `public interface CloseableIterator<T> extends
Iterator<T>, Closeable`).

# equals/hashCode/toString

Das Interface deklariert keine dieser Methoden; jede implementierende Klasse
erbt/überschreibt sie unabhängig (siehe die jeweiligen Implementierer-Dokumente, z. B.
[PrefetchingRowIterator](/api-reference/prefetching-row-iterator/prefetching-row-iterator.md)).

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `public interface CloseableIterator<T> extends Iterator<T>, Closeable`
— erweitert zwei JDK-Interfaces: `java.util.Iterator<T>` und `java.io.Closeable`; keine eigene
Oberklasse (Interfaces haben keine Superklasse).

**Rückwärts (Implementierer innerhalb dieses Projekts, verifiziert per Grep):**
- [PrefetchingRowIterator](/api-reference/prefetching-row-iterator/prefetching-row-iterator.md) — `final class
  PrefetchingRowIterator implements CloseableIterator<Row>`.
- `ExternalMergeSort.MergeIterator` (private statische Nested Class innerhalb von
  [ExternalMergeSort](/api-reference/external-merge-sort/external-merge-sort.md)) — `private static final class
  MergeIterator implements CloseableIterator<Row>`. Keine eigene `api-reference/`-Datei, da
  paketprivat und nicht separat dokumentiert.

Keine weiteren Implementierer gefunden (grep über `src/main/java/de/makno/xlsxbuilder/**/*.java`
nach `implements.*CloseableIterator`).

# Konstruktoren


# Methoden

- [``void close()``](./close.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/CloseableIterator.java`
