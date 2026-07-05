---
type: API Reference
title: WorkbookBuilder.sxssfRowWindow(...)
description: Methode sxssfRowWindow von WorkbookBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/WorkbookBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `WorkbookBuilder sxssfRowWindow(int window)`


Setzt die Anzahl der Zeilen, die SXSSF pro Sheet gleichzeitig im Speicher haelt (Rest wird auf
Temp-Dateien gespillt). Default: `100` (`DEFAULT_ROW_WINDOW`).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `window` | `int` | primitiv; muss `>= 1` sein |

Rueckgabewert: `this` (fluentes API), nie `null`. Geworfene Exceptions:
`IllegalArgumentException("sxssfRowWindow must be >= 1")`, wenn `window < 1` (verifiziert exakt
gegen den Code).

# Citations

[1] [WorkbookBuilder (Übersicht)](./workbook-builder.md)
