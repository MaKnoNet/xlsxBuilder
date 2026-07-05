---
type: API Reference
title: ExternalMergeSort.sort(...)
description: Methode sort von ExternalMergeSort - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/ExternalMergeSort.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `CloseableIterator<Row> sort(Iterator<Row> source) throws IOException`


Konsumiert die Quelle vollständig (erzeugt dabei die Runs) und liefert den sortierten
Merge-Stream zurück.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `source` | `java.util.Iterator<Row>` | nicht explizit geprüft; `source.hasNext()`/`source.next()` wird direkt aufgerufen — ein `null`-Argument führt zu sofortiger `NullPointerException` beim ersten Zugriff |

Rückgabewert: `CloseableIterator<Row>` über die sortierten Zeilen, nie `null` (bei Erfolg wird
immer ein `MergeIterator` konstruiert und zurückgegeben).

Geworfene Exceptions:
- `IOException`, wenn das Anlegen des Temp-Verzeichnisses/der Run-Dateien fehlschlägt oder das
  Schreiben/Lesen der Run-Dateien fehlschlägt. **Bei Fehler wird `close()` aufgerufen** (alle
  bereits erzeugten Temp-Dateien werden gelöscht), bevor die `IOException` weitergeworfen wird
  — verifiziert im `catch (IOException e)`-Block.
- Eine `NullPointerException`, falls `comparator` (aus dem Konstruktor) `null` war und beim
  Sortieren des Puffers (`buffer.sort(comparator)`) verwendet wird.

# Citations

[1] [ExternalMergeSort (Übersicht)](./external-merge-sort.md)
