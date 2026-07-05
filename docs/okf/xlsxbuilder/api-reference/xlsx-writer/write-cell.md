---
type: API Reference
title: XlsxWriter.writeCell(...)
description: Methode writeCell von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private static void writeCell(org.apache.poi.ss.usermodel.Row row, int col, ColumnType type, Object value, CellStyle style)`


Schreibt einen einzelnen Zellwert je nach `ColumnType`.

Geworfene Exceptions (über `setDateValue`/`setTimeValue`):
- `IllegalArgumentException("Unsupported date type: " + value.getClass().getName())`, wenn
  `type` `DATE`/`DATETIME` ist und `value` weder `LocalDate`, `LocalDateTime` noch
  `java.util.Date` ist.
- `IllegalArgumentException("Unsupported time type: " + value.getClass().getName())`, wenn
  `type` `TIME` ist und `value` weder `LocalTime` noch `LocalDateTime` ist.
- `ClassCastException`, wenn `type` `INTEGER`/`LONG`/`DOUBLE` ist und `value` kein `Number` ist
  (Cast `((Number) value)` ohne vorherige `instanceof`-Prüfung) — **nicht dokumentiert**, aber
  durch den Code verifiziert: anders als bei DATE/TIME gibt es hier **keinen** sprechenden
  `IllegalArgumentException`-Pfad, sondern eine rohe `ClassCastException` mit
  Standard-JDK-Meldung.
- `NumberFormatException` (aus `new BigDecimal(value.toString())`), wenn `type == DECIMAL` und
  `value` weder `BigDecimal` noch in einen gültigen `BigDecimal`-String-Repräsentation
  konvertierbar ist.
- Bei `value == null` wird **keine** Zelle erzeugt (frühzeitiger Return "don't even create an
  empty cell") — abweichend vom Verhalten in `writeDataRow`, das für `null`-Werte explizit eine
  `CellType.BLANK`-Zelle erzeugt, wenn kein Null-Text konfiguriert ist. Diese Methode
  (`writeCell`) wird jedoch nur mit bereits als nicht-`null` bekannten Werten aufgerufen (aus
  `writeDataRow` und `writeSummaryRow`), sodass dieser Zweig in der Praxis nur als defensive
  Absicherung dient.

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
