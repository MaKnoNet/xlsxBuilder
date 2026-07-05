---
type: API Reference
title: DataAccessException – Konstruktoren
description: Alle Konstruktoren von DataAccessException.
resource: src/main/java/de/makno/xlsxbuilder/DataAccessException.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## `public DataAccessException(String message, Throwable cause)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `message` | `String` | ja — wird ungeprüft an `RuntimeException(String, Throwable)` durchgereicht, kein `requireNonNull` im Konstruktor |
| `cause` | `Throwable` | ja — ebenso ungeprüft durchgereicht |

Verhalten bei ungültiger Eingabe: keins — der Konstruktor validiert nicht selbst; `null` für
`message` oder `cause` ist gemäß `java.lang.Throwable`-Vertrag zulässig (führt lediglich zu
`getMessage() == null` bzw. `getCause() == null`). Keine Exception wird geworfen.

# Citations

[1] [DataAccessException (Übersicht)](./data-access-exception.md)
