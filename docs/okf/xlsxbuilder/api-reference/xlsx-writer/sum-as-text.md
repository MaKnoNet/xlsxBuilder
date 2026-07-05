---
type: API Reference
title: XlsxWriter.sumAsText(...)
description: Methode sumAsText von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static String sumAsText(ColumnType type, BigDecimal sum)`


Textrepräsentation einer Summe für `{sum:Column}`-Platzhalter.

Geworfene Exceptions: `ArithmeticException` (aus `BigDecimal.longValueExact()`), wenn `type`
`INTEGER`/`LONG` ist und die Summe den `long`-Wertebereich überschreitet oder einen
Nachkommaanteil hat — laut Kommentar im Code **bewusst** so belassen ("fails honestly instead of
being truncated silently"), nicht in einer separaten Javadoc dokumentiert, aber ein
sicherheitsrelevantes, verifiziertes Verhalten (kein stilles Abschneiden bei Überlauf).

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
