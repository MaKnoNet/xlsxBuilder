---
type: API Reference
title: XlsxBuilder.header(...)
description: Methode header von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> header(String... lines)`


Optionale Titelzeile(n) über den Spaltenköpfen; jede Zeile wird über die volle Tabellenbreite
verschmolzen und zentriert dargestellt. Wiederholter Aufruf hängt weitere Titelzeilen an.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `lines` | `String...` | ein explizit `null` übergebenes Array würde in der `for`-Schleife eine `NullPointerException` auslösen (Randfall bei Varargs); **jedes einzelne Element** `line` ist nicht erlaubt: `Objects.requireNonNull(line, "line")` pro Element |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn ein
Element von `lines` `null` ist.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
