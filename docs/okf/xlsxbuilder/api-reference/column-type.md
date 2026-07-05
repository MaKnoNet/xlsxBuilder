---
type: API Reference
title: ColumnType
description: Enum für den logischen Typ einer Spalte — steuert Zelltyp/-format beim Schreiben und die Sortierbarkeit.
resource: src/main/java/de/makno/xlsxbuilder/ColumnType.java
tags: [api-reference, enum, configuration]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`ColumnType` ist ein öffentliches Enum mit den Konstanten `STRING, INTEGER, LONG, DOUBLE,
DECIMAL, BOOLEAN, DATE, DATETIME, TIME, FORMULA`. Steuert, wie ein projizierter Wert als
Excel-Zelle geschrieben wird (Zelltyp/-format, siehe
[XlsxWriter](/api-reference/xlsx-writer.md)`.writeCell`) und wie beim Sortieren verglichen
wird. Enum-Konstanten sind implizit thread-safe (unveränderliche Singletons).

**Sicherheitshinweis zu `FORMULA`** (aus dem Javadoc, gegen den Code verifiziert — korrekt):
Der Formeltext wird wörtlich als Excel-Formel geschrieben (siehe
`XlsxWriter.writeCell`, Fall `FORMULA`: `cell.setCellFormula(...)`, kein Escaping) — niemals
aus nicht vertrauenswürdiger Eingabe zusammensetzen (Formel-Injection). Für Text, der nur wie
eine Formel aussieht, `STRING` verwenden.

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `public enum ColumnType` — Enums erweitern implizit
`java.lang.Enum<ColumnType>`; keine explizit implementierten Interfaces im Quelltext (über
`Enum` werden `Comparable<ColumnType>` und `Serializable` transitiv mitgebracht, aber nicht selbst
deklariert).

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts — Enum-Typen sind implizit
`final` und nicht erweiterbar. Wird u. a. in `Column`, `XlsxWriter` als Feld-/Parametertyp
verwendet — keine Vererbungsbeziehung.

# Konstruktoren

Kein öffentlicher Konstruktor (Enum). Der private Enum-Konstruktor `ColumnType(boolean
sortable)` wird nur von den Enum-Konstanten selbst aufgerufen (`STRING(true)` … `FORMULA(false)`)
und ist daher nicht Teil der aufrufbaren API.

# Methoden

## `boolean isSortable()`

Keine Parameter. Rückgabewert: `boolean`, primitiv, kann nicht `null` sein. `true` für alle
Typen außer `FORMULA` (verifiziert: nur `FORMULA(false)`, alle anderen neun Konstanten haben
`sortable = true`). Wird u. a. von
[XlsxBuilder.renderInto](/api-reference/xlsx-builder.md) genutzt, um nicht-sortierbare
Sortierspalten mit `IllegalArgumentException` abzulehnen. Keine Exceptions.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/ColumnType.java`
