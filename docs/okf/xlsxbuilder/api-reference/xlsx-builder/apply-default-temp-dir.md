---
type: API Reference
title: XlsxBuilder.applyDefaultTempDir(...)
description: Methode applyDefaultTempDir von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `void applyDefaultTempDir(Path defaultDir)` (paketintern)


Wendet ein workbook-weites Default-Sort-Temp-Verzeichnis an, sofern dieses Sheet noch kein
eigenes hat. Wird vom `WorkbookBuilder` vor dem Rendern aufgerufen.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `defaultDir` | `Path` | **ja** — bei `null` passiert nichts (Bedingung `defaultDir != null && sortTempDir == null`) |

Rückgabewert: `void`. Keine Exceptions.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
