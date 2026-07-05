---
type: API Reference
title: SortKey
description: Öffentlicher, unveränderlicher Record — eine Sortierstufe (Spaltenname + Richtung); mehrere Keys ergeben eine mehrstufige Sortierung.
resource: src/main/java/de/makno/xlsxbuilder/SortKey.java
tags: [api-reference, record, value-object, sorting]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`public record SortKey(String columnName, SortOrder order)` — eine Sortierstufe. Mehrere Keys
in einer Liste (siehe [XlsxBuilder.sortBy](/api-reference/xlsx-builder/sort-by.md)) ergeben eine
mehrstufige Sortierung, ausgewertet von
[RowComparator](/api-reference/row-comparator/row-comparator.md).

# Felder

Felder = Record-Komponenten, siehe [Konstruktor](./constructor.md).

# Thread-Safety

Immutable — beide Komponenten sind `final` (implizit bei Records) und werden im kompakten
Konstruktor auf Nicht-`null` geprüft; keine Setter, kein veränderlicher Zustand. Beliebig
zwischen Threads teilbar.

# Serialisierung

Nicht `Serializable` — `SortKey` implementiert kein Serialisierungs-Interface (verifiziert:
`public record SortKey(String columnName, SortOrder order)`, keine `implements`-Klausel).

# equals/hashCode/toString

Automatisch generiert (Record): komponentenbasiert (`columnName` und `order`), keine eigenen
Overrides im Quellcode.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `public record SortKey(String columnName, SortOrder order)` —
erweitert implizit `java.lang.Record`; keine `implements`-Klausel im Quelltext.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; Records sind implizit `final`.
Referenziert [SortOrder](/api-reference/sort-order/sort-order.md) als Komponenten-Typ (Komposition, keine
Vererbung). Wird als Element-/Parametertyp in `XlsxBuilder.sortBy(...)` und
[RowComparator](/api-reference/row-comparator/row-comparator.md) verwendet — ebenfalls reine Verwendung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``String columnName()``](./column-name.md)
- [``SortOrder order()``](./order.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SortKey.java`
