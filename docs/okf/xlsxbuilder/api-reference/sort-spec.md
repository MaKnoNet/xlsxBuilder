---
type: API Reference
title: SortSpec
description: Paketinterner, unveränderlicher Record — die Sortier-Konfiguration eines Sheets (mehrstufige Sortierschlüssel + Out-of-Core-Parameter der External Merge Sort).
resource: src/main/java/de/makno/xlsxbuilder/SortSpec.java
tags: [api-reference, record, value-object, sorting, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`record SortSpec(List<SortKey> sortKeys, int sortChunkSize, Path sortTempDir)` — paketintern.
Unveränderliche Sortier-Konfiguration: leere `sortKeys` bedeutet "nicht sortieren". Näher
beschrieben in [Konfigurationsobjekte](/components/configuration-models.md).

# Konstruktoren

## Kanonischer Record-Konstruktor (kein kompakter Validierungs-Konstruktor im Quelltext)

| Parameter | Typ | null-erlaubt | Bedeutung |
|---|---|---|---|
| `sortKeys` | `List<SortKey>` | **nicht geprüft durch den Record selbst** — kein `requireNonNull`, **keine defensive Kopie** (anders als z. B. [SheetWriteOptions](/api-reference/sheet-write-options.md), das `List.copyOf` im kompakten Konstruktor verwendet); ein `null`-Argument wird kommentarlos gespeichert und führt erst bei `sortKeys().isEmpty()` (z. B. in `SheetRenderer.render`) zu einer `NullPointerException` | leer = keine Sortierung |
| `sortChunkSize` | `int` | primitiv, **nicht auf `>= 1` geprüft in diesem Record** — die Validierung erfolgt vorgelagert in `XlsxBuilder.sortChunkSize(int)` (dort `IllegalArgumentException`, wenn `< 1`), nicht hier | Zeilen pro Sortier-Chunk im Speicher, bevor auf Platte gespillt wird |
| `sortTempDir` | `Path` | **ja** — `null` bedeutet System-Temp | Verzeichnis für gespillte Sort-Runs |

Verhalten bei ungültiger Eingabe: **keine eigene Validierung** in diesem Record — ein direkt
(außerhalb der normalen `XlsxBuilder`-Konfiguration) konstruierter `SortSpec` mit
`sortChunkSize = 0` oder `sortKeys = null` würde vom Record anstandslos akzeptiert und erst bei
späterer Verwendung fehlschlagen (z. B. `ExternalMergeSort`-Konstruktor prüft `chunkSize >= 1`
selbst nochmal separat).

# Methoden

Als Record automatisch generiert: `sortKeys()`, `sortChunkSize()`, `sortTempDir()` — jeweils
unveränderte Rückgabe des im Konstruktor übergebenen Werts (keine Kopie).

- `sortKeys()`: laut Vertrag nie `null` bei normaler Nutzung über `XlsxBuilder` (dort stets
  `List.copyOf(sortKeys)` übergeben, mindestens die leere Liste); der Record selbst garantiert
  das aber nicht.
- `sortChunkSize()`: primitiv, nie `null`.
- `sortTempDir()`: **kann `null` sein** (bedeutet System-Temp).

Keine der generierten Methoden wirft eine Exception.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SortSpec.java`
[2] [Konfigurationsobjekte](/components/configuration-models.md)
