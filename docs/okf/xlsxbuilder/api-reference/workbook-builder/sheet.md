---
type: API Reference
title: WorkbookBuilder.sheet(...)
description: Methode sheet von WorkbookBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/WorkbookBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `WorkbookBuilder sheet(XlsxBuilder<?> sheet)`


Fuegt ein Sheet hinzu. Der uebergebene `XlsxBuilder` muss eine Datenquelle besitzen
(`.data(...)`) - diese Prufung erfolgt allerdings **nicht** hier, sondern erst spaeter beim
Rendern in [XlsxBuilder.renderInto(...)](/api-reference/xlsx-builder/render-into.md).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `sheet` | `XlsxBuilder<?>` | **nein** - `Objects.requireNonNull(sheet, "sheet")` |

Rueckgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`sheet == null`.

# Citations

[1] [WorkbookBuilder (Übersicht)](./workbook-builder.md)
