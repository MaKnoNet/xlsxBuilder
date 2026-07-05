---
type: API Reference
title: Column – Konstruktoren
description: Alle Konstruktoren von Column.
resource: src/main/java/de/makno/xlsxbuilder/Column.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## `Column(String name, ColumnType type, Function<? super T, ?> extractor)`

Delegiert an den privaten Vollkonstruktor mit `format = null`, `nullText = null`,
`converter = null`.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `name` | `String` | nein — `Objects.requireNonNull` im delegierten Konstruktor wirft `NullPointerException` |
| `type` | `ColumnType` | nein — ebenso `NullPointerException` |
| `extractor` | `Function<? super T, ?>` | nein — ebenso `NullPointerException` |

Geworfene Exceptions: `NullPointerException`, wenn `name`, `type` oder `extractor` `null` ist
(jeweils mit dem Parameternamen als Meldung, verifiziert im privaten Vollkonstruktor).

## `Column(String name, ColumnType type, String format, Function<? super T, ?> extractor)`

Delegiert an den privaten Vollkonstruktor mit `nullText = null`, `converter = null`.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `name` | `String` | nein |
| `type` | `ColumnType` | nein |
| `format` | `String` | **ja** — optionaler Excel-Formatcode (z. B. `"#,##0.00"`, `"dd.mm.yyyy"`, `"hh:mm:ss"`); `null` = Default-Format des Typs |
| `extractor` | `Function<? super T, ?>` | nein |

Geworfene Exceptions: `NullPointerException` bei `name`, `type` oder `extractor` == `null`.

## `private Column(String name, ColumnType type, String format, String nullText, Function<? super T, ?> extractor, Function<Object, Object> converter)`

Privater Vollkonstruktor, nur intern über die beiden obigen Konstruktoren und die `with*`-Methoden
erreichbar.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `name` | `String` | nein — `Objects.requireNonNull(name, "name")` |
| `type` | `ColumnType` | nein — `Objects.requireNonNull(type, "type")` |
| `format` | `String` | ja |
| `nullText` | `String` | ja |
| `extractor` | `Function<? super T, ?>` | nein — `Objects.requireNonNull(extractor, "extractor")` |
| `converter` | `Function<Object, Object>` | ja |

Geworfene Exceptions: `NullPointerException` bei `name`, `type` oder `extractor` == `null`.

# Citations

[1] [Column (Übersicht)](./column.md)
