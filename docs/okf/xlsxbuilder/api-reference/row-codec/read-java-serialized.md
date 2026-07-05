---
type: API Reference
title: RowCodec.readJavaSerialized(...)
description: Methode readJavaSerialized von RowCodec - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/RowCodec.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static Object readJavaSerialized(DataInputStream in)` (intern)


Liest den Java-serialisierten Fallback-Block, wendet dabei den oben beschriebenen
`ObjectInputFilter` (`DESERIALIZATION_LIMITS`) an. Wirft `IOException("Deserialization
failed", e)`, wenn `ClassNotFoundException` beim Deserialisieren auftritt.

Diese fünf privaten Hilfsmethoden sind nicht von außerhalb der Klasse aufrufbar; sie werden hier
dokumentiert, weil sie das beobachtbare Fehlerverhalten der beiden öffentlich (paketintern)
erreichbaren Einstiegspunkte `writeRow`/`readRow` vollständig erklären.

# Citations

[1] [RowCodec (Übersicht)](./row-codec.md)
