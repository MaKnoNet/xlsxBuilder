---
type: API Reference
title: XlsxBuilder.maxRowsPerSheet(int)
description: Paketinterner Test-Seam - senkt das Zeilenlimit pro Sheet, damit Split-/Limit-Verhalten ohne Millionen Zeilen testbar ist.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method, test-seam]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> maxRowsPerSheet(int maxRows)` (paketintern)

**Neu ergänzt bei der Restrukturierung** — fehlte in der vorherigen flachen
`api-reference/xlsx-builder.md` (verifiziert gegen den Quellcode, Zeile 301). Test-Seam:
senkt das Zeilenlimit pro Sheet, damit das Limit-/Split-Verhalten testbar ist, ohne
tatsächlich Millionen Zeilen zu erzeugen. Bewusst nicht `public` — außerhalb von Tests gilt
immer Excels `SpreadsheetVersion.EXCEL2007`-Maximum (1.048.576 Zeilen).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `maxRows` | `int` | primitiv; muss `>= 1` sein |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
`IllegalArgumentException("maxRows must be >= 1")`, wenn `maxRows < 1`.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
