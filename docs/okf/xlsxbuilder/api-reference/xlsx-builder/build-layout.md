---
type: API Reference
title: XlsxBuilder.buildLayout(...)
description: Methode buildLayout von XlsxBuilder - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxBuilder.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private SheetWriteOptions buildLayout()`


Baut die Layout-Optionen inkl. der statisch auflösbaren Platzhalter `{date}`/`{datetime}`
(`putIfAbsent`, überschreibt also keinen bereits vom Aufrufer gesetzten gleichnamigen
Platzhalter — verifiziert). Nicht von außen aufrufbar; hier dokumentiert, weil sie das
Verhalten von `renderInto` erklärt.

# Citations

[1] [XlsxBuilder (Übersicht)](./xlsx-builder.md)
