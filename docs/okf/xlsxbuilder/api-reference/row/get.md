---
type: API Reference
title: Row.get(...)
description: Methode get von Row - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/Row.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `Object get(int index)`


| Parameter | Typ | null-erlaubt |
|---|---|---|
| `index` | `int` | primitiv; muss `0 <= index < size()` sein |

Rückgabewert: der Zellwert an Position `index`; **kann `null` sein** (repräsentiert eine leere
Zelle in der Originaldaten). Geworfene Exceptions:
`ArrayIndexOutOfBoundsException`, wenn `index` außerhalb des gültigen Bereichs liegt (direkter
Array-Zugriff `values[index]`, keine eigene Bereichsprüfung); `NullPointerException`, wenn das
zugrunde liegende `values`-Array selbst `null` ist (siehe Konstruktor-Hinweis oben).

# Citations

[1] [Row (Übersicht)](./row.md)
