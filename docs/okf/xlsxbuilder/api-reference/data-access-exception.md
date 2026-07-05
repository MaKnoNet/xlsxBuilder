---
type: API Reference
title: DataAccessException
description: Ungechecktes Wrapping einer SQLException, die in DataProvider-Methoden ohne geprüfte Exception-Signatur auftritt.
resource: src/main/java/de/makno/xlsxbuilder/DataAccessException.java
tags: [api-reference, exception, jdbc]
timestamp: '2026-07-07T10:00:00+02:00'
---

# Überblick

`DataAccessException extends RuntimeException` — ungechecktes Wrapping für eine
`SQLException`, die innerhalb der [DataProvider](/api-reference/data-provider.md)-Methoden
(`hasNext()`/`next()`) auftritt, deren Signaturen keine geprüfte Exception erlauben. Besitzt
eine explizite `serialVersionUID = 1L`. Siehe
[Fehlerbehandlung](/architecture/error-handling.md) für den Gesamtkontext.

# Konstruktoren

## `public DataAccessException(String message, Throwable cause)`

| Parameter | Typ | null-erlaubt |
|---|---|---|
| `message` | `String` | ja — wird ungeprüft an `RuntimeException(String, Throwable)` durchgereicht, kein `requireNonNull` im Konstruktor |
| `cause` | `Throwable` | ja — ebenso ungeprüft durchgereicht |

Verhalten bei ungültiger Eingabe: keins — der Konstruktor validiert nicht selbst; `null` für
`message` oder `cause` ist gemäß `java.lang.Throwable`-Vertrag zulässig (führt lediglich zu
`getMessage() == null` bzw. `getCause() == null`). Keine Exception wird geworfen.

# Methoden

Keine eigenen Methoden — erbt `getMessage()`, `getCause()`, `getStackTrace()` etc. unverändert
von `RuntimeException`/`Throwable`.

# Citations

[1] Quelle: `src/main/java/de/makno/xlsxbuilder/DataAccessException.java`
[2] [Fehlerbehandlung](/architecture/error-handling.md)
