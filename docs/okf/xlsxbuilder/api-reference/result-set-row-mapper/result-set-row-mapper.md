---
type: API Reference
title: ResultSetRowMapper
description: Funktionales Interface zum Mappen der aktuellen ResultSet-Zeile auf ein Objekt T, konsumiert von DataProviders.ofResultSet.
resource: src/main/java/de/makno/xlsxbuilder/ResultSetRowMapper.java
tags: [api-reference, functional-interface, jdbc]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick


`@FunctionalInterface public interface ResultSetRowMapper<T>` — liest ausschließlich die
Spalten der **aktuellen** Zeile (z. B. `rs.getString(...)`) und ruft **nicht** `rs.next()` auf
— das übernimmt der Adapter
([DataProviders.ofResultSet](/api-reference/data-providers/of-result-set.md)). Siehe auch
[DataProvider (Komponente) – Supporting types](/components/data-provider.md).

# Felder

Keine Felder — reines funktionales Interface ohne Zustand (eine abstrakte Methode `map(...)`).

# Thread-Safety

Kein Vertrag auf Interface-Ebene erzwingbar. Da Implementierungen typischerweise auf einem
JDBC `ResultSet` operieren (selbst nicht thread-sicher), ist implizit Single-Thread-Nutzung
vorausgesetzt — analog zum Vertrag von [DataProvider](/api-reference/data-provider/data-provider.md).

# Serialisierung

Nicht `Serializable` — `ResultSetRowMapper<T>` implementiert kein Serialisierungs-Interface
(verifiziert: `@FunctionalInterface public interface ResultSetRowMapper<T>`, keine
`extends`-Klausel).

# equals/hashCode/toString

Das Interface deklariert keine dieser Methoden; typischerweise als Lambda implementiert, für
die die JVM eine synthetische, identitätsbasierte Implementierung erzeugt.

# Vererbungshierarchie


**Vorwärts (eigene Deklaration):** `@FunctionalInterface public interface
ResultSetRowMapper<T>` — keine `extends`-Klausel, keine Oberklasse (reines funktionales
Top-Level-Interface).

**Rückwärts:** Keine Klasse im Projekt implementiert dieses Interface namentlich; es wird
ausschließlich als **Lambda-/Parametertyp** in
[DataProviders](/api-reference/data-providers/data-providers.md)`.ofResultSet(ResultSet,
ResultSetRowMapper<? extends T>)` konsumiert — Aufrufer übergeben typischerweise eine Lambda oder
Methodenreferenz, keine benannte Implementierungsklasse existiert im Quellbaum (verifiziert per
Grep nach `implements.*ResultSetRowMapper`, keine Treffer).

# Konstruktoren

- [siehe constructor.md](./constructor.md)

# Methoden

- [``T map(ResultSet rs) throws SQLException``](./map.md)

# Citations


[1] Quelle: `src/main/java/de/makno/xlsxbuilder/ResultSetRowMapper.java`
[2] [DataProvider (Komponente)](/components/data-provider.md)
