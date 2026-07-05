---
type: API Reference
title: SortSpec – Konstruktoren
description: Alle Konstruktoren von SortSpec.
resource: src/main/java/de/makno/xlsxbuilder/SortSpec.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## Kanonischer Record-Konstruktor (kein kompakter Validierungs-Konstruktor im Quelltext)

| Parameter | Typ | null-erlaubt | Bedeutung |
|---|---|---|---|
| `sortKeys` | `List<SortKey>` | **nicht geprüft durch den Record selbst** — kein `requireNonNull`, **keine defensive Kopie** (anders als z. B. [SheetWriteOptions](/api-reference/sheet-write-options/sheet-write-options.md), das `List.copyOf` im kompakten Konstruktor verwendet); ein `null`-Argument wird kommentarlos gespeichert und führt erst bei `sortKeys().isEmpty()` (z. B. in `SheetRenderer.render`) zu einer `NullPointerException` | leer = keine Sortierung |
| `sortChunkSize` | `int` | primitiv, **nicht auf `>= 1` geprüft in diesem Record** — die Validierung erfolgt vorgelagert in `XlsxBuilder.sortChunkSize(int)` (dort `IllegalArgumentException`, wenn `< 1`), nicht hier | Zeilen pro Sortier-Chunk im Speicher, bevor auf Platte gespillt wird |
| `sortTempDir` | `Path` | **ja** — `null` bedeutet System-Temp | Verzeichnis für gespillte Sort-Runs |

Verhalten bei ungültiger Eingabe: **keine eigene Validierung** in diesem Record — ein direkt
(außerhalb der normalen `XlsxBuilder`-Konfiguration) konstruierter `SortSpec` mit
`sortChunkSize = 0` oder `sortKeys = null` würde vom Record anstandslos akzeptiert und erst bei
späterer Verwendung fehlschlagen (z. B. `ExternalMergeSort`-Konstruktor prüft `chunkSize >= 1`
selbst nochmal separat).

# Citations

[1] [SortSpec (Übersicht)](./sort-spec.md)
