---
type: API Reference
title: SheetWriteOptions – Konstruktoren
description: Alle Konstruktoren von SheetWriteOptions.
resource: src/main/java/de/makno/xlsxbuilder/SheetWriteOptions.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## Kompakter Konstruktor `SheetWriteOptions { ... }`

Erzwingt defensive, unveränderliche Kopien der Kollektions-Komponenten, damit die Optionen ein
echter unveränderlicher Value-Type sind: ein Aufrufer, der die übergebenen Kollektionen noch
referenziert, kann das Layout nicht nachträglich mutieren, und die Zugriffsmethoden geben
Views heraus, die selbst nicht modifizierbar sind.

| Parameter | Typ | null-erlaubt | Verhalten bei ungültiger Eingabe |
|---|---|---|---|
| `headerLines` | `List<String>` | **ja** — bleibt `null`, wenn `null` übergeben wird (optional per Vertrag); sonst `List.copyOf(headerLines)` | `NullPointerException`, falls die Liste selbst nicht `null`, aber ein enthaltenes Element `null` ist (`List.copyOf` verbietet `null`-Elemente) |
| `footerLines` | `List<String>` | **nein** — laut Doku "required to be non-null"; `List.copyOf(footerLines)` ohne vorherigen expliziten Null-Check | `NullPointerException` bei `footerLines == null` (ausgelöst von `List.copyOf`, nicht durch einen eigenen `requireNonNull`-Aufruf mit sprechender Meldung) |
| `columnGroups` | `List<ColumnGroup>` | **nein** — analog, `List.copyOf(columnGroups)` | `NullPointerException` bei `columnGroups == null` |
| `placeholders` | `Map<String, String>` | **nein** — analog, `Map.copyOf(placeholders)` | `NullPointerException` bei `placeholders == null` |
| `placeholderResolver` | `Function<String, String>` | ja — unverändert übernommen, keine Kopie/Prüfung | keine |
| `showColumnHeaders` | `boolean` | primitiv | keine |
| `defaultNullText` | `String` | ja | keine |
| `splitOnRowLimit` | `boolean` | primitiv | keine |
| `splitSheetNamer` | `SplitSheetNamer` | ja | keine |
| `maxRowsPerSheet` | `int` | primitiv, nicht auf Wertebereich geprüft in diesem Record (die Validierung `>= 1` erfolgt vorgelagert in `XlsxBuilder.maxRowsPerSheet(int)`, nicht hier) | keine |

Zusammengefasst: `footerLines`, `columnGroups` und `placeholders` lösen bei `null` eine
`NullPointerException` über `List.copyOf`/`Map.copyOf` aus — das ist funktional korrekt zum
dokumentierten "required to be non-null"-Vertrag, aber die Exception-Meldung stammt aus der
JDK-Bibliotheksmethode, nicht aus einem eigenen `Objects.requireNonNull(x, "name")`-Aufruf mit
spezifischem Parameternamen.

# Citations

[1] [SheetWriteOptions (Übersicht)](./sheet-write-options.md)
