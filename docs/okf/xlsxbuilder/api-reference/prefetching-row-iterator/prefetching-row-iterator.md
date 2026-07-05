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

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `LOG` | `private static final Logger` | Log4j2-Logger dieser Klasse. | nein |
| `CAPACITY` | `private static final int` | Konstante `2048` — Kapazität der `ArrayBlockingQueue`. | entfällt (primitiv) |
| `DEFAULT_JOIN_TIMEOUT_MS` | `private static final long` | Konstante `5000` — Default-Timeout für `producer.join(...)` bei `close()`. | entfällt (primitiv) |
| `END` | `private static final Object` | Sentinel-Objekt, das das Ende der Produktion signalisiert. | nein — statisch, nie `null` |
| `queue` | `private final BlockingQueue<Object>` | Begrenzte Queue zwischen Producer- und Consumer-Thread. | nein |
| `producer` | `private final Thread` | Daemon-Hintergrundthread, der die Quelle liest. | nein |
| `joinTimeoutMillis` | `private final long` | Konfigurierbares Timeout für `close()` (Test-Seam). | entfällt (primitiv) |
| `closed` | `private volatile boolean` | Flag: `close()` wurde aufgerufen. `volatile`, da von Consumer- und Producer-Thread gelesen. | entfällt (primitiv) |
| `failure` | `private volatile Throwable` | Vom Producer aufgefangener Fehler, zur späteren Weitergabe an den Consumer. `volatile` für sichtbare Cross-Thread-Kommunikation. | ja — `null` bedeutet kein Fehler aufgetreten |
| `nextRow` | `private Row` | Vorgelesene, gepufferte nächste Zeile. Nur vom Consumer-Thread gelesen/geschrieben. | ja — `null` bedeutet kein vorgelesener Wert vorhanden |
| `finished` | `private boolean` | Ob das `END`-Sentinel bereits gesehen wurde. Nur Consumer-Thread. | entfällt (primitiv) |
| `failureSurfaced` | `private boolean` | Ob ein Producer-Fehler bereits an den Consumer weitergegeben wurde (verhindert doppeltes Werfen). Laut Kommentar im Code bewusst **nicht** `volatile`, da nur vom Consumer-Thread gelesen/geschrieben. | entfällt (primitiv) |

# Thread-Safety

**Zwei-Thread-Design, dokumentiert und verifiziert:** ein Daemon-Producer-Thread befüllt eine
begrenzte `ArrayBlockingQueue` (Kapazität `2048`), ein Consumer-Thread (der Aufrufer von
`hasNext()`/`next()`) liest daraus. Die Klasse selbst ist **nicht** für gleichzeitigen Zugriff
mehrerer Consumer-Threads gedacht (nur ein Konsument ist vorgesehen). Zustand ist bewusst in
"nur vom Producer", "nur vom Consumer" und "von beiden" (`volatile`) unterteilt: `closed` und
`failure` sind `volatile` für sichere Cross-Thread-Sichtbarkeit; `nextRow`/`finished`/
`failureSurfaced` sind bewusst **nicht** `volatile`, da sie ausschließlich vom Consumer-Thread
berührt werden. Der Vertrag verlangt, dass die zugrunde liegende Quelle Interrupts
respektiert; `close()` ist best-effort (siehe Überblick).

# Serialisierung

Nicht `Serializable` — `PrefetchingRowIterator` implementiert kein Serialisierungs-Interface
(verifiziert: `final class PrefetchingRowIterator implements CloseableIterator<Row>`, keine
`Serializable`-Implementierung in der Kette).

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben — es gilt die Identitätssemantik von
`java.lang.Object`. Als Single-Use-Iterator mit internem Thread-Zustand ist Werte-Gleichheit
ohnehin nicht sinnvoll definierbar.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `final class PrefetchingRowIterator implements
CloseableIterator<Row>` — implementiert das projekteigene Interface
[CloseableIterator](/api-reference/closeable-iterator/closeable-iterator.md) (das seinerseits `Iterator<T>` und
`Closeable` erweitert); keine eigene Oberklasse außer `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, daher nicht
erweiterbar, und kein anderer Typ implementiert `PrefetchingRowIterator` (es ist keine
Schnittstelle). Siehe [CloseableIterator](/api-reference/closeable-iterator/closeable-iterator.md) für die
vollständige Implementierer-Liste dieses gemeinsamen Interfaces (u. a. auch
`ExternalMergeSort.MergeIterator`).

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``boolean hasNext()``](./has-next.md)
- [``Row next()``](./next.md)
- [``void close()``](./close.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/PrefetchingRowIterator.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
