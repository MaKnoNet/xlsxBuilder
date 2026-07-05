---
type: API Reference
title: ColumnGroup
description: Unveränderlicher Record für eine Zelle der optionalen gruppierten Kopfzeile — Label plus Spanne über eine Anzahl Spalten.
resource: src/main/java/de/makno/xlsxbuilder/ColumnGroup.java
tags: [api-reference, record, value-object, configuration]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`ColumnGroup` ist ein öffentlicher, unveränderlicher `record` mit zwei Komponenten: `label`
(Gruppentext) und `span` (Anzahl der Spalten, die die Zelle überspannt; bei `span > 1` wird die
Zelle verschmolzen). Siehe [XlsxBuilder.columnGroups(List)](/api-reference/xlsx-builder.md) für
die Verwendung. Näher beschrieben in
[Konfigurationsobjekte](/components/configuration-models.md).

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `public record ColumnGroup(String label, int span)` — Records
erweitern implizit `java.lang.Record` (nicht `Object` direkt) und implementieren keine
Interfaces im Quelltext dieser Deklaration.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; Records sind implizit `final`
und können nicht weiter abgeleitet werden. Wird in `XlsxBuilder.columnGroups(List<ColumnGroup>)`
nur als **Parametertyp** verwendet — keine Vererbungsbeziehung.

# Konstruktoren

## Kompakter Konstruktor `ColumnGroup(String label, int span)`

Records haben keinen expliziten Konstruktor im Quelltext außer dem kompakten Validierungs-Konstruktor.

| Parameter | Typ | null-erlaubt | Verhalten bei ungültiger Eingabe |
|---|---|---|---|
| `label` | `String` | **nein** — `Objects.requireNonNull(label, "label")` | wirft `NullPointerException` |
| `span` | `int` | (primitiv, kein `null` möglich) | wirft `IllegalArgumentException("span must be >= 1: " + span)`, wenn `span < 1` |

Verifiziert gegen den Code: Der Javadoc-Kommentar am Record sagt „label darf leer, aber nie
`null` sein" — das stimmt exakt mit der `requireNonNull`-Prüfung überein; kein Widerspruch
gefunden.

# Methoden

Als Record werden `label()` und `span()` automatisch generiert (keine expliziten Methoden im
Quelltext):

## `String label()`

Keine Parameter. Rückgabewert: das Gruppen-Label, **nie `null`** (durch die
Konstruktor-Invariante garantiert), kann aber leer sein (`""`, für eine ungruppierte Spalte
laut Javadoc).

## `int span()`

Keine Parameter. Rückgabewert: Anzahl der überspannten Spalten, garantiert `>= 1`.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/ColumnGroup.java`
[2] [Konfigurationsobjekte](/components/configuration-models.md)
