---
type: API Reference
title: RenderJob
description: Paketinterner, unveränderlicher Record — die vollständige Ausführungsbeschreibung eines Sheets, kompiliert vom XlsxBuilder für den SheetRenderer.
resource: src/main/java/de/makno/xlsxbuilder/RenderJob.java
tags: [api-reference, record, value-object, package-private]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`record RenderJob<T>(...)` — paketintern, unveränderlich. Trennt die fluente Konfiguration
([XlsxBuilder](/api-reference/xlsx-builder/xlsx-builder.md)) von der Ausführung
([SheetRenderer](/api-reference/sheet-renderer/sheet-renderer.md)). Siehe auch
[DataProvider (Komponente) – Supporting types](/components/data-provider.md).

# Felder

Felder = Record-Komponenten (`sheetName`, `columns`, `filter`, `dataProvider`, `sort`,
`summary`, `layout`, `parallel`), siehe [Konstruktor](./constructor.md) für Typen und
Null-Erlaubtheit je Komponente (dort aus dem Javadoc der Record-Deklaration übernommen).

# Thread-Safety

Immutable Value-Type (Record ohne kompakten Konstruktor mit Validierung/Kopie) — alle
Komponenten sind `final`. Trägt jedoch selbst einen `DataProvider<T>` (single-use, nicht
thread-sicher) und ggf. einen zustandsbehafteten `filter`/`sort`; ein `RenderJob` ist damit
zwar strukturell unveränderlich, aber praktisch — wie der gesamte `DataProvider`-Vertrag — für
einmalige, sequenzielle Verarbeitung durch einen Thread gedacht.

# Serialisierung

Nicht `Serializable` — `RenderJob<T>` implementiert kein Serialisierungs-Interface (verifiziert:
`record RenderJob<T>(...) {}`, keine `implements`-Klausel). Wäre wegen des enthaltenen
`DataProvider<T>`/`Predicate<? super T>` (beliebige Lambdas/Ressourcen) ohnehin nicht sinnvoll
serialisierbar.

# equals/hashCode/toString

Automatisch generiert (Record): komponentenbasiert über alle acht Komponenten, keine eigenen
Overrides im Quellcode. Da `dataProvider`/`filter` beliebige Objekte ohne eigene
`equals`-Semantik sein können, ist die generierte Gleichheit in der Praxis auf Identität dieser
Objekte angewiesen — nicht für Sammlungs-Deduplizierung gedacht.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `record RenderJob<T>(...)` — erweitert implizit
`java.lang.Record`; keine `implements`-Klausel im Quelltext.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; Records sind implizit `final`.
Wird nur als Rückgabe-/Parametertyp zwischen `XlsxBuilder` und `SheetRenderer` verwendet — keine
Vererbungsbeziehung.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden


# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/RenderJob.java`
[2] [DataProvider (Komponente)](/components/data-provider.md)
