---
type: API Reference
title: SplitSheetNamer.partSheetName(...)
description: Methode partSheetName von SplitSheetNamer - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/SplitSheetNamer.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `String partSheetName(String baseSheetName, int partNumber)`


| Parameter | Typ | null-erlaubt |
|---|---|---|
| `baseSheetName` | `String` | nicht durch das Interface selbst geprüft; der einzige Aufrufer (`XlsxWriter.partSheetName`) übergibt stets das über `XlsxBuilder.sheetName(String)` gesetzte, dort bereits `requireNonNull`-geprüfte Feld — praktisch nie `null` |
| `partNumber` | `int` | primitiv; laut Doku `2` für das erste Folge-Sheet (das Basis-Sheet ist Teil 1), passend zum Default-Suffix `" (2)"` |

Rückgabewert: der Sheet-Name für das Folge-Sheet. **Laut Vertrag darf die Implementierung nicht
`null` oder leer zurückgeben** — verifiziert gegen den einzigen Aufrufer
(`XlsxWriter.partSheetName`): dort wird geprüft `if (name == null || name.isBlank())` und in
diesem Fall eine `IllegalStateException("SplitSheetNamer returned no name for part " +
partNumber + " of sheet '" + baseSheetName + "'")` geworfen — die Prüfung erfolgt also **beim
Aufrufer**, nicht im Interface selbst. Der zurückgegebene Name muss außerdem im Workbook
eindeutig sein; ist er es nicht (nach Anwendung von `WorkbookUtil.createSafeSheetName`), wirft
der Aufrufer ebenfalls `IllegalStateException` ("... but a sheet with that name already exists
in the workbook").

Geworfene Exceptions: das Interface selbst deklariert keine (`throws`-Klausel fehlt); eine
konkrete Implementierung (Lambda) kann jede `RuntimeException` werfen, die dann ungefangen beim
Aufrufer (`XlsxWriter.startSheet`/`partSheetName`) propagiert.

# Citations

[1] [SplitSheetNamer (Übersicht)](./split-sheet-namer.md)
