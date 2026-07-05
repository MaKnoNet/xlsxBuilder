---
type: API Reference
title: WorkbookBuilder.tempDir(...)
description: Methode tempDir von WorkbookBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/WorkbookBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `WorkbookBuilder tempDir(Path dir)`


Setzt ein Standard-Basisverzeichnis fuer die temporaeren Sort-Run-Dateien (External Merge Sort)
aller Sheets dieses Workbooks. Ein Per-Sheet-`XlsxBuilder.sortTempDir(Path)` hat weiterhin
Vorrang.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `dir` | `Path` | **ja** - `null` (Default) bedeutet: Per-Sheet-Einstellung bzw. System-Temp (`java.io.tmpdir`) greift; nicht geprueft, direkt uebernommen |

Rueckgabewert: `this`, nie `null`. Keine Exceptions.

# Citations

[1] [WorkbookBuilder (Übersicht)](./workbook-builder.md)
