---
type: API Reference
title: SplitSheetNamer
description: Öffentliches funktionales Interface zum Benennen von Folge-Sheets, die bei splitOnRowLimit(true) entstehen.
resource: src/main/java/de/makno/xlsxbuilder/SplitSheetNamer.java
tags: [api-reference, functional-interface, configuration]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`@FunctionalInterface public interface SplitSheetNamer` — benennt die Folge-Sheets, die beim
Splitten eines Sheets am Excel-Zeilenlimit entstehen (siehe
[XlsxBuilder.splitOnRowLimit(boolean)](/api-reference/xlsx-builder.md)). Der Namer wird **nur**
für Folge-Sheets konsultiert — das erste Sheet behält immer den über
`XlsxBuilder.sheetName(String)` konfigurierten Namen (beim Streaming ist ein Split erst bekannt,
sobald das erste Sheet voll ist). Ohne Namer gilt das Default-Schema `"Name (2)"`,
`"Name (3)"`, ...

Der zurückgegebene Name wird Excel-safe gemacht (ungültige Zeichen ersetzt, max. 31 Zeichen),
aber bewusst **nicht** dedupliziert: ein bereits existierender Name schlägt mit
`IllegalStateException` fehl, sodass der Aufrufer die Kontrolle über die tatsächlichen Namen
behält. Näher beschrieben in
[Konfigurationsobjekte](/components/configuration-models.md).

# Konstruktoren

Keine — funktionales Interface ohne Konstruktor.

# Methoden

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

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SplitSheetNamer.java`
[2] [Konfigurationsobjekte](/components/configuration-models.md)
