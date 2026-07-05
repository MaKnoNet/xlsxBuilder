---
type: API Reference
title: RowCodec.readRow(...)
description: Methode readRow von RowCodec - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/RowCodec.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `static Row readRow(DataInputStream in) throws IOException`


| Parameter | Typ | null-erlaubt |
|---|---|---|
| `in` | `DataInputStream` | nicht geprüft — `null` führt zu `NullPointerException` bei `in.readInt()` |

Rückgabewert: die gelesene `Row`, nie `null` bei erfolgreichem Rückgabepfad (immer frisch
konstruiert). Geworfene Exceptions: `IOException` bei einem Lese-/Formatfehler (u. a. über
`readValue`/`readBytes`, siehe unten); `EOFException` (Unterklasse von `IOException`), wenn der
Stream vorzeitig endet (`DataInputStream.readInt()`/`readFully(...)`); `NullPointerException`
bei `in == null`.

# Citations

[1] [RowCodec (Übersicht)](./row-codec.md)
