---
type: API Reference
title: ColumnGroup – Konstruktoren
description: Alle Konstruktoren von ColumnGroup.
resource: src/main/java/de/makno/xlsxbuilder/ColumnGroup.java
tags: [api-reference, constructor]
timestamp: '2026-07-08T09:00:00+02:00'
---


## Kompakter Konstruktor `ColumnGroup(String label, int span)`

Records haben keinen expliziten Konstruktor im Quelltext außer dem kompakten Validierungs-Konstruktor.

| Parameter | Typ | null-erlaubt | Verhalten bei ungültiger Eingabe |
|---|---|---|---|
| `label` | `String` | **nein** — `Objects.requireNonNull(label, "label")` | wirft `NullPointerException` |
| `span` | `int` | (primitiv, kein `null` möglich) | wirft `IllegalArgumentException("span must be >= 1: " + span)`, wenn `span < 1` |

Verifiziert gegen den Code: Der Javadoc-Kommentar am Record sagt „label darf leer, aber nie
`null` sein" — das stimmt exakt mit der `requireNonNull`-Prüfung überein; kein Widerspruch
gefunden.

# Citations

[1] [ColumnGroup (Übersicht)](./column-group.md)
