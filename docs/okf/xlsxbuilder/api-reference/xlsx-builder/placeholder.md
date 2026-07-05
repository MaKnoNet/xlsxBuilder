---
type: API Reference
title: XlsxBuilder.placeholder(...)
description: Methode placeholder von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> placeholder(String key, String value)`


Definiert einen Platzhalter `{key}`, der in Titel-, Kopf- und Fußzeilentexten ersetzt wird.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `key` | `String` | **nein** — `Objects.requireNonNull(key, "key")` |
| `value` | `String` | **nein** — `Objects.requireNonNull(value, "value")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn `key` oder
`value` `null` ist. **Bemerkenswert:** anders als der `placeholderResolver` (siehe unten), der
`null` als "kein Ersatzwert gefunden" nutzt, kann ein statischer Platzhalterwert hier **nicht**
`null` sein — konsistent mit `Placeholders.resolve`, das einen `null`-Ersatzwert als "nicht
gefunden" interpretieren würde (die Map speichert daher nur nicht-`null`-Werte).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
