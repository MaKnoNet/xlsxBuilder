---
type: API Reference
title: SortOrder
description: Öffentliches Enum für die Sortierrichtung — ASC oder DESC.
resource: src/main/java/de/makno/xlsxbuilder/SortOrder.java
tags: [api-reference, enum, sorting]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`public enum SortOrder { ASC, DESC }` — die Sortierrichtung, verwendet von
[SortKey](/api-reference/sort-key/sort-key.md) und ausgewertet von
[RowComparator](/api-reference/row-comparator/row-comparator.md) (bei `DESC` wird der Gesamtvergleich
inklusive Null-Behandlung negiert). Enum-Konstanten sind implizit unveränderlich und
thread-sicher.

# Felder

Keine eigenen Felder — reines Enum ohne zusätzliche Instanzattribute (anders als
[ColumnType](/api-reference/column-type/column-type.md), das ein `sortable`-Flag trägt).

# Thread-Safety

Immutable — Enum-Konstanten sind JVM-garantierte Singletons; beliebig zwischen Threads
teilbar, keine Synchronisation nötig.

# Serialisierung

Implizit `Serializable` — jedes Java-`enum` erbt transitiv `java.io.Serializable` über
`java.lang.Enum` (verifiziert: kein eigenes `implements Serializable` im Quelltext, aber im
Deklarations-Sinn geerbt). Enum-Serialisierung folgt dem JDK-Spezialfall (nur der
Konstantenname wird geschrieben, keine `serialVersionUID`-Pflicht).

# equals/hashCode/toString

Keine eigenen Overrides im Quellcode; es gelten die von `java.lang.Enum` geerbten
Implementierungen: identitätsbasierte `equals`/`hashCode`, `toString()` liefert den
Konstantennamen (`"ASC"`/`"DESC"`).

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `public enum SortOrder { ASC, DESC }` — erweitert implizit
`java.lang.Enum<SortOrder>`; keine explizit implementierten Interfaces im Quelltext.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts — Enum-Typen sind implizit
`final`. Wird als Komponente von [SortKey](/api-reference/sort-key/sort-key.md) und als Parameter in
`XlsxBuilder.sortBy(String, SortOrder)` verwendet — keine Vererbungsbeziehung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden


# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SortOrder.java`
