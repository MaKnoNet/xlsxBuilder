---
type: API Reference
title: RowCodec.readValue(...)
description: Methode readValue von RowCodec - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/RowCodec.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static Object readValue(DataInputStream in)` (intern)


Liest einen Tag und dispatcht auf die passende Lesevariante. Wirft `IOException("Unknown
RowCodec type tag: " + tag)`, falls das Tag-Byte keinem bekannten Fall entspricht (Default-Zweig
des `switch`) — eine defensive Prüfung gegen korrupte/fremde Run-Dateien.

# Citations

[1] [RowCodec (Übersicht)](./row-codec.md)
