---
type: API Reference
title: XlsxBuilder.placeholderResolver(...)
description: Methode placeholderResolver von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `XlsxBuilder<T> placeholderResolver(Function<String, String> resolver)`


Optionaler Resolver für lazy/berechnete Platzhalter (z. B. Versionsnummer, Benutzername),
konsultiert **nur**, wenn weder die statische Platzhalter-Map noch die eingebauten Platzhalter
den Schlüssel kennen.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `resolver` | `Function<String, String>` | **nein** — `Objects.requireNonNull(resolver, "resolver")` |

**Verifizierte Präzisierung:** der Parameter der `resolver`-Funktion selbst kann `null`
zurückgeben (dokumentiert: "If the resolver returns null, the token stays visible unchanged");
aber der `resolver`-**Funktionswert** (das `Function`-Objekt) darf beim Setzen nicht `null` sein
— zwei unterschiedliche Ebenen von "null-erlaubt", die in der Javadoc klar getrennt sind und
hier bestätigt werden.

Rückgabewert: `this`, nie `null`. Geworfene Exceptions: `NullPointerException`, wenn
`resolver == null`. Wiederholter Aufruf ersetzt den vorherigen Resolver (kein additives
Verhalten, da nur ein Feld).

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
