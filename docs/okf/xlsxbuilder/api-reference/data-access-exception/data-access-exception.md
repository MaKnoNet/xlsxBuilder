---
type: API Reference
title: DataAccessException
description: Ungechecktes Wrapping einer SQLException, die in DataProvider-Methoden ohne geprüfte Exception-Signatur auftritt.
resource: src/main/java/de/makno/xlsxbuilder/DataAccessException.java
tags: [api-reference, exception, jdbc]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`DataAccessException extends RuntimeException` — ungechecktes Wrapping für eine
`SQLException`, die innerhalb der [DataProvider](/api-reference/data-provider/data-provider.md)-Methoden
(`hasNext()`/`next()`) auftritt, deren Signaturen keine geprüfte Exception erlauben. Besitzt
eine explizite `serialVersionUID = 1L`. Siehe
[Fehlerbehandlung](/architecture/error-handling.md) für den Gesamtkontext.

# Felder

Keine eigenen Felder — nur die von `RuntimeException`/`Throwable` geerbten (Message, Cause,
Stacktrace, unterdrückte Exceptions).

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `serialVersionUID` | `private static final long` | Wert `1L`, explizit deklariert für stabile Serialisierungskompatibilität. | entfällt (primitiv) |

# Thread-Safety

Wie jede Exception-Instanz nach Konstruktion effektiv unveränderlich (Message/Cause werden im
Konstruktor gesetzt und danach nicht mehr verändert); der geerbte, veränderliche Stacktrace-
Zustand aus `Throwable` unterliegt dessen üblichen Thread-Safety-Grenzen (in der Praxis
unkritisch, da Exceptions typischerweise nicht zwischen Threads geteilt und mutiert werden).

# Serialisierung

`Serializable` (transitiv über `Throwable`) mit expliziter `serialVersionUID = 1L` — verifiziert
gegen den Quellcode. Zukünftige abwärtskompatible Änderungen (z. B. neue Felder) sollten diesen
Wert beibehalten, um bestehende serialisierte Instanzen deserialisierbar zu halten.

# equals/hashCode/toString

Keine eigenen Overrides im Quellcode; es gelten die von `Throwable`/`Object` geerbten
Implementierungen: `equals`/`hashCode` sind identitätsbasiert, `toString()` liefert
Klassenname + Message (`Throwable.toString()`-Format).

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `public class DataAccessException extends RuntimeException`
(JDK-Klasse, kein Projekt-internes Pendant) — implementiert keine zusätzlichen Interfaces selbst
(erbt transitiv `Serializable` von `Throwable`).

**Rückwärts:** Keine Unterklassen innerhalb dieses Projekts (verifiziert: `grep -rn "extends
DataAccessException"` über den gesamten Quellbaum liefert keinen Treffer). Auch keine Klasse
implementiert sie als Interface (sie ist keins). Wird in
[DataProviders.ofResultSet(...)](/api-reference/data-providers/of-result-set.md) geworfen, aber das ist
Verwendung, keine Vererbung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden


# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/DataAccessException.java`
[2] [Fehlerbehandlung](/architecture/error-handling.md)
