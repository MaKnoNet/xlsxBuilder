---
type: API Reference
title: XlsxBuilder.convertToColumnType(...)
description: Methode convertToColumnType von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `<R> XlsxBuilder<T> convertToColumnType(Function<R, ?> converter)`


Optionaler Converter, der den extrahierten Rohwert der zuletzt definierten Spalte vor dem
Schreiben in die Ziel-Repräsentation transformiert. Wirkt sich auch auf Sortierung und
Summenzeile aus, da bereits zur Projektionszeit angewendet.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `converter` | `Function<R, ?>` | **nein** — `Objects.requireNonNull(converter, "converter")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
- `NullPointerException`, wenn `converter == null`.
- `IllegalStateException("ofType()/formatForType() requires a preceding column(...)")`, wenn
  noch keine Spalte definiert wurde.

**Wichtiger Javadoc-Hinweis, verifiziert korrekt:** es gibt bewusst **keine** automatische
Laufzeitprüfung auf verlustbehaftete Konvertierungen (z. B. `BigDecimal -> double`) —
Performance-Entscheidung auf dem Hot Path; der Aufrufer trägt die Verantwortung für
Präzisionsverlust.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
