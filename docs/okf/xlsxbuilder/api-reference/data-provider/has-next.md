---
type: API Reference
title: DataProvider.hasNext(...)
description: Methode hasNext von DataProvider - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/DataProvider.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `boolean hasNext()`


Keine Parameter. Rückgabewert: `true`, solange ein weiterer Datensatz verfügbar ist; primitiv,
nie `null`. Keine Exceptions in der Interface-Deklaration; konkrete Implementierungen (z. B.
`DataProviders.ofResultSet`) können ungecheckte Exceptions werfen (dort:
`DataAccessException`, siehe [DataProviders.ofResultSet](/api-reference/data-providers/of-result-set.md)).

# Citations

[1] [DataProvider (Übersicht)](./data-provider.md)
