---
type: API Reference
title: XlsxWriter.partSheetName(...)
description: Methode partSheetName von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private String partSheetName(int partNumber)`


Name eines Teil-Sheets: Teil 1 und das Default-Schema nutzen `uniqueSheetName` ("Name (2)", ...);
ein konfigurierter `SplitSheetNamer` benennt die Folge-Sheets selbst. Dessen Ergebnis wird
Excel-safe gemacht, aber bewusst **nicht** dedupliziert — ein Duplikat schlägt mit einer klaren
Exception fehl.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `partNumber` | `int` | primitiv, kein `null` möglich |

Rückgabewert: `String`, sheetsicherer Name, nie `null` bei erfolgreichem Rückgabepfad.

Geworfene Exceptions:
- `IllegalStateException("SplitSheetNamer returned no name for part " + partNumber + " of
  sheet '" + baseSheetName + "'")`, wenn der konfigurierte `SplitSheetNamer` `null` oder einen
  leeren/blanken Namen zurückgibt (verifiziert: `if (name == null || name.isBlank())`).
- `IllegalStateException("SplitSheetNamer returned the name '" + safeName + "' for part " +
  partNumber + " of sheet '" + baseSheetName + "', but a sheet with that name already exists in
  the workbook")`, wenn der (Excel-safe gemachte) Name bereits im Workbook existiert.

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
