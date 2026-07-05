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

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `LOG` | `private static final Logger` | Log4j2-Logger dieser Klasse. | nein |
| `MAX_FAN_IN` | `private static final int` | Konstante `16` — maximale Anzahl gleichzeitig offener Runs während des Merges (begrenzt offene Datei-Handles). | entfällt (primitiv) |
| `comparator` | `private final Comparator<Row>` | Vergleichsfunktion für die Sortierung. | nein — Konstruktorparameter ohne expliziten Null-Check im Konstruktor selbst, aber praktisch stets ein `RowComparator` |
| `chunkSize` | `private final int` | Zeilen pro In-Memory-Chunk vor dem Spillen. | entfällt (primitiv); Konstruktor prüft `>= 1` |
| `baseTempDir` | `private final Path` | Basisverzeichnis für Run-Dateien. | ja — `null` bedeutet System-Temp (`java.io.tmpdir`) |
| `runFiles` | `private final List<Path>` | Alle bislang erzeugten Temp-Dateien (Runs + Merge-Zwischenergebnisse), zum Aufräumen bei `close()`. | Feld nie `null` (mit `new ArrayList<>()` initialisiert) |
| `tempDir` | `private Path` | Konkretes, zur Laufzeit angelegtes Temp-Unterverzeichnis dieser Sortierung. | ja — `null` bis zum ersten `sort(...)`-Aufruf |
| `rowsRead` | `private long` | Performance-Metrik: Anzahl gelesener Zeilen (nur fürs Logging). | entfällt (primitiv) |
| `initialRuns` | `private int` | Performance-Metrik: Anzahl initial erzeugter Runs. | entfällt (primitiv) |
| `mergePasses` | `private int` | Performance-Metrik: Anzahl der Vor-Merge-Pässe. | entfällt (primitiv) |

# Thread-Safety

Nicht dokumentiert im Javadoc, aber aus dem Entwurf ersichtlich und hier verifiziert: die
Instanz kapselt veränderlichen Zustand (`runFiles`, `tempDir`, die drei Zähler) ohne
`synchronized` oder sonstige Synchronisation — für einmalige, sequenzielle Nutzung durch
genau einen Aufrufer-Thread gedacht (kein Aufruf von `sort(...)` durch mehrere Threads
gleichzeitig, kein zweiter `sort(...)`-Aufruf auf derselben Instanz vorgesehen). Passt zum
Single-Use-Charakter der gesamten Bibliothek.

# Serialisierung

Nicht `Serializable` — `ExternalMergeSort` implementiert kein Serialisierungs-Interface
(verifiziert: `final class ExternalMergeSort implements Closeable`, `Closeable` selbst ist
nicht `Serializable`).

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben (verifiziert: keine `equals`/`hashCode`/`toString`-
Deklaration im Quellcode) — es gilt die Identitätssemantik von `java.lang.Object`. Bei einer
paketinternen, zustandsbehafteten Single-Use-Klasse wie dieser ist das die erwartete, unkritische
Konsequenz.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `final class ExternalMergeSort implements Closeable` —
implementiert das JDK-Interface `java.io.Closeable` direkt; keine eigene Oberklasse außer
`java.lang.Object`.

Diese Datei/Klasse enthält außerdem drei private statische Nested Classes mit eigenen
Vererbungsbeziehungen (nicht separat in `api-reference/` dokumentiert, da paketprivat/nested):
- `private static final class RunReader implements Closeable`
- `private static final class MergingIterator implements java.util.Iterator<Row>`
- `private static final class MergeIterator implements CloseableIterator<Row>` — siehe
  [CloseableIterator](/api-reference/closeable-iterator/closeable-iterator.md) für dessen Implementierer-Liste.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts für `ExternalMergeSort` selbst
(`final`, kein anderer Typ implementiert oder erweitert sie — verifiziert per Grep).

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``CloseableIterator<Row> sort(Iterator<Row> source) throws IOException``](./sort.md)
- [``void close()``](./close.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/ExternalMergeSort.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
