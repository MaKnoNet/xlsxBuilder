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
Zelle verschmolzen). Siehe [XlsxBuilder.columnGroups(List)](/api-reference/xlsx-builder/column-groups.md) für
die Verwendung. Näher beschrieben in
[Konfigurationsobjekte](/components/configuration-models.md).

# Felder

Felder = Record-Komponenten, siehe [Konstruktor](./constructor.md).

# Thread-Safety

Immutable — beide Komponenten sind `final` (implizit bei Records) und werden im kompakten
Konstruktor validiert; keine Setter, kein veränderlicher Zustand. Beliebig zwischen Threads
teilbar.

# Serialisierung

Nicht `Serializable` — `ColumnGroup` implementiert kein Serialisierungs-Interface (verifiziert
gegen die Deklaration `public record ColumnGroup(String label, int span)`, keine
`implements`-Klausel).

# equals/hashCode/toString

Automatisch generiert (Record): komponentenbasiert (`label` und `span`), keine eigenen
Overrides im Quellcode.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `public record ColumnGroup(String label, int span)` — Records
erweitern implizit `java.lang.Record` (nicht `Object` direkt) und implementieren keine
Interfaces im Quelltext dieser Deklaration.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; Records sind implizit `final`
und können nicht weiter abgeleitet werden. Wird in `XlsxBuilder.columnGroups(List<ColumnGroup>)`
nur als **Parametertyp** verwendet — keine Vererbungsbeziehung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``String label()``](./label.md)
- [``int span()``](./span.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/ColumnGroup.java`
[2] [Konfigurationsobjekte](/components/configuration-models.md)
