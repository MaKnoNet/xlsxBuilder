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
([RowComparator](/api-reference/row-comparator/row-comparator.md)) und Breitenschätzung mit dem
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

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `NULL`, `STRING`, `INT`, `LONG`, `DOUBLE`, `BOOL`, `BIGDEC`, `LDATE`, `LDATETIME`, `LTIME`, `FLOAT` | `private static final byte` | Typ-Tags `0`–`10` für die kompakte Kodierung der jeweiligen Java-Typen. | entfällt (primitiv) |
| `JAVA` | `private static final byte` | Typ-Tag `99` — Fallback-Kodierung über Java-Serialisierung für unbekannte, aber `Serializable` Typen. | entfällt (primitiv) |
| `DESERIALIZATION_LIMITS` | `private static final ObjectInputFilter` | Ressourcenlimits für den Java-Fallback (`maxbytes=16777216;maxdepth=64;maxrefs=100000;maxarray=1000000`), siehe Überblick. | nein — statisch initialisiert |
| `MAX_BYTE_ARRAY_LENGTH` | `private static final int` | Konstante `16 * 1024 * 1024` (16 MiB) — Obergrenze für ein einzelnes längenpräfigiertes Byte-Array vor der Allokation. | entfällt (primitiv) |

# Thread-Safety

Zustandslos — alle Felder sind `static final` (Konstanten/Filter), alle Methoden `static`; die
Klasse selbst hält keinen veränderlichen Zustand. `writeRow`/`readRow` sind daher parallel aus
mehreren Threads aufrufbar, solange jeder Aufruf einen eigenen `DataOutputStream`/
`DataInputStream` verwendet (die Streams selbst sind nicht thread-sicher, aber das ist
Aufrufer-Verantwortung, nicht der Klasse).

# Serialisierung

Nicht `Serializable` — `RowCodec` implementiert kein Serialisierungs-Interface (verifiziert:
`final class RowCodec`, keine `implements`-Klausel). Ironischerweise implementiert die Klasse
selbst ein **eigenes** binäres (De-)Serialisierungsformat für `Row`, ist aber selbst kein
`Serializable`-Objekt.

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben — es gilt die Identitätssemantik von
`java.lang.Object`. Da die Klasse nicht instanziierbar ist (privater Konstruktor), ist das ohne
praktische Relevanz.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `final class RowCodec` — keine
`extends`-/`implements`-Klausel; erweitert implizit nur `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, nicht instanziierbar
(privater Konstruktor).

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``static void writeRow(DataOutputStream out, Row row) throws IOException``](./write-row.md)
- [``static Row readRow(DataInputStream in) throws IOException``](./read-row.md)
- [``private static void writeValue(DataOutputStream out, Object v)` (intern, über `writeRow` erreichbar)`](./write-value.md)
- [``private static Object readValue(DataInputStream in)` (intern)`](./read-value.md)
- [``private static void writeJavaSerialized(DataOutputStream out, Object v)` (intern)`](./write-java-serialized.md)
- [``private static Object readJavaSerialized(DataInputStream in)` (intern)`](./read-java-serialized.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/RowCodec.java`
[2] [Out-of-core pipeline](/architecture/out-of-core-pipeline.md)
