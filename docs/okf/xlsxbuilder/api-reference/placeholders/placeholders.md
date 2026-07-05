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

# Felder

| Feld | Typ | Bedeutung | null-erlaubt |
|---|---|---|---|
| `TOKEN` | `private static final Pattern` | Kompiliertes Regex `\{([^{}]+)\}` zum Erkennen von `{key}`-Tokens. | nein — statisch initialisiert, nie `null` |

# Thread-Safety

Zustandslos bis auf das statische, unveränderliche `Pattern TOKEN` (`java.util.regex.Pattern`
ist selbst thread-sicher für parallele `matcher(...)`-Aufrufe) — die statische Methode
`resolve(...)` ist damit ohne Einschränkung parallel aus mehreren Threads aufrufbar.

# Serialisierung

Nicht `Serializable` — `Placeholders` implementiert kein Serialisierungs-Interface (verifiziert:
`final class Placeholders`, keine `implements`-Klausel).

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben — es gilt die Identitätssemantik von
`java.lang.Object`. Da die Klasse nicht instanziierbar ist (privater Konstruktor), ist das ohne
praktische Relevanz.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `final class Placeholders` — keine
`extends`-/`implements`-Klausel; erweitert implizit nur `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, nicht instanziierbar
(privater Konstruktor), daher auch praktisch keine Unterklasse möglich.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``static String resolve(String text, Map<String, String> values)``](./resolve.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/Placeholders.java`
[2] [Konfigurationsobjekte](/components/configuration-models.md)
