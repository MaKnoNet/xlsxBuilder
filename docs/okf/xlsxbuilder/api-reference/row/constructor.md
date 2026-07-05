---
type: API Reference
title: Row – Konstruktoren
description: Alle Konstruktoren von Row.
resource: src/main/java/de/makno/xlsxbuilder/Row.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## `Row(Object[] values)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `values` | `Object[]` | **nicht geprüft** — kein `requireNonNull`; ein `null`-Array wird kommentarlos im Feld gespeichert. Der erste Zugriff über `get(int)` oder `size()` löst dann eine `NullPointerException` aus (Zugriff auf `values.length`/`values[index]` auf `null`) |

Verhalten bei ungültiger Eingabe: keine sofortige Validierung; die Klasse verlässt sich
vollständig darauf, dass der einzige interne Erzeuger (`SheetRenderer.project(...)`) stets ein
frisch alloziertes, nie-`null`-Array der korrekten Spaltenanzahl übergibt. Einzelne Elemente des
Arrays dürfen `null` sein (repräsentiert eine `null`-Zelle) — das ist der Normalfall, kein
Fehlerzustand.

# Citations

[1] [Row (Übersicht)](./row.md)
