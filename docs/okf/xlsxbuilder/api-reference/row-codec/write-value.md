---
type: API Reference
title: RowCodec.writeValue(...)
description: Methode writeValue von RowCodec - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/RowCodec.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static void writeValue(DataOutputStream out, Object v)` (intern, über `writeRow` erreichbar)


Schreibt einen einzelnen Zellwert mit Typ-Tag. `v == null` wird explizit als `NULL`-Tag
behandelt (kein Fehler — repräsentiert eine leere Zelle). Unbekannte, aber `Serializable`-Typen
fallen auf `JAVA` zurück; ein nicht-serialisierbarer Typ löst über
`writeJavaSerialized` eine `IOException` mit erklärender Meldung aus (siehe unten). Nicht
Teil der aufrufbaren API dieser Klasse (privat), aber relevant für das Verständnis von
`writeRow`.

# Citations

[1] [RowCodec (Übersicht)](./row-codec.md)
