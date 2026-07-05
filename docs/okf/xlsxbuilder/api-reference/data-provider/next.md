---
type: API Reference
title: DataProvider.next(...)
description: Methode next von DataProvider - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/DataProvider.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `T next()`


Keine Parameter. Rückgabewert: der nächste Datensatz vom Typ `T`. Ob `null` zurückgegeben
werden kann, hängt von der konkreten Implementierung und dem Datentyp `T` ab — das Interface
selbst macht keine Zusicherung. Keine Exceptions in der Interface-Deklaration; konkrete
Implementierungen werfen typischerweise `NoSuchElementException`, wenn kein Element mehr
vorhanden ist (siehe `DataProviders`), oder `DataAccessException` bei einem zugrunde liegenden
SQL-Fehler.

# Citations

[1] [DataProvider (Übersicht)](./data-provider.md)
