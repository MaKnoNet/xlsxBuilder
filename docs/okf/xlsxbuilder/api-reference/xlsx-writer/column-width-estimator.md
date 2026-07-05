---
type: API Reference
title: XlsxWriter.ColumnWidthEstimator (innere Klasse)
description: Private static nested Klasse von XlsxWriter - schätzt Spaltenbreiten inhaltsbasiert während des Streamings.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, nested-class]
timestamp: '2026-07-08T09:00:00+02:00'
---

## Innere Klasse `private static final class ColumnWidthEstimator`

Schätzt Spaltenbreiten inhaltsbasiert, sodass nichts als `#####` angezeigt wird. Da SXSSF
Autosize gespillte Zeilen nicht sehen kann, wird die Breite während des Streamings gemessen:
String-Längen exakt, Zahlenbreite aus Ziffernzahl + Format (inkl. der ggf. großen
Summenwerte). Die finale Breite wird erst gesetzt, nachdem alle Zeilen geschrieben wurden
(bei SXSSF jederzeit möglich) — auf jedem Teil-Sheet, damit ein Split einheitliche Breiten
behält.

Eigenständiges Hilfsobjekt ohne `extends`/`implements`-Klausel — kein eigenes
`api-reference/`-Dokument, da paketprivat/nested (siehe
[XlsxWriter (Übersicht)](./xlsx-writer.md), Abschnitt Vererbungshierarchie).

### `ColumnWidthEstimator(List<? extends Column<?>> columns, boolean showColumnHeaders)`

Konstruktor, initialisiert Breiten-Arrays je Spalte aus Typ, Format und (falls aktiviert)
Spaltenname.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `columns` | `List<? extends Column<?>>` | nicht geprüft — `columns.size()` wird sofort aufgerufen; `null` löst `NullPointerException` aus |
| `showColumnHeaders` | `boolean` | primitiv, kein `null` möglich |

Keine expliziten Null-Prüfungen im Konstruktor selbst.

### `void track(int c, Object value)`

Aktualisiert die geschätzte Breite einer Spalte anhand des konkret geschriebenen Werts.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `c` | `int` | primitiv, kein `null` möglich |
| `value` | `Object` | ja — bei `value == null`: sofortiger Return, keine Änderung |

Rückgabewert: `void`. Für `DATE`/`DATETIME`/`TIME`/`BOOLEAN`/`FORMULA`: kein Tracking
(Basisbreite genügt, `default -> return`). Geworfene Exceptions: `ClassCastException` bzw.
`NumberFormatException` indirekt über `integerDigits(value)`, wenn `value` bei einem
numerischen Typ kein `Number`/`BigDecimal` ist (analog zu `toBigDecimal`).

### `void ensureAtLeast(int c, int chars)`

Stellt sicher, dass eine Spalte mindestens `chars` Zeichen breit ist.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `c` | `int` | primitiv, kein `null` möglich |
| `chars` | `int` | primitiv, kein `null` möglich |

Rückgabewert: `void`. Keine Exceptions bei gültigem Index; bei `c` außerhalb des gültigen
Arrayindex-Bereichs: `ArrayIndexOutOfBoundsException` (kein eigener Bereichscheck).

### `void applyTo(SXSSFSheet sheet)`

Setzt die ermittelten Breiten auf das Sheet (Zeichen -> POI-Einheiten, mit Padding), begrenzt
auf `MAX_WIDTH_CHARS = 255` Zeichen (Excel-Obergrenze).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `sheet` | `SXSSFSheet` | nicht geprüft — `sheet.setColumnWidth(...)` wird direkt aufgerufen; `null` löst `NullPointerException` aus |

Rückgabewert: `void`. Keine Exceptions bei gültigem `sheet`.

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
