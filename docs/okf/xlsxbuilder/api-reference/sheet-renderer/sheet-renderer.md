---
type: API Reference
title: SheetRenderer
description: Paketinterner, zustandsloser Ausführer eines RenderJob — Projektion, optionale Out-of-Core-Sortierung, optionales Prefetching, Schreiben via XlsxWriter.
resource: src/main/java/de/makno/xlsxbuilder/SheetRenderer.java
tags: [api-reference, orchestration, streaming, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`final class SheetRenderer` — paketintern, nicht instanziierbar (privater No-Op-Konstruktor).
Führt einen [RenderJob](/api-reference/render-job/render-job.md) aus: projiziert die (gefilterten)
Datensätze gestreamt auf [Row](/api-reference/row/row.md)s, sortiert bei Bedarf Out-of-Core über
[ExternalMergeSort](/api-reference/external-merge-sort/external-merge-sort.md), überlappt optional Lesen/Sortieren
mit Schreiben ([PrefetchingRowIterator](/api-reference/prefetching-row-iterator/prefetching-row-iterator.md)) und
schreibt das Sheet über den [XlsxWriter](/api-reference/xlsx-writer/xlsx-writer.md) ins Workbook. Gegenstück
zur Konfigurationsseite ([XlsxBuilder](/api-reference/xlsx-builder/xlsx-builder.md)).

**Zustandslos:** alle Eingaben leben im `RenderJob`; die vorwärts-lesbare Datenquelle wird genau
einmal konsumiert und vom Renderer selbst geschlossen (`try (DataProvider<T> provider =
job.dataProvider())`).

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `LOG` | `private static final Logger` | Log4j2-Logger dieser Klasse. | nein |

# Thread-Safety

Zustandslos — die einzige Instanzmethode ist `static`, keine Instanzfelder außer dem
statischen `LOG`. `render(...)` ist damit parallel aus mehreren Threads für unabhängige
`RenderJob`-Instanzen aufrufbar; jeder Aufruf konsumiert seinen eigenen `DataProvider`
exklusiv (kein geteilter Zustand zwischen parallelen `render(...)`-Aufrufen).

# Serialisierung

Nicht `Serializable` — `SheetRenderer` implementiert kein Serialisierungs-Interface
(verifiziert: `final class SheetRenderer`, keine `implements`-Klausel).

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben — es gilt die Identitätssemantik von
`java.lang.Object`. Da die Klasse nicht instanziierbar ist (privater Konstruktor), ist das ohne
praktische Relevanz.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `final class SheetRenderer` — keine
`extends`-/`implements`-Klausel; erweitert implizit nur `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, nicht instanziierbar
(privater Konstruktor).

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``static <T> int render(SXSSFWorkbook wb, RenderJob<T> job) throws IOException``](./render.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SheetRenderer.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
