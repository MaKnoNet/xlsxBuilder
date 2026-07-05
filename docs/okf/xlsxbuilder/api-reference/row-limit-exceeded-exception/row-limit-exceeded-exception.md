---
type: API Reference
title: RowLimitExceededException
description: Wird geworfen, wenn ein Sheet die maximale Zeilenzahl pro Worksheet überschreitet und kein Split konfiguriert ist.
resource: src/main/java/de/makno/xlsxbuilder/RowLimitExceededException.java
tags: [api-reference, exception, row-limit]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`public class RowLimitExceededException extends IllegalStateException` — wird geworfen, wenn die
Daten eines Sheets die maximale Zeilenzahl pro Worksheet überschreiten (Excel: 1.048.576,
inklusive Titel-/Gruppen-/Kopfzeilen sowie der für Summenzeile/Fußzeilen reservierten Zeilen)
und das Sheet nicht auf Split konfiguriert ist. Mit
[XlsxBuilder](/api-reference/xlsx-builder/split-on-row-limit.md)`.splitOnRowLimit(true)` wird stattdessen auf
Folge-Sheets fortgesetzt. Besitzt eine explizite `serialVersionUID = 1L`. Siehe
[Fehlerbehandlung](/architecture/error-handling.md).

# Felder

Keine eigenen Felder außer der `serialVersionUID`; ansonsten nur die von `IllegalStateException`/
`RuntimeException`/`Throwable` geerbten (Message, Cause, Stacktrace).

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `serialVersionUID` | `private static final long` | Wert `1L`, explizit deklariert. | entfällt (primitiv) |

# Thread-Safety

Wie jede Exception-Instanz nach Konstruktion effektiv unveränderlich (Message wird im
Konstruktor gesetzt). Instanzen werden intern von `XlsxWriter` geworfen, nicht von Aufrufern
konstruiert (der einzige Konstruktor ist paketintern, siehe unten) — Nebenläufigkeitsfragen
stellen sich praktisch nicht.

# Serialisierung

`Serializable` (transitiv über `Throwable`) mit expliziter `serialVersionUID = 1L` —
verifiziert gegen den Quellcode.

# equals/hashCode/toString

Keine eigenen Overrides im Quellcode; es gelten die von `Throwable`/`Object` geerbten
Implementierungen: identitätsbasierte `equals`/`hashCode`, `toString()` im
`Throwable`-Standardformat (Klassenname + Message).

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `public class RowLimitExceededException extends
IllegalStateException` (JDK-Klasse `java.lang.IllegalStateException`, selbst eine Unterklasse von
`RuntimeException`) — implementiert keine zusätzlichen Interfaces selbst.

**Rückwärts:** Keine Unterklassen innerhalb dieses Projekts (verifiziert: `grep -rn "extends
RowLimitExceededException"` über den gesamten Quellbaum liefert keinen Treffer). Wird von
[XlsxWriter.writeSheets](/api-reference/xlsx-writer/write-sheets.md) geworfen, wenn ein Sheet das Excel-Zeilenlimit
überschreitet und kein Split konfiguriert ist — das ist Verwendung (Werfen einer Exception-Instanz),
keine Vererbungsbeziehung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden


# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/RowLimitExceededException.java`
[2] [Fehlerbehandlung](/architecture/error-handling.md)
