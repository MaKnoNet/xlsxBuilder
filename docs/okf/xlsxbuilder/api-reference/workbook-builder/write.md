---
type: API Reference
title: WorkbookBuilder.write(...)
description: Methode write von WorkbookBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/WorkbookBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `void write(Path out) throws IOException`


Schreibt das Workbook atomar: zunaechst als Temp-Datei (`*.part`) im Zielverzeichnis, dann
Verschieben auf den Zielpfad erst nach erfolgreichem Schreiben. Bei einem Fehler (Datenquelle,
Validierung, I/O) bleibt der Zielpfad unangetastet - eine bereits vorhandene Datei behaelt ihren
alten Inhalt, es entsteht keine unvollstaendige `.xlsx`; die Temp-Datei wird entfernt.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `out` | `Path` | **nein** - `Objects.requireNonNull(out, "out")` |

Rueckgabewert: `void`. Geworfene Exceptions:
- `NullPointerException`, wenn `out == null`.
- `IllegalArgumentException("Output path has no parent directory (must be a file inside a
  directory, not a filesystem root): " + out)`, wenn `out.toAbsolutePath().getParent() ==
  null` (verifiziert: expliziter Check vor dem Erzeugen der Temp-Datei).
- `IOException`, wenn das Erzeugen der Temp-Datei, das Schreiben oder das finale Verschieben
  fehlschlaegt. Bei einem `IOException`/`RuntimeException`/`Error` waehrend des Schreibens wird
  die Temp-Datei best-effort geloescht (`deleteQuietly`), bevor die urspruengliche Exception
  erneut geworfen wird (verifiziert: `catch (IOException | RuntimeException | Error e)`-Block
  mit `throw e;`).
- Alle Exceptions aus dem zugrunde liegenden `write(OutputStream)` (siehe unten) propagieren
  unveraendert (z. B. `IllegalStateException`, `RowLimitExceededException`).

Das atomare Verschieben (privates `moveInPlace`) versucht zuerst
`StandardCopyOption.ATOMIC_MOVE`; unterstuetzt das Dateisystem das nicht
(`AtomicMoveNotSupportedException`), faellt es auf einen nicht-atomaren `Files.move(...,
REPLACE_EXISTING)` zurueck - dieser Fallback ist **nicht** Teil der urspruenglichen
Atomaritaetsgarantie und wird in der oeffentlichen Javadoc-Doku nicht gesondert erwaehnt, ist aber
im Code klar ersichtlich (kein Widerspruch, nur eine Praezisierung: "atomar, wo das Dateisystem
es unterstuetzt").

## `void write(OutputStream out) throws IOException`


Schreibt das Workbook nach `out` (der Aufrufer besitzt und schliesst den Stream). Jedes Sheet wird
gestreamt nacheinander gerendert. Bei einem Fehler werden die Datenquellen **aller** uebergebenen
Sheets geschlossen - auch die von Sheets, die nie erreicht wurden - damit kein
[DataProvider](/api-reference/data-provider/data-provider.md) (z. B. ein offenes JDBC-`ResultSet` oder ein
`Stream`) leckt.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `out` | `OutputStream` | **nein** - `Objects.requireNonNull(out, "out")` |

Rueckgabewert: `void`. Geworfene Exceptions:
- `NullPointerException`, wenn `out == null`.
- `IllegalStateException("At least one sheet is required")`, wenn keine Sheets hinzugefuegt
  wurden (`sheets.isEmpty()`).
- `IllegalStateException("WorkbookBuilder is single-use: already written - create a new
  instance per job")`, wenn diese Instanz bereits einmal geschrieben wurde (Flag `written`);
  **verifiziert:** diese Pruefung erfolgt **vor** dem Setzen von `written = true`, sodass ein
  fehlgeschlagener erster Schreibversuch (Exception aus einem Sheet) die Instanz trotzdem
  dauerhaft als "verbraucht" markiert - ein erneuter `write`-Versuch nach einem Fehler schlaegt
  ebenfalls mit dieser `IllegalStateException` fehl, nicht mit einer Wiederholung des
  urspruenglichen Fehlers. Das ist im Javadoc nicht explizit erwaehnt, aber aus der Reihenfolge
  `written = true;` **vor** dem `try`-Block klar ersichtlich.
- Jede `IOException`/`RuntimeException`/`Error`, die beim Rendern eines Sheets auftritt,
  propagiert nach dem Aufraeumen (Schliessen aller nicht konsumierten Datenquellen) unveraendert
  weiter (verifiziert: `catch (IOException | RuntimeException | Error e) { ...; throw e; }`).

# Citations

[1] [WorkbookBuilder (Übersicht)](./workbook-builder.md)
