---
type: API Reference
title: RowLimitExceededException – Konstruktoren
description: Alle Konstruktoren von RowLimitExceededException.
resource: src/main/java/de/makno/xlsxbuilder/RowLimitExceededException.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## `RowLimitExceededException(String message)`

Paketintern — laut Javadoc-Kommentar "nur von der Bibliothek selbst werfbar" (kein öffentlicher
Konstruktor vorhanden, daher von außerhalb des Pakets `de.makno.xlsxbuilder` nicht
instanziierbar).

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `message` | `String` | ja — ungeprüft an `IllegalStateException(String)` durchgereicht, kein `requireNonNull` |

Verhalten bei ungültiger Eingabe: keins — kein Validierungscode im Konstruktor. `null` als
`message` ist zulässig (führt zu `getMessage() == null`).

# Citations

[1] [RowLimitExceededException (Übersicht)](./row-limit-exceeded-exception.md)
