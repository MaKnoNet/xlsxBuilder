---
type: API Reference
title: WorkbookBuilder
description: Oeffentlicher, nicht thread-sicherer Single-Use-Builder, der beliebig viele XlsxBuilder-Sheets zu einer .xlsx-Datei kombiniert und den Datei-/Workbook-Lifecycle inkl. atomarem Schreiben besitzt.
resource: src/main/java/de/makno/xlsxbuilder/WorkbookBuilder.java
tags: [api-reference, builder, excel, workbook, atomic-write]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Ueberblick

`public final class WorkbookBuilder` - kombiniert einen oder mehrere Arbeitsblaetter zu einer
`.xlsx`-Datei. Jedes Sheet wird durch einen eigenen
[XlsxBuilder](/api-reference/xlsx-builder.md) beschrieben (mit eigenem Datentyp, Spalten,
Sortierung, Summenzeile etc.). Narrative Gesamtbeschreibung, Beispielcode und Atomic-Write-Kontext
bereits in [WorkbookBuilder (Komponente)](/components/workbook-builder.md) - diese Datei
fokussiert auf die vollstaendige, verifizierte Methoden-Ebene.

**Thread-Safety / Single-Use** (verifiziert): nicht thread-sicher, fuer einmalige Nutzung
gedacht - eine eigene Instanz pro Job, niemals gleichzeitig von mehreren Threads verwenden und
niemals dieselbe Datei nebenlaeufig beschreiben. Ein zweiter `write(...)`-Aufruf auf derselben
Instanz wirft `IllegalStateException` (Flag `written`, siehe unten). Siehe
[Concurrency contract](/architecture/concurrency-contract.md).

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `public final class WorkbookBuilder` — keine
`extends`-/`implements`-Klausel; erweitert implizit nur `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, daher nicht
erweiterbar. Referenziert [XlsxBuilder](/api-reference/xlsx-builder.md) als Feldtyp (`List<
XlsxBuilder<?>>`) — Komposition/Aggregation, keine Vererbungsbeziehung.

# Konstruktoren

## `private WorkbookBuilder()`

Leerer privater Konstruktor - Instanzen werden ausschliesslich ueber die statische Fabrikmethode
`create()` erzeugt. Keine Parameter, keine Exceptions.

# Methoden

## `static WorkbookBuilder create()`

Keine Parameter. Rueckgabewert: neue `WorkbookBuilder`-Instanz, nie `null`. Keine Exceptions.

## `WorkbookBuilder sxssfRowWindow(int window)`

Setzt die Anzahl der Zeilen, die SXSSF pro Sheet gleichzeitig im Speicher haelt (Rest wird auf
Temp-Dateien gespillt). Default: `100` (`DEFAULT_ROW_WINDOW`).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `window` | `int` | primitiv; muss `>= 1` sein |

Rueckgabewert: `this` (fluentes API), nie `null`. Geworfene Exceptions:
`IllegalArgumentException("sxssfRowWindow must be >= 1")`, wenn `window < 1` (verifiziert exakt
gegen den Code).

## `WorkbookBuilder tempDir(Path dir)`

Setzt ein Standard-Basisverzeichnis fuer die temporaeren Sort-Run-Dateien (External Merge Sort)
aller Sheets dieses Workbooks. Ein Per-Sheet-`XlsxBuilder.sortTempDir(Path)` hat weiterhin
Vorrang.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `dir` | `Path` | **ja** - `null` (Default) bedeutet: Per-Sheet-Einstellung bzw. System-Temp (`java.io.tmpdir`) greift; nicht geprueft, direkt uebernommen |

Rueckgabewert: `this`, nie `null`. Keine Exceptions.

## `WorkbookBuilder sheet(XlsxBuilder<?> sheet)`

Fuegt ein Sheet hinzu. Der uebergebene `XlsxBuilder` muss eine Datenquelle besitzen
(`.data(...)`) - diese Prufung erfolgt allerdings **nicht** hier, sondern erst spaeter beim
Rendern in `XlsxBuilder.renderInto(...)`.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `sheet` | `XlsxBuilder<?>` | **nein** - `Objects.requireNonNull(sheet, "sheet")` |

Rueckgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`sheet == null`.

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
[DataProvider](/api-reference/data-provider.md) (z. B. ein offenes JDBC-`ResultSet` oder ein
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

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/WorkbookBuilder.java`
[2] [WorkbookBuilder (Komponente)](/components/workbook-builder.md)
[3] [Concurrency contract](/architecture/concurrency-contract.md)
