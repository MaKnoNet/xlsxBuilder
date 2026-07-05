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
Führt einen [RenderJob](/api-reference/render-job.md) aus: projiziert die (gefilterten)
Datensätze gestreamt auf [Row](/api-reference/row.md)s, sortiert bei Bedarf Out-of-Core über
[ExternalMergeSort](/api-reference/external-merge-sort.md), überlappt optional Lesen/Sortieren
mit Schreiben ([PrefetchingRowIterator](/api-reference/prefetching-row-iterator.md)) und
schreibt das Sheet über den [XlsxWriter](/api-reference/xlsx-writer.md) ins Workbook. Gegenstück
zur Konfigurationsseite ([XlsxBuilder](/api-reference/xlsx-builder.md)).

**Zustandslos:** alle Eingaben leben im `RenderJob`; die vorwärts-lesbare Datenquelle wird genau
einmal konsumiert und vom Renderer selbst geschlossen (`try (DataProvider<T> provider =
job.dataProvider())`).

# Konstruktoren

## `private SheetRenderer()`

Leerer privater Konstruktor, verhindert Instanziierung. Keine Parameter, keine Exceptions.

# Methoden

## `static <T> int render(SXSSFWorkbook wb, RenderJob<T> job) throws IOException`

Schreibt das durch `job` beschriebene Sheet in `wb`.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `wb` | `SXSSFWorkbook` | nicht geprüft — `null` führt zu `NullPointerException`, sobald `wb` an `XlsxWriter.addSheet(wb, ...)` weitergereicht und dort verwendet wird |
| `job` | `RenderJob<T>` | nicht geprüft — `null` führt sofort zu `NullPointerException` bei `job.sort()` |

Rückgabewert: Anzahl der geschriebenen Datenzeilen (`int`, primitiv, nie `null`) — für
Performance-Logs.

Geworfene Exceptions:
- `IOException`, wenn `ExternalMergeSort.sort(...)` oder das Schreiben über `XlsxWriter`
  fehlschlägt (deklariert, propagiert unverändert).
- `NullPointerException` bei `wb == null` oder `job == null` (nicht dokumentiert, aber durch
  fehlenden Null-Check verifiziert).
- Alle Exceptions, die aus dem sortierten/unsortierten Schreibpfad propagieren, u. a.
  `RowLimitExceededException` (aus `XlsxWriter`, wenn kein Split konfiguriert ist),
  `IllegalArgumentException` (aus `RowComparator`, z. B. bei nicht vergleichbaren Werten).
- Ressourcen-Handling: der `DataProvider` wird in jedem Fall (Erfolg oder Fehler) über
  try-with-resources geschlossen; bei aktivierter Sortierung wird zusätzlich der
  `ExternalMergeSort`/der sortierte Iterator try-with-resources geschlossen — verifiziert
  gegen die verschachtelten `try`-Blöcke.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SheetRenderer.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
