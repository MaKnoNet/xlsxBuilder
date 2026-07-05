---
type: API Reference
title: RowCodec.writeJavaSerialized(...)
description: Methode writeJavaSerialized von RowCodec - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/RowCodec.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static void writeJavaSerialized(DataOutputStream out, Object v)` (intern)


Fallback-Pfad für nicht direkt unterstützte, aber `Serializable` Typen. Wirft `IOException`
mit der Meldung "Cell value of type ... is not Serializable - with sortBy(...) all cell values
must be Serializable, because sorted runs are spilled to temp files", wenn `v` **nicht**
`Serializable` ist (`NotSerializableException` wird gefangen und in diese sprechendere
`IOException` übersetzt) — verifiziert exakt gegen den Code.

# Citations

[1] [RowCodec (Übersicht)](./row-codec.md)
