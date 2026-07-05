---
type: API Reference
title: XlsxBuilder.isNumeric(ColumnType)
description: Private Hilfsmethode - klassifiziert, ob ein ColumnType summierbar (numerisch) ist.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static boolean isNumeric(ColumnType type)`

Reine interne Hilfsmethode ohne eigene Validierung; liefert `true` für
`INTEGER, LONG, DOUBLE, DECIMAL`, sonst `false` (`switch`-Ausdruck mit `default -> false`).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
