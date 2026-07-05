---
type: API Reference
title: Row
description: Paketinterne projizierte Datenzeile — bereits extrahierte Zellwerte, ein Wert je Spalte; Serializable für den Spill der ExternalMergeSort.
resource: src/main/java/de/makno/xlsxbuilder/Row.java
tags: [api-reference, value-object, serialization, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`final class Row implements Serializable` — paketintern, nicht Teil der öffentlichen API. Die
bereits extrahierten Zellwerte, ein Wert pro Spalte. `Serializable`, damit
[ExternalMergeSort](/api-reference/external-merge-sort/external-merge-sort.md) ganze Runs auf Temp-Dateien spillen
kann; die enthaltenen Werttypen (String, Long, Double, BigDecimal, Boolean, LocalDate/-Time, …)
sind selbst `Serializable` — der ursprüngliche Datentyp `T` muss es nicht sein. Besitzt eine
explizite `serialVersionUID = 1L`. Das Werte-Array wird **bewusst nicht kopiert** — Zeilen
werden intern einmal pro Datensatz auf dem Hot Path erzeugt und nie geteilt (kein
Aliasing-Risiko in der aktuellen Nutzung, aber auch keine defensive Kopie beim Konstruieren
oder Lesen).

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `serialVersionUID` | `private static final long` | Wert `1L`, explizit deklariert. | entfällt (primitiv) |
| `values` | `private final Object[]` | Die projizierten Zellwerte, ein Eintrag pro Spalte. Bewusst nicht defensiv kopiert (siehe Überblick). | Feld selbst nie `null` (immer im Konstruktor gesetzt); einzelne Einträge dürfen `null` sein (repräsentiert eine leere Zelle) |

# Thread-Safety

Kein expliziter Vertrag dokumentiert. Da `values` `final`, aber ein **veränderliches** Array
ist und **nicht** defensiv kopiert wird (bewusste Design-Entscheidung laut Überblick), ist
`Row` nur so unveränderlich, wie der Aufrufer es zulässt: solange das übergebene Array nach der
Konstruktion nicht mehr von außen verändert wird (was in der aktuellen Nutzung — ein Array pro
Datensatz, nie geteilt — zutrifft), ist die Instanz effektiv unveränderlich und zwischen
Threads teilbar (auch für den Spill/Merge der `ExternalMergeSort`). Ein Aufrufer, der das
Quell-Array nach Übergabe weiter mutiert, würde diese Garantie brechen.

# Serialisierung

`Serializable` mit expliziter `serialVersionUID = 1L` — verifiziert gegen den Quellcode. Die
Serialisierbarkeit hängt zur Laufzeit zusätzlich davon ab, dass jeder einzelne Eintrag in
`values` selbst `Serializable` ist; das ist für die von der Bibliothek unterstützten Zelltypen
gegeben (String, Long, Double, BigDecimal, Boolean, LocalDate/-Time, …). Für den
`RowCodec`-Fallback-Pfad (beliebige benutzerdefinierte `Serializable`-Typen über
`convertToColumnType`) siehe [RowCodec](/api-reference/row-codec/row-codec.md).

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben (verifiziert: keine `equals`/`hashCode`/`toString`-
Deklaration im Quellcode) — es gilt die Identitätssemantik von `java.lang.Object`. Insbesondere
**kein** `Arrays.equals`-basierter Vergleich der `values` — zwei `Row`-Instanzen mit
inhaltsgleichen Werten gelten als ungleich, sofern es nicht dieselbe Instanz ist. Da `Row`
paketintern und nur als Durchlaufobjekt verwendet wird (kein Einsatz in `equals`-sensitiven
Collections erkennbar), ist das ein verifizierter, aber praktisch unkritischer Befund.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `final class Row implements Serializable` — implementiert das
JDK-Marker-Interface `java.io.Serializable` direkt; keine eigene Oberklasse außer
`java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, daher nicht
erweiterbar. Wird als Feld-/Rückgabetyp u. a. in
[RowComparator](/api-reference/row-comparator/row-comparator.md) (`Comparator<Row>`),
[CloseableIterator](/api-reference/closeable-iterator/closeable-iterator.md)`<Row>`,
[RowCodec](/api-reference/row-codec/row-codec.md) und
[PrefetchingRowIterator](/api-reference/prefetching-row-iterator/prefetching-row-iterator.md) verwendet — reine
Typparameter-/Feldverwendung, keine Vererbungsbeziehung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``Object get(int index)``](./get.md)
- [``int size()``](./size.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/Row.java`
[2] [DataProvider (Komponente) – Supporting types](/components/data-provider.md)
