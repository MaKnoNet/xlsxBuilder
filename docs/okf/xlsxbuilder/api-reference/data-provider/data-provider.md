---
type: API Reference
title: DataProvider
description: Vorwärts-lesbare, einmal durchlaufbare Datenquellen-Abstraktion für XlsxBuilder mit Default-close().
resource: src/main/java/de/makno/xlsxbuilder/DataProvider.java
tags: [api-reference, interface, streaming, data]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`DataProvider<T> extends Closeable` — abstrahiert die Datenquelle für
[XlsxBuilder](/api-reference/xlsx-builder/xlsx-builder.md). Wird genau einmal durchlaufen, sodass auch nicht
vollständig in den Speicher passende Datenmengen verarbeitet werden können (z. B. ein JDBC
`ResultSet` oder ein gepufferter Datei-Reader). Vertrag laut
[Konzeptdokumentation](/components/data-provider.md): **vorwärts-lesbar, single-use, nicht
teilen** — eigene Quelle pro Request. Fabrikmethoden dafür: siehe
[DataProviders](/api-reference/data-providers/data-providers.md).

# Felder

Keine Felder — reines Interface ohne Zustand (`hasNext()`, `next()` abstrakt; `close()` mit
No-Op-Default-Implementierung).

# Thread-Safety

Kein Vertrag auf Interface-Ebene erzwingbar, aber explizit dokumentiert (Javadoc, verifiziert):
vorwärts-lesbar, **single-use, nicht zwischen Threads teilen** — jede konkrete Quelle
(anonyme Implementierungen in [DataProviders](/api-reference/data-providers/data-providers.md))
erbt diesen Vertrag.

# Serialisierung

Nicht `Serializable` — `DataProvider<T>` implementiert kein Serialisierungs-Interface
(verifiziert: `public interface DataProvider<T> extends Closeable`, `Closeable` selbst ist
nicht `Serializable`).

# equals/hashCode/toString

Das Interface deklariert keine dieser Methoden; jede (anonyme) Implementierung erbt die
Default-Semantik von `java.lang.Object`, sofern sie sie nicht selbst überschreibt (die
anonymen Implementierungen in `DataProviders` tun das nicht — verifiziert).

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `public interface DataProvider<T> extends Closeable` —
erweitert das JDK-Interface `java.io.Closeable`; keine eigene Oberklasse.

**Rückwärts (Implementierer innerhalb dieses Projekts, verifiziert per Grep über
`implements.*DataProvider`/`extends.*DataProvider`):** Es gibt **keine benannte** Klasse, die
`DataProvider` implementiert — alle Implementierungen sind **anonyme Klassen** innerhalb von
[DataProviders](/api-reference/data-providers/data-providers.md) (`ofIterator`, `ofIterable`, `ofStream`,
`ofResultSet` geben jeweils `new DataProvider<T>() { ... }` zurück). Diese anonymen
Implementierungen haben keine eigene `api-reference/`-Datei, da sie nicht separat benannt/exportiert
sind. Kein anderer Produktionscode im Projekt implementiert dieses Interface direkt.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``boolean hasNext()``](./has-next.md)
- [``T next()``](./next.md)
- [``default void close()``](./close.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/DataProvider.java`
[2] [DataProvider / DataProviders (Komponente)](/components/data-provider.md)
