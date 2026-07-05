---
type: API Reference
title: XlsxBuilder.placeholders(...)
description: Methode placeholders von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> placeholders(Map<String, String> values)`


Fügt mehrere Platzhalter auf einmal hinzu (delegiert intern an `placeholder(String, String)`
pro Eintrag über `values.forEach(this::placeholder)`).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `values` | `Map<String, String>` | **nein** — `Objects.requireNonNull(values, "values")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions:
- `NullPointerException`, wenn `values == null`.
- `NullPointerException`, wenn ein Schlüssel oder Wert in `values` selbst `null` ist (da jeder
  Eintrag durch `placeholder(key, value)` läuft, welches beide Parameter `requireNonNull`
  prüft) — nicht explizit im Javadoc dieser Methode erwähnt, aber durch die Delegation
  verifiziert.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
