---
type: API Reference
title: RowCodec.writeRow(...)
description: Methode writeRow von RowCodec - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/RowCodec.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `static void writeRow(DataOutputStream out, Row row) throws IOException`


| Parameter | Typ | null-erlaubt |
|---|---|---|
| `out` | `DataOutputStream` | nicht geprüft — `null` führt zu `NullPointerException` beim ersten `out.writeInt(...)` |
| `row` | `Row` | nicht geprüft — `null` führt zu `NullPointerException` bei `row.size()` |

Rückgabewert: `void`. Geworfene Exceptions: `IOException`, wenn der zugrunde liegende Stream
beim Schreiben fehlschlägt (z. B. Datenträger voll); `NullPointerException` bei `out == null`
oder `row == null` (nicht in der Javadoc erwähnt, aber durch fehlenden Null-Check verifiziert).

# Citations

[1] [RowCodec (Übersicht)](./row-codec.md)
