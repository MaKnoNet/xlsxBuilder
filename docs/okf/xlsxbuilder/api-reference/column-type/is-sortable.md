---
type: API Reference
title: ColumnType.isSortable(...)
description: Methode isSortable von ColumnType - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/ColumnType.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `boolean isSortable()`


Keine Parameter. Rückgabewert: `boolean`, primitiv, kann nicht `null` sein. `true` für alle
Typen außer `FORMULA` (verifiziert: nur `FORMULA(false)`, alle anderen neun Konstanten haben
`sortable = true`). Wird u. a. von
[XlsxBuilder.renderInto](/api-reference/xlsx-builder/render-into.md) genutzt, um nicht-sortierbare
Sortierspalten mit `IllegalArgumentException` abzulehnen. Keine Exceptions.

# Citations

[1] [ColumnType (Übersicht)](./column-type.md)
