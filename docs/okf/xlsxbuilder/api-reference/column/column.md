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
über die fluente [XlsxBuilder.column(...)](/api-reference/xlsx-builder/column.md)-API definiert.
Unveränderlicher Value-Type: optionale Attribute (`type`, `format`, `nullText`, `converter`)
werden nicht über Setter, sondern über `with*`-Methoden als neue Instanz (Copy-on-Write)
gesetzt — das hält den zur Renderzeit ([XlsxBuilder.renderInto](/api-reference/xlsx-builder/render-into.md)) gezogenen Spalten-Snapshot
isoliert von einer späteren Rekonfiguration des Builders (relevant für den
Multi-User-/Multi-Thread-Zielbetrieb, siehe
[Concurrency contract](/architecture/concurrency-contract.md)).

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `name` | `final String` | Spaltenname (Header-Text/Lookup-Schlüssel). | nein — `Objects.requireNonNull(name, "name")` im privaten Vollkonstruktor |
| `type` | `final ColumnType` | Logischer Spaltentyp. | nein — `Objects.requireNonNull(type, "type")` |
| `format` | `final String` | Optionaler Excel-Formatcode (z. B. `"#,##0.00"`). | ja — `null` bedeutet Default-Format des Typs |
| `nullText` | `final String` | Spaltenspezifischer Platzhalter für `null`-Werte. | ja — `null` bedeutet sheet-weiten Default verwenden |
| `extractor` | `final Function<? super T, ?>` | Extrahiert den Rohwert aus dem Datensatz `T`. | nein — `Objects.requireNonNull(extractor, "extractor")` |
| `converter` | `final Function<Object, Object>` | Optionaler Wertkonverter, angewendet auf den extrahierten (nicht-`null`) Rohwert. | ja — `null` bedeutet keine Konvertierung |

# Thread-Safety

Immutable Value-Type: alle Felder sind `final`, optionale Attribute werden nicht mutiert,
sondern über `with*`-Methoden als neue Instanz zurückgegeben (Copy-on-Write). Eine `Column`-
Instanz ist damit beliebig zwischen Threads teilbar — vorausgesetzt, `extractor`/`converter`
sind selbst zustandslos bzw. thread-sicher (das liegt in der Verantwortung des Aufrufers, der
die Lambda übergibt).

# Serialisierung

Nicht `Serializable` — `Column<T>` implementiert kein Serialisierungs-Interface (verifiziert:
`final class Column<T>` ohne `implements`-Klausel). Da `extractor`/`converter` beliebige
Lambdas sein können, wäre eine Serialisierung ohnehin fragil.

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben (verifiziert: keine `equals`/`hashCode`/`toString`-
Deklaration im Quellcode) — es gilt die Identitätssemantik von `java.lang.Object`. Da
`Column` paketintern ist und nur innerhalb der Bibliothek als Snapshot verwendet wird
(kein Einsatz in `equals`-sensitiven Collections erkennbar), hat das praktisch geringe
Relevanz, wird hier aber als verifizierter Befund festgehalten.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `final class Column<T>` — keine `extends`-/`implements`-Klausel
im Quelltext; erweitert implizit nur `java.lang.Object` und implementiert keine Interfaces (kein
`Comparable`, kein `Serializable`).

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts — `final`, daher grundsätzlich
nicht erweiterbar, und kein anderer Typ implementiert `Column` (es ist keine Schnittstelle). Wird
in `XlsxWriter`, `RowComparator`, `XlsxBuilder` u. a. lediglich als **Feld-/Parametertyp**
verwendet — das ist Verwendung, keine Vererbungsbeziehung, und daher hier nicht aufgeführt.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``String name()``](./name.md)
- [``ColumnType type()``](./type.md)
- [``String format()``](./format.md)
- [``String nullText()``](./null-text.md)
- [``Column<T> withType(ColumnType type)``](./with-type.md)
- [``Column<T> withFormat(String format)``](./with-format.md)
- [``Column<T> withNullText(String nullText)``](./with-null-text.md)
- [``Column<T> withConverter(Function<Object, Object> converter)``](./with-converter.md)
- [``Object extract(T record)``](./extract.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/Column.java`
