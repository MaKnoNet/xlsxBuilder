---
type: API Reference
title: SheetWriteOptions
description: Paketinterner, unveränderlicher Record — bündelt alle Layout-Parameter für das Schreiben eines Sheets, mit defensiven Kopien im kompakten Konstruktor.
resource: src/main/java/de/makno/xlsxbuilder/SheetWriteOptions.java
tags: [api-reference, record, value-object, immutability, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`record SheetWriteOptions(...)` — paketintern. Bündelt die Layout-Parameter für das Schreiben
eines Sheets (`.xlsx`) und hält die Writer-Signaturen schlank. Näher beschrieben in
[Konfigurationsobjekte](/components/configuration-models.md).

# Felder

Felder = Record-Komponenten (`headerLines`, `footerLines`, `columnGroups`, `placeholders`,
`placeholderResolver`, `showColumnHeaders`, `defaultNullText`, `splitOnRowLimit`,
`splitSheetNamer`, `maxRowsPerSheet`), siehe [Konstruktor](./constructor.md) für die defensiven
Kopien im kompakten Konstruktor.

# Thread-Safety

Immutable Value-Type mit defensiven, unveränderlichen Kopien der Collection-Komponenten
(`List.copyOf(...)`/`Map.copyOf(...)` im kompakten Konstruktor, siehe Konstruktor-Dokument) —
ein Aufrufer, der die ursprünglich übergebenen Collections weiterhin referenziert, kann die
Optionen nicht nachträglich mutieren, und die Accessor-Methoden geben unveränderliche Views
zurück. Beliebig zwischen Threads teilbar.

# Serialisierung

Nicht `Serializable` — `SheetWriteOptions` implementiert kein Serialisierungs-Interface
(verifiziert: `record SheetWriteOptions(...) {...}`, keine `implements`-Klausel).

# equals/hashCode/toString

Automatisch generiert (Record): komponentenbasiert über alle zehn Komponenten, keine eigenen
Overrides im Quellcode. Da `placeholderResolver`/`splitSheetNamer` beliebige Lambdas sein
können, ist die generierte Gleichheit für diese beiden Komponenten faktisch identitätsbasiert.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `record SheetWriteOptions(...)` — erweitert implizit
`java.lang.Record`; keine `implements`-Klausel im Quelltext.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; Records sind implizit `final`.
Referenziert selbst [ColumnGroup](/api-reference/column-group/column-group.md) und
[SplitSheetNamer](/api-reference/split-sheet-namer/split-sheet-namer.md) als Komponenten-Typen — das ist
Komposition (Feldtyp), keine Vererbung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden


# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/SheetWriteOptions.java`
[2] [Konfigurationsobjekte](/components/configuration-models.md)
