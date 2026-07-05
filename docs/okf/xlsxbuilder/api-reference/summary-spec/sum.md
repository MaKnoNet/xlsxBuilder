---
type: API Reference
title: SummarySpec.sum(...)
description: Methode sum von SummarySpec - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/SummarySpec.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `boolean[] sum()` (ueberschriebener Record-Accessor)


Explizit ueberschrieben (nicht der automatisch generierte Accessor): gibt eine Kopie
(`sum.clone()`) zurueck, damit Aufrufer die internen Summen-Flags des Value Objects nicht
nachtraeglich mutieren koennen.

Keine Parameter. Rueckgabewert: `boolean[]`, nie `null` (da das Feld selbst nie `null` sein
kann - der kompakte Konstruktor haette sonst bereits eine `NullPointerException` geworfen), stets
eine frische Kopie bei jedem Aufruf. Keine Exceptions.

# Citations

[1] [SummarySpec (Übersicht)](./summary-spec.md)
