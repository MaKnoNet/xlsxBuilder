---
type: API Reference
title: RowCodec
description: Paketinterne, kompakte typmarkierte (De-)Serialisierung einer Row für die Run-Dateien der ExternalMergeSort, mit gehärteten Deserialisierungs-Limits im Java-Fallback.
resource: src/main/java/de/makno/xlsxbuilder/RowCodec.java
tags: [api-reference, serialization, security, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`final class RowCodec` — paketintern, nicht instanziierbar. Schreibt jeden Zellwert mit einem
1-Byte-Typ-Tag plus kompakter Primitiv-Kodierung statt Java-`ObjectOutputStream` (das pro Objekt
Klassendeskriptoren schreibt und mit `reset()` teuer ist). Unbekannte, aber `Serializable`
Typen fallen auf Java-Serialisierung zurück (Tag `JAVA = 99`). Der konkrete Laufzeittyp bleibt
erhalten (z. B. `Integer` vs. `Long`), damit Vergleich
([RowComparator](/api-reference/row-comparator.md)) und Breitenschätzung mit dem
In-Memory-Fall identisch bleiben. Näher beschrieben in
[Out-of-core pipeline](/architecture/out-of-core-pipeline.md).

**Sicherheit (verifiziert im Code):** Die Run-Dateien werden vom selben Prozess geschrieben und
gelesen (eigenes Sort-Temp-Verzeichnis) — keine primäre Vertrauensgrenze. Dennoch begrenzt ein
`ObjectInputFilter` (`DESERIALIZATION_LIMITS`, `maxbytes=16777216;maxdepth=64;maxrefs=100000;
maxarray=1000000`) Tiefe, Referenzen, Array-Größe und Gesamtbytes des Java-Fallbacks, um
Deserialisierungs-„Bomben" abzufedern, falls eine Temp-Datei je manipuliert würde. Bewusst
**kein** Klassen-Allowlisting, da der Fallback beliebige nutzerdefinierte `Serializable`-Typen
tragen soll. Zusätzlich begrenzt `MAX_BYTE_ARRAY_LENGTH` (16 MiB) jedes längenpräfigierte
Byte-Array (String/BigDecimal-Betrag/Java-Blob) **vor** der Allokation — eine korrupte oder
manipulierte Länge (negativ oder `Integer.MAX_VALUE`) wird dadurch zu einer sauberen
`IOException` statt zu `NegativeArraySizeException`/`OutOfMemoryError`.

# Vererbungshierarchie

**Vorwärts (eigene Deklaration):** `final class RowCodec` — keine
`extends`-/`implements`-Klausel; erweitert implizit nur `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, nicht instanziierbar
(privater Konstruktor).

# Konstruktoren

## `private RowCodec()`

Leerer privater Konstruktor, verhindert Instanziierung. Keine Parameter, keine Exceptions.

# Methoden

## `static void writeRow(DataOutputStream out, Row row) throws IOException`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `out` | `DataOutputStream` | nicht geprüft — `null` führt zu `NullPointerException` beim ersten `out.writeInt(...)` |
| `row` | `Row` | nicht geprüft — `null` führt zu `NullPointerException` bei `row.size()` |

Rückgabewert: `void`. Geworfene Exceptions: `IOException`, wenn der zugrunde liegende Stream
beim Schreiben fehlschlägt (z. B. Datenträger voll); `NullPointerException` bei `out == null`
oder `row == null` (nicht in der Javadoc erwähnt, aber durch fehlenden Null-Check verifiziert).

## `static Row readRow(DataInputStream in) throws IOException`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `in` | `DataInputStream` | nicht geprüft — `null` führt zu `NullPointerException` bei `in.readInt()` |

Rückgabewert: die gelesene `Row`, nie `null` bei erfolgreichem Rückgabepfad (immer frisch
konstruiert). Geworfene Exceptions: `IOException` bei einem Lese-/Formatfehler (u. a. über
`readValue`/`readBytes`, siehe unten); `EOFException` (Unterklasse von `IOException`), wenn der
Stream vorzeitig endet (`DataInputStream.readInt()`/`readFully(...)`); `NullPointerException`
bei `in == null`.

## `private static void writeValue(DataOutputStream out, Object v)` (intern, über `writeRow` erreichbar)

Schreibt einen einzelnen Zellwert mit Typ-Tag. `v == null` wird explizit als `NULL`-Tag
behandelt (kein Fehler — repräsentiert eine leere Zelle). Unbekannte, aber `Serializable`-Typen
fallen auf `JAVA` zurück; ein nicht-serialisierbarer Typ löst über
`writeJavaSerialized` eine `IOException` mit erklärender Meldung aus (siehe unten). Nicht
Teil der aufrufbaren API dieser Klasse (privat), aber relevant für das Verständnis von
`writeRow`.

## `private static Object readValue(DataInputStream in)` (intern)

Liest einen Tag und dispatcht auf die passende Lesevariante. Wirft `IOException("Unknown
RowCodec type tag: " + tag)`, falls das Tag-Byte keinem bekannten Fall entspricht (Default-Zweig
des `switch`) — eine defensive Prüfung gegen korrupte/fremde Run-Dateien.

## `private static void writeJavaSerialized(DataOutputStream out, Object v)` (intern)

Fallback-Pfad für nicht direkt unterstützte, aber `Serializable` Typen. Wirft `IOException`
mit der Meldung "Cell value of type ... is not Serializable - with sortBy(...) all cell values
must be Serializable, because sorted runs are spilled to temp files", wenn `v` **nicht**
`Serializable` ist (`NotSerializableException` wird gefangen und in diese sprechendere
`IOException` übersetzt) — verifiziert exakt gegen den Code.

## `private static Object readJavaSerialized(DataInputStream in)` (intern)

Liest den Java-serialisierten Fallback-Block, wendet dabei den oben beschriebenen
`ObjectInputFilter` (`DESERIALIZATION_LIMITS`) an. Wirft `IOException("Deserialization
failed", e)`, wenn `ClassNotFoundException` beim Deserialisieren auftritt.

Diese fünf privaten Hilfsmethoden sind nicht von außerhalb der Klasse aufrufbar; sie werden hier
dokumentiert, weil sie das beobachtbare Fehlerverhalten der beiden öffentlich (paketintern)
erreichbaren Einstiegspunkte `writeRow`/`readRow` vollständig erklären.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/RowCodec.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
