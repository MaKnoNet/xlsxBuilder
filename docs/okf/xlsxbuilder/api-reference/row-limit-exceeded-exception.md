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
[XlsxBuilder](/api-reference/xlsx-builder.md)`.splitOnRowLimit(true)` wird stattdessen auf
Folge-Sheets fortgesetzt. Besitzt eine explizite `serialVersionUID = 1L`. Siehe
[Fehlerbehandlung](/architecture/error-handling.md).

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `public class RowLimitExceededException extends
IllegalStateException` (JDK-Klasse `java.lang.IllegalStateException`, selbst eine Unterklasse von
`RuntimeException`) — implementiert keine zusätzlichen Interfaces selbst.

**Rückwärts:** Keine Unterklassen innerhalb dieses Projekts (verifiziert: `grep -rn "extends
RowLimitExceededException"` über den gesamten Quellbaum liefert keinen Treffer). Wird von
[XlsxWriter](/api-reference/xlsx-writer.md) geworfen, wenn ein Sheet das Excel-Zeilenlimit
überschreitet und kein Split konfiguriert ist — das ist Verwendung (Werfen einer Exception-Instanz),
keine Vererbungsbeziehung.

# Konstruktoren

## `RowLimitExceededException(String message)`

Paketintern — laut Javadoc-Kommentar "nur von der Bibliothek selbst werfbar" (kein öffentlicher
Konstruktor vorhanden, daher von außerhalb des Pakets `de.makno.xlsxbuilder` nicht
instanziierbar).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `message` | `String` | ja — ungeprüft an `IllegalStateException(String)` durchgereicht, kein `requireNonNull` |

Verhalten bei ungültiger Eingabe: keins — kein Validierungscode im Konstruktor. `null` als
`message` ist zulässig (führt zu `getMessage() == null`).

# Methoden

Keine eigenen Methoden — erbt das vollständige Verhalten von `IllegalStateException`/
`RuntimeException`/`Throwable` unverändert.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/RowLimitExceededException.java`
[2] [Fehlerbehandlung](/architecture/error-handling.md)
