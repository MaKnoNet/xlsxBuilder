---
type: Architecture Concept
title: Fehlerbehandlung (RowLimitExceededException, DataAccessException)
description: Fail-fast bei Zeilenlimit-Ueberschreitung und geprueften SQL-Fehlern in ungecheckten DataProvider-Methoden; dank atomarem write(Path) nie ein Teil-File.
resource: src/main/java/de/makno/xlsxbuilder/RowLimitExceededException.java
tags: [architecture, error-handling, exceptions]
timestamp: '2026-07-05T09:30:00+02:00'
---

# Überblick

| Exception | Wann | Verhalten |
|---|---|---|
| `RowLimitExceededException` | Sheet überschreitet das Excel-Zeilenlimit (1.048.576, inkl. Titel-/Gruppen-/Kopfzeilen sowie der für Summenzeile/Fußzeilen reservierten Zeilen) und ist nicht auf Split konfiguriert | `extends IllegalStateException`; nur paketintern werfbar. Mit `splitOnRowLimit(true)` (siehe [Konfigurationsobjekte](/components/configuration-models.md)) wird stattdessen auf Folge-Sheets fortgesetzt |
| `DataAccessException` | Eine `SQLException` tritt in `DataProvider`-Methoden (`hasNext()`/`next()`) auf, deren Signatur keine geprüfte Exception erlaubt | ungechecktes Wrapping der ursprünglichen `SQLException` als Ursache |

**Kein Teil-File dank atomarem Write:** `WorkbookBuilder.write(Path)` schreibt zunächst in
eine Temp-Datei und verschiebt sie erst nach erfolgreichem Schreiben auf den Zielpfad — ein
fehlgeschlagener Export (z. B. durch eine der beiden obigen Exceptions) hinterlässt daher
nie eine unvollständige `.xlsx` und überschreibt nie eine zuvor gültige Datei.

# Citations

[1] [README - Row-limit handling, Atomic write](https://github.com/MaKnoNet/xlsxBuilder/blob/main/README.md)
