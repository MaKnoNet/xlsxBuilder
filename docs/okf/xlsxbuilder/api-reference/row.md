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
[ExternalMergeSort](/api-reference/external-merge-sort.md) ganze Runs auf Temp-Dateien spillen
kann; die enthaltenen Werttypen (String, Long, Double, BigDecimal, Boolean, LocalDate/-Time, …)
sind selbst `Serializable` — der ursprüngliche Datentyp `T` muss es nicht sein. Besitzt eine
explizite `serialVersionUID = 1L`. Das Werte-Array wird **bewusst nicht kopiert** — Zeilen
werden intern einmal pro Datensatz auf dem Hot Path erzeugt und nie geteilt (kein
Aliasing-Risiko in der aktuellen Nutzung, aber auch keine defensive Kopie beim Konstruieren
oder Lesen).

# Konstruktoren

## `Row(Object[] values)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `values` | `Object[]` | **nicht geprüft** — kein `requireNonNull`; ein `null`-Array wird kommentarlos im Feld gespeichert. Der erste Zugriff über `get(int)` oder `size()` löst dann eine `NullPointerException` aus (Zugriff auf `values.length`/`values[index]` auf `null`) |

Verhalten bei ungültiger Eingabe: keine sofortige Validierung; die Klasse verlässt sich
vollständig darauf, dass der einzige interne Erzeuger (`SheetRenderer.project(...)`) stets ein
frisch alloziertes, nie-`null`-Array der korrekten Spaltenanzahl übergibt. Einzelne Elemente des
Arrays dürfen `null` sein (repräsentiert eine `null`-Zelle) — das ist der Normalfall, kein
Fehlerzustand.

# Methoden

## `Object get(int index)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `index` | `int` | primitiv; muss `0 <= index < size()` sein |

Rückgabewert: der Zellwert an Position `index`; **kann `null` sein** (repräsentiert eine leere
Zelle in der Originaldaten). Geworfene Exceptions:
`ArrayIndexOutOfBoundsException`, wenn `index` außerhalb des gültigen Bereichs liegt (direkter
Array-Zugriff `values[index]`, keine eigene Bereichsprüfung); `NullPointerException`, wenn das
zugrunde liegende `values`-Array selbst `null` ist (siehe Konstruktor-Hinweis oben).

## `int size()`

Keine Parameter. Rückgabewert: Anzahl der Spalten (Array-Länge), primitiv, kann nicht `null`
sein. Geworfene Exceptions: `NullPointerException`, wenn `values == null` (Zugriff auf
`values.length`).

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/Row.java`
[2] [DataProvider (Komponente) – Supporting types](/components/data-provider.md)
