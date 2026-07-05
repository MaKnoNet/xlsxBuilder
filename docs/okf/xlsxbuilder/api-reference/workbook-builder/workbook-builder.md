---
type: API Reference
title: WorkbookBuilder
description: Oeffentlicher, nicht thread-sicherer Single-Use-Builder, der beliebig viele XlsxBuilder-Sheets zu einer .xlsx-Datei kombiniert und den Datei-/Workbook-Lifecycle inkl. atomarem Schreiben besitzt.
resource: src/main/java/de/makno/xlsxbuilder/WorkbookBuilder.java
tags: [api-reference, builder, excel, workbook, atomic-write]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`public final class WorkbookBuilder` - kombiniert einen oder mehrere Arbeitsblätter zu einer
`.xlsx`-Datei. Jedes Sheet wird durch einen eigenen
[XlsxBuilder](/api-reference/xlsx-builder/xlsx-builder.md) beschrieben (mit eigenem Datentyp, Spalten,
Sortierung, Summenzeile etc.). Narrative Gesamtbeschreibung, Beispielcode und Atomic-Write-Kontext
bereits in [WorkbookBuilder (Komponente)](/components/workbook-builder.md) - diese Datei
fokussiert auf die vollständige, verifizierte Methoden-Ebene.

**Thread-Safety / Single-Use** (verifiziert): nicht thread-sicher, für einmalige Nutzung
gedacht - eine eigene Instanz pro Job, niemals gleichzeitig von mehreren Threads verwenden und
niemals dieselbe Datei nebenläufig beschreiben. Ein zweiter `write(...)`-Aufruf auf derselben
Instanz wirft `IllegalStateException` (Flag `written`, siehe unten). Siehe
[Concurrency contract](/architecture/concurrency-contract.md).

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `LOG` | `private static final Logger` | Log4j2-Logger dieser Klasse. | nein |
| `DEFAULT_ROW_WINDOW` | `private static final int` | Konstante `100` — Default-Anzahl der von SXSSF im Speicher gehaltenen Zeilen pro Sheet. | entfällt (primitiv) |
| `sheets` | `private final List<XlsxBuilder<?>>` | Alle hinzugefügten Sheet-Builder, in Deklarationsreihenfolge. | Feld nie `null` (mit `new ArrayList<>()` initialisiert); Elemente nie `null` (`sheet(...)` erzwingt `requireNonNull`) |
| `sxssfRowWindow` | `private int` | Konfigurierte SXSSF-Fenstergröße. Default `DEFAULT_ROW_WINDOW` (100). | entfällt (primitiv); Setter erzwingt `>= 1` |
| `tempDir` | `private Path` | Default-Basisverzeichnis für die Sortier-Temp-Dateien aller Sheets. | ja — Default `null` bedeutet je-Sheet-Einstellung bzw. System-Temp |
| `written` | `private boolean` | Single-Use-Flag: `true`, sobald `write(...)` zu schreiben begonnen hat. Default `false`. | entfällt (primitiv) |

# Thread-Safety

**Nicht thread-sicher, Single-Use** (verifiziert, siehe Überblick): für einmalige Nutzung
gedacht — neue Instanz pro Job, nicht zwischen Threads teilen, nicht dieselbe Zieldatei
nebenläufig beschreiben. Ein zweiter `write(...)`-Aufruf wirft `IllegalStateException` (Flag
`written`). Da die Bibliothek keinen geteilten/statischen veränderlichen Zustand besitzt,
laufen mehrere gleichzeitige Jobs mit jeweils eigenen Instanzen isoliert voneinander; jeder
`write(...)`-Aufruf erzeugt sein eigenes POI-`SXSSFWorkbook`.

# Serialisierung

Nicht `Serializable` — `WorkbookBuilder` implementiert kein Serialisierungs-Interface
(verifiziert gegen die Klassendeklaration `public final class WorkbookBuilder`).

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben (verifiziert: keine `equals`/`hashCode`/`toString`-
Deklaration im Quellcode) — es gilt die Identitätssemantik von `java.lang.Object`. Da die
Klasse Single-Use und nicht für Sammlungen mit Werte-Gleichheit gedacht ist, hat das in der
Praxis geringe Relevanz, wird hier aber explizit als verifizierter Befund festgehalten
(analog zu [XlsxBuilder](/api-reference/xlsx-builder/xlsx-builder.md)).

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `public final class WorkbookBuilder` — keine
`extends`-/`implements`-Klausel; erweitert implizit nur `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, daher nicht
erweiterbar. Referenziert [XlsxBuilder](/api-reference/xlsx-builder/xlsx-builder.md) als Feldtyp (`List<
XlsxBuilder<?>>`) — Komposition/Aggregation, keine Vererbungsbeziehung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``static WorkbookBuilder create()``](./create.md)
- [``WorkbookBuilder sxssfRowWindow(int window)``](./sxssf-row-window.md)
- [``WorkbookBuilder tempDir(Path dir)``](./temp-dir.md)
- [``WorkbookBuilder sheet(XlsxBuilder<?> sheet)``](./sheet.md)
- [``void write(Path out) throws IOException``](./write.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/WorkbookBuilder.java`
[2] [WorkbookBuilder (Komponente)](/components/workbook-builder.md)
[3] [Concurrency contract](/architecture/concurrency-contract.md)
