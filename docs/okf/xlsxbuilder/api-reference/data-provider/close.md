---
type: API Reference
title: DataProvider.close(...)
description: Methode close von DataProvider - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/DataProvider.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `default void close()`


Keine Parameter. Rückgabewert: `void`. Default-Implementierung: leerer Methodenrumpf (nichts zu
schließen) — Quellen, die Ressourcen halten (DB, Datei), überschreiben diese Methode. Keine
Exceptions (Override von `Closeable#close()` ohne `throws IOException` in der Default-Variante;
überschreibende Implementierungen könnten theoretisch eine ungechecktes Pendant werfen, tun das
in dieser Bibliothek aber nicht — sie wrappen `SQLException` stattdessen in
`DataAccessException`).

# Citations

[1] [DataProvider (Übersicht)](./data-provider.md)
