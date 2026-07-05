---
type: API Reference
title: Placeholders
description: Paketinterner Utility-Ersatz für {key}-Platzhalter in Titel-/Kopf-/Fußzeilen; unbekannte Tokens bleiben sichtbar stehen.
resource: src/main/java/de/makno/xlsxbuilder/Placeholders.java
tags: [api-reference, utility, package-private, text-processing]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`final class Placeholders` — paketintern, nicht instanziierbar (privater No-Op-Konstruktor).
Ersetzt `{key}`-Tokens in Titel-, Kopf- und Fußzeilentexten. Unbekannte Platzhalter bleiben
unverändert **sichtbar** stehen statt still verschluckt zu werden — bewusste Design-Entscheidung,
siehe [Konfigurationsobjekte](/components/configuration-models.md). Zustandslos bis auf das
statische, unveränderliche `Pattern TOKEN` — thread-sicher aufrufbar.

# Konstruktoren

## `private Placeholders()`

Leerer privater Konstruktor, verhindert Instanziierung. Keine Parameter, keine Exceptions.

# Methoden

## `static String resolve(String text, Map<String, String> values)`

Ersetzt alle `{key}`-Tokens in `text` anhand von `values`. Delegiert an die Drei-Parameter-
Variante mit `fallback = null`.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `text` | `String` | **ja** — bei `text == null` gibt die Methode sofort `text` (also `null`) zurück (verifiziert: `if (text == null || ...) return text;`) |
| `values` | `Map<String, String>` | **nicht geprüft, aber faktisch erforderlich** — `values.isEmpty()` wird ohne Null-Check aufgerufen; ein `null`-`values` führt zu `NullPointerException`, sobald `text` nicht `null` ist und mindestens ein `{` enthält (der Kurzschluss-Check `text.indexOf('{') < 0` greift vorher nur bei fehlendem `{`) |

Rückgabewert: der aufgelöste Text; **kann `null` sein**, wenn `text == null` übergeben wurde
(direkte Rückgabe ohne Transformation). Ansonsten nie `null` (String-Verarbeitung über
`StringBuilder`).

Geworfene Exceptions: `NullPointerException`, wenn `values == null` und `text` nicht `null` ist
und mindestens eine `{`-Klammer enthält (nicht in der Javadoc erwähnt, aber durch fehlenden
Null-Check verifiziert).

## `static String resolve(String text, Map<String, String> values, Function<String, String> fallback)`

Ersetzt alle `{key}`-Tokens in `text`. Reihenfolge pro Token: zuerst die statische `values`-Map,
dann — falls dort nicht vorhanden — der optionale `fallback`-Resolver (lazy/berechnete Werte).
Liefert auch dieser `null`, bleibt das Token unverändert stehen. Die statische Map hat also
Vorrang vor dem Resolver.

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `text` | `String` | **ja** — Kurzschluss-Rückgabe von `text` selbst, wenn `text == null` |
| `values` | `Map<String, String>` | **nicht `null`, wenn `text` nicht-`null` ist und `{` enthält** — sonst `NullPointerException` bei `values.isEmpty()`; ist `fallback` gesetzt und `values` leer aber nicht `null`, funktioniert es normal |
| `fallback` | `Function<String, String>` | **ja** — `null` bedeutet "kein Fallback"; wird nur aufgerufen, wenn `values.get(key) == null` |

Rückgabewert: aufgelöster Text. **Kann `null` sein**, wenn `text == null` war (direkte
Rückgabe) — ansonsten nie `null`. Frühzeitiger Ausstieg (Rückgabe von `text` unverändert, ohne
Verarbeitung) auch dann, wenn `text.indexOf('{') < 0` (keine Platzhalter vorhanden) **oder**
`values.isEmpty() && fallback == null` (nichts zum Ersetzen vorhanden) — in diesem zweiten Fall
wird `values.isEmpty()` ausgewertet, was bei `values == null` eine `NullPointerException`
auslöst, selbst wenn `text` gar keine Platzhalter enthalten hätte, sofern `text.indexOf('{') >=
0` zufällig wahr ist (Reihenfolge der `||`-Bedingung: `text.indexOf('{') < 0` wird zuerst
geprüft und kann den `values`-Zugriff überspringen).

Geworfene Exceptions: `NullPointerException` bei `values == null` unter obigen Bedingungen.
Keine sonstigen Exceptions — unbekannte Tokens werden nicht als Fehler behandelt, sondern
(verifiziert über `Matcher.quoteReplacement(matcher.group())`) unverändert stehen gelassen.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/Placeholders.java`
[2] [Konfigurationsobjekte](/components/configuration-models.md)
