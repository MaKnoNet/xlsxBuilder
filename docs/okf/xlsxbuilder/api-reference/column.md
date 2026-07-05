---
type: API Reference
title: Column
description: Paketinterner, unveränderlicher Value-Type für eine Tabellenspalte — Name, logischer Typ, Format, Null-Text, Extractor und optionaler Converter.
resource: src/main/java/de/makno/xlsxbuilder/Column.java
tags: [api-reference, value-object, immutability, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`Column<T>` ist **paketintern** (kein Teil der öffentlichen API) — Spalten werden ausschließlich
über die fluente [XlsxBuilder](/api-reference/xlsx-builder.md)`.column(...)`-API definiert.
Unveränderlicher Value-Type: optionale Attribute (`type`, `format`, `nullText`, `converter`)
werden nicht über Setter, sondern über `with*`-Methoden als neue Instanz (Copy-on-Write)
gesetzt — das hält den zur Renderzeit (`XlsxBuilder.renderInto`) gezogenen Spalten-Snapshot
isoliert von einer späteren Rekonfiguration des Builders (relevant für den
Multi-User-/Multi-Thread-Zielbetrieb, siehe
[Concurrency contract](/architecture/concurrency-contract.md)).

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `final class Column<T>` — keine `extends`-/`implements`-Klausel
im Quelltext; erweitert implizit nur `java.lang.Object` und implementiert keine Interfaces (kein
`Comparable`, kein `Serializable`).

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts — `final`, daher grundsätzlich
nicht erweiterbar, und kein anderer Typ implementiert `Column` (es ist keine Schnittstelle). Wird
in `XlsxWriter`, `RowComparator`, `XlsxBuilder` u. a. lediglich als **Feld-/Parametertyp**
verwendet — das ist Verwendung, keine Vererbungsbeziehung, und daher hier nicht aufgeführt.

# Konstruktoren

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

# Methoden

## `String name()`

Keine Parameter. Rückgabewert: Spaltenname, nie `null` (durch Konstruktor-Invariante
garantiert).

## `ColumnType type()`

Keine Parameter. Rückgabewert: logischer Spaltentyp, nie `null`.

## `String format()`

Keine Parameter. Rückgabewert: optionaler Excel-Formatcode, **kann `null` sein** (bedeutet:
Default-Format des Typs wird verwendet, siehe `XlsxWriter.defaultFormat`).

## `String nullText()`

Keine Parameter. Rückgabewert: spaltenspezifischer Platzhaltertext für `null`-Werte, **kann
`null` sein** (bedeutet: sheet-weiter Default aus `SheetWriteOptions.defaultNullText()` greift).

## `Column<T> withType(ColumnType type)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `type` | `ColumnType` | nein — `Objects.requireNonNull(type, "type")` |

Rückgabewert: neue `Column<T>`-Instanz mit geändertem Typ, nie `null`. Geworfene Exceptions:
`NullPointerException` bei `type == null`.

## `Column<T> withFormat(String format)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `format` | `String` | ja |

Rückgabewert: neue `Column<T>`-Instanz mit geändertem Format, nie `null`. Keine Exceptions.

## `Column<T> withNullText(String nullText)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `nullText` | `String` | ja |

Rückgabewert: neue `Column<T>`-Instanz mit geändertem Null-Text, nie `null`. Keine Exceptions.

## `Column<T> withConverter(Function<Object, Object> converter)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `converter` | `Function<Object, Object>` | ja |

Rückgabewert: neue `Column<T>`-Instanz mit geändertem Converter, nie `null`. Keine Exceptions.

## `Object extract(T record)`

Liefert den Zellwert für den Datensatz.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `record` | `T` | wird direkt an `extractor.apply(record)` durchgereicht; ob `null` erlaubt ist, hängt vom konkreten Extractor ab — die Methode selbst prüft nicht auf `null` |

Rückgabewert: extrahierter (ggf. konvertierter) Zellwert, **kann `null` sein** — wenn der
Extractor `null` liefert, wird der Converter (falls gesetzt) **nicht** aufgerufen und `null`
direkt zurückgegeben (verifiziert: `if (value == null || converter == null) return value;`).
Ist ein Converter gesetzt und der extrahierte Wert nicht `null`, wird `converter.apply(value)`
angewendet und dessen Ergebnis zurückgegeben (kann theoretisch wieder `null` sein, falls der
Converter das tut). Geworfene Exceptions: keine eigenen; eine `RuntimeException` aus
`extractor.apply(...)` oder `converter.apply(...)` propagiert ungefangen.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/Column.java`
