---
type: API Reference
title: DataProviders
description: Factory-Klasse mit Adaptern für Iterator/Iterable/Stream/JDBC-ResultSet als DataProvider.
resource: src/main/java/de/makno/xlsxbuilder/DataProviders.java
tags: [api-reference, factory, jdbc, streaming]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`public final class DataProviders` — zustandslose Factory für gängige
[DataProvider](/api-reference/data-provider/data-provider.md)-Adapter. Nicht instanziierbar (privater
No-Op-Konstruktor). Thread-Safety: die Factory-Methoden selbst sind zustandslos und damit
thread-sicher aufrufbar; die zurückgegebenen `DataProvider`-Instanzen sind jedoch — wie der
gesamte `DataProvider`-Vertrag — vorwärts-lesbar/single-use und **nicht** thread-sicher (siehe
[Concurrency contract](/architecture/concurrency-contract.md)).

# Felder

Keine Instanzfelder — die Klasse ist rein statisch (Factory) und besitzt nur den privaten
No-Op-Konstruktor; die zurückgegebenen anonymen `DataProvider`-Implementierungen kapseln ihren
eigenen Zustand (z. B. `lookedAhead`/`hasRow` im `ofResultSet`-Adapter), der aber zu den
jeweiligen anonymen Instanzen gehört, nicht zu `DataProviders` selbst.

# Thread-Safety

Die Klasse selbst ist zustandslos und ihre statischen Factory-Methoden sind thread-sicher
aufrufbar. Die zurückgegebenen `DataProvider`-Instanzen sind jedoch — wie der gesamte
`DataProvider`-Vertrag — vorwärts-lesbar/single-use und **nicht** thread-sicher; der
`ofResultSet`-Adapter im Speziellen ist zusätzlich an die Single-Thread-Semantik des
zugrunde liegenden JDBC-`ResultSet` gebunden.

# Serialisierung

Nicht `Serializable` — `DataProviders` implementiert kein Serialisierungs-Interface
(verifiziert: `public final class DataProviders`, keine `implements`-Klausel).

# equals/hashCode/toString

Keine dieser Methoden ist überschrieben — es gilt die Identitätssemantik von
`java.lang.Object`. Da die Klasse nicht instanziierbar ist (privater Konstruktor), ist das
irrelevant in der Praxis (es existiert nie mehr als die implizite Klassenreferenz).

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `public final class DataProviders` — keine
`extends`-/`implements`-Klausel; erweitert implizit nur `java.lang.Object`.

**Rückwärts:** Keine Ober-/Unterklassen innerhalb dieses Projekts; `final`, daher nicht
erweiterbar. Die Klasse selbst **erzeugt** anonyme
[DataProvider](/api-reference/data-provider/data-provider.md)-Implementierungen (siehe dortige
Vererbungssektion) — das ist jedoch eine Fabrik-Beziehung (Objekterzeugung), keine
Vererbungsbeziehung von `DataProviders` selbst.

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``static <T> DataProvider<T> ofIterator(Iterator<? extends T> iterator)``](./of-iterator.md)
- [``static <T> DataProvider<T> ofIterable(Iterable<? extends T> iterable)``](./of-iterable.md)
- [``static <T> DataProvider<T> ofStream(Stream<? extends T> stream)``](./of-stream.md)
- [``static <T> DataProvider<T> ofResultSet(ResultSet rs, ResultSetRowMapper<? extends T> mapper)``](./of-result-set.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/DataProviders.java`
[2] [DataProvider / DataProviders (Komponente)](/components/data-provider.md)
