---
type: API Reference
title: ExternalMergeSort
description: Paketinterne externe Sortierung über Row-Objekte mit fan-in-begrenztem k-way Merge; Temp-Dateien werden bei close() gelöscht.
resource: src/main/java/de/makno/xlsxbuilder/ExternalMergeSort.java
tags: [api-reference, sorting, streaming, out-of-core, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`final class ExternalMergeSort implements Closeable` — paketintern, siehe
[Out-of-core pipeline](/architecture/out-of-core-pipeline.md) für den Gesamtkontext. Sortiert
auch Datenmengen, die nicht in den Speicher passen: Quelle wird in Chunks fester Größe gelesen,
jeder Chunk im Speicher sortiert und als "Run" auf eine Temp-Datei geschrieben; übersteigt die
Run-Anzahl den maximalen Fan-in (`MAX_FAN_IN = 16`), werden die Runs in mehreren Pässen
vor-gemergt, bis höchstens `MAX_FAN_IN` Runs übrig bleiben (begrenzt gleichzeitig offene
Datei-Handles); abschließend liefert ein k-way Merge (`PriorityQueue` über die Run-Köpfe) einen
sortierten Stream. Speicherverbrauch ist durch Chunk-Größe und Fan-in begrenzt, unabhängig von
der Gesamtzeilenzahl.

**Thread-Safety:** nicht dokumentiert, aber aus dem Entwurf ersichtlich: eine Instanz kapselt
veränderlichen Zustand (`runFiles`, `tempDir`, Zähler) und ist für einmalige, sequenzielle
Nutzung durch einen Aufrufer gedacht (kein `synchronized`, keine defensiven Kopien für
Nebenläufigkeit) — passend zum Single-Use-Charakter der Bibliothek.

# Konstruktoren

## `ExternalMergeSort(Comparator<Row> comparator, int chunkSize)`

Delegiert an den Vollkonstruktor mit `baseTempDir = null`.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `comparator` | `Comparator<Row>` | im Vollkonstruktor **nicht geprüft** — kein `requireNonNull`; ein `null`-Comparator führt erst bei der tatsächlichen Verwendung (`buffer.sort(comparator)` bzw. im `PriorityQueue`-Vergleich) zu einer `NullPointerException`, nicht sofort im Konstruktor |
| `chunkSize` | `int` | primitiv; muss `>= 1` sein |

Verhalten bei ungültiger Eingabe: `IllegalArgumentException("chunkSize must be >= 1")`, wenn
`chunkSize < 1` (Prüfung sitzt im Vollkonstruktor, an den delegiert wird).

## `ExternalMergeSort(Comparator<Row> comparator, int chunkSize, Path baseTempDir)`

Vollkonstruktor.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `comparator` | `Comparator<Row>` | **nicht geprüft** (siehe oben — abweichend von einer naiven Erwartung, dass ein Kern-Kollaborator immer `requireNonNull`-geprüft wird) |
| `chunkSize` | `int` | muss `>= 1` sein, sonst `IllegalArgumentException` |
| `baseTempDir` | `Path` | **ja** — `null` bedeutet System-Temp (`java.io.tmpdir`), laut Javadoc "Created on demand" |

Geworfene Exceptions: `IllegalArgumentException("chunkSize must be >= 1")` bei `chunkSize < 1`.
Kein `NullPointerException` bei `comparator == null` an dieser Stelle (erst bei späterer
Verwendung in `sort(...)`).

# Methoden

## `CloseableIterator<Row> sort(Iterator<Row> source) throws IOException`

Konsumiert die Quelle vollständig (erzeugt dabei die Runs) und liefert den sortierten
Merge-Stream zurück.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `source` | `java.util.Iterator<Row>` | nicht explizit geprüft; `source.hasNext()`/`source.next()` wird direkt aufgerufen — ein `null`-Argument führt zu sofortiger `NullPointerException` beim ersten Zugriff |

Rückgabewert: `CloseableIterator<Row>` über die sortierten Zeilen, nie `null` (bei Erfolg wird
immer ein `MergeIterator` konstruiert und zurückgegeben).

Geworfene Exceptions:
- `IOException`, wenn das Anlegen des Temp-Verzeichnisses/der Run-Dateien fehlschlägt oder das
  Schreiben/Lesen der Run-Dateien fehlschlägt. **Bei Fehler wird `close()` aufgerufen** (alle
  bereits erzeugten Temp-Dateien werden gelöscht), bevor die `IOException` weitergeworfen wird
  — verifiziert im `catch (IOException e)`-Block.
- Eine `NullPointerException`, falls `comparator` (aus dem Konstruktor) `null` war und beim
  Sortieren des Puffers (`buffer.sort(comparator)`) verwendet wird.

## `void close()`

Aus `Closeable`, hier **ohne** `throws IOException` überschrieben (wie
[CloseableIterator](/api-reference/closeable-iterator.md)). Löscht alle registrierten
Run-Dateien (`runFiles`) sowie das Temp-Verzeichnis (`tempDir`), jeweils best-effort — I/O-Fehler
beim Löschen werden verschluckt (`catch (IOException ignored)`), nie propagiert. Keine
Parameter, kein Rückgabewert, keine Exceptions. Mehrfacher Aufruf ist sicher (leert `runFiles`
und setzt `tempDir = null`, daher idempotent).

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/ExternalMergeSort.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
