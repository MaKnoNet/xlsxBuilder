---
type: API Reference
title: SheetRenderer.render(...)
description: Methode render von SheetRenderer - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/SheetRenderer.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

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

[1] [SheetRenderer (Übersicht)](./sheet-renderer.md)
