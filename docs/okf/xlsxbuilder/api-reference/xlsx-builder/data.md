---
type: API Reference
title: XlsxBuilder.data(...)
description: Methode data von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> data(DataProvider<T> provider)`


Setzt die Datenquelle dieses Sheets. Erforderlich, bevor das Sheet geschrieben werden kann.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `provider` | `DataProvider<T>` | **nein** — `Objects.requireNonNull(provider, "provider")` |

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`provider == null`.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
