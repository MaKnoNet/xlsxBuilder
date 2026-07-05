---
type: API Reference
title: ColumnType
description: Enum für den logischen Typ einer Spalte — steuert Zelltyp/-format beim Schreiben und die Sortierbarkeit.
resource: src/main/java/de/makno/xlsxbuilder/ColumnType.java
tags: [api-reference, enum, configuration]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`ColumnType` ist ein öffentliches Enum mit den Konstanten `STRING, INTEGER, LONG, DOUBLE,
DECIMAL, BOOLEAN, DATE, DATETIME, TIME, FORMULA`. Steuert, wie ein projizierter Wert als
Excel-Zelle geschrieben wird (Zelltyp/-format, siehe
[XlsxWriter.writeCell](/api-reference/xlsx-writer/write-cell.md)) und wie beim Sortieren verglichen
wird. Enum-Konstanten sind implizit thread-safe (unveränderliche Singletons).

**Sicherheitshinweis zu `FORMULA`** (aus dem Javadoc, gegen den Code verifiziert — korrekt):
Der Formeltext wird wörtlich als Excel-Formel geschrieben (siehe
`XlsxWriter.writeCell`, Fall `FORMULA`: `cell.setCellFormula(...)`, kein Escaping) — niemals
aus nicht vertrauenswürdiger Eingabe zusammensetzen (Formel-Injection). Für Text, der nur wie
eine Formel aussieht, `STRING` verwenden.

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `sortable` | `final boolean` | Ob dieser Typ sortierbar ist; `true` für alle Konstanten außer `FORMULA` (`false`). Gesetzt im privaten Enum-Konstruktor `ColumnType(boolean sortable)`. | entfällt (primitiv) |

# Thread-Safety

Immutable — Enum-Konstanten sind JVM-garantierte Singletons mit ausschließlich `final`-Feldern;
beliebig zwischen Threads teilbar, keine Synchronisation nötig.

# Serialisierung

Implizit `Serializable` — jedes Java-`enum` erbt transitiv `java.io.Serializable` über
`java.lang.Enum`, ohne dass die Klasse dies explizit deklariert (verifiziert: kein eigenes
`implements Serializable` im Quelltext, aber im Deklarations-Sinn geerbt). Enum-Serialisierung
folgt dem JDK-Spezialfall (nur der Konstantenname wird geschrieben, keine
`serialVersionUID`-Pflicht, keine Feldserialisierung) — die zusätzlichen Instanzfelder
(`sortable`) werden bei der Deserialisierung nicht aus dem Stream gelesen, sondern über die
bereits existierende Singleton-Konstante aufgelöst.

# equals/hashCode/toString

Keine eigenen Overrides im Quellcode; es gelten die von `java.lang.Enum` geerbten
Implementierungen: `equals`/`hashCode` sind identitätsbasiert (äquivalent zu `==`, da pro
Konstante nur eine Instanz existiert), `toString()` liefert per Default den Konstantennamen
(z. B. `"FORMULA"`).

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `public enum ColumnType` — Enums erweitern implizit
`java.lang.Enum<ColumnType>`; keine explizit implementierten Interfaces im Quelltext (über
`Enum` werden `Comparable<ColumnType>` und `Serializable` transitiv mitgebracht, aber nicht selbst
deklariert).

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts — Enum-Typen sind implizit
`final` und nicht erweiterbar. Wird u. a. in `Column`, `XlsxWriter` als Feld-/Parametertyp
verwendet — keine Vererbungsbeziehung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``boolean isSortable()``](./is-sortable.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/ColumnType.java`
