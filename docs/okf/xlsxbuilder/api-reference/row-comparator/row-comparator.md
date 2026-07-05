---
type: API Reference
title: RowComparator
description: Paketinterner Comparator<Row> aus einer Liste von SortKeys — null-sicher, mehrstufig, ASC/DESC.
resource: src/main/java/de/makno/xlsxbuilder/RowComparator.java
tags: [api-reference, comparator, sorting, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`final class RowComparator implements Comparator<Row>` — paketintern. Baut einen `Comparator`
über projizierte [Row](/api-reference/row/row.md)s aus den
[SortKey](/api-reference/sort-key/sort-key.md)s. Vergleicht Zellwerte nach natürlicher Ordnung
(`Comparable`), null-sicher, unterstützt mehrstufige Sortierung sowie `DESC`.

**Null-Ordnung** (Javadoc, verifiziert korrekt): `null`-Werte werden bei `SortOrder.ASC` **zuletzt**
einsortiert. `DESC` negiert den gesamten Vergleich (inklusive der Null-Behandlung), sodass bei
`SortOrder.DESC` `null`-Werte **zuerst** erscheinen — die konventionelle Konsequenz der
Umkehrung eines "nulls-last"-Comparators.

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `indices` | `private final int[]` | Spaltenindex je Sortierstufe, im Konstruktor aus `SortKey.columnName()` aufgelöst. | nein — Array wird immer mit `sortKeys.size()` Einträgen angelegt |
| `descending` | `private final boolean[]` | Je Sortierstufe: `true`, wenn `SortOrder.DESC`. | nein |
| `columnNames` | `private final String[]` | Spaltenname je Sortierstufe, für Fehlermeldungen bei nicht vergleichbaren Werten. | nein |

# Thread-Safety

Immutable nach Konstruktion: alle drei Arrays werden im Konstruktor einmalig befüllt und danach
nie mutiert (kein Setter, kein Schreibzugriff in `compare(...)`). Da Java-Arrays selbst aber
technisch veränderlich sind und hier keine defensive Kopie nach außen gegeben wird (die Felder
sind `private`, also ohnehin nicht von außen erreichbar), ist die Instanz effektiv unveränderlich
und beliebig zwischen Threads teilbar — vorausgesetzt, die verglichenen `Row`-Werte sind selbst
unveränderlich (was für `Row` zutrifft, siehe [Row](/api-reference/row/row.md)).

# Serialisierung

Nicht `Serializable` — `RowComparator` implementiert kein Serialisierungs-Interface
(verifiziert: `final class RowComparator implements Comparator<Row>`; `Comparator` selbst ist
nicht `Serializable`, anders als z. B. `Comparator.naturalOrder()`-Implementierungen im JDK).

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben — es gilt die Identitätssemantik von
`java.lang.Object`. Da `Comparator`-Instanzen typischerweise nicht in `equals`-sensitiven
Kontexten verglichen werden, hat das praktisch geringe Relevanz.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `final class RowComparator implements Comparator<Row>` —
implementiert das JDK-Funktionsinterface `java.util.Comparator<Row>` direkt; keine eigene
Oberklasse außer `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, daher nicht
erweiterbar, und kein anderer Typ implementiert `RowComparator` (es ist keine Schnittstelle). Wird
in [ExternalMergeSort](/api-reference/external-merge-sort/external-merge-sort.md) als `Comparator<Row>`-Parameter
verwendet — Verwendung, keine Vererbung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``int compare(Row a, Row b)``](./compare.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/RowComparator.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
