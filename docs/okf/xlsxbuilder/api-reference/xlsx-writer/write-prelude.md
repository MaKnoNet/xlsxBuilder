---
type: API Reference
title: XlsxWriter.writePrelude(...)
description: Methode writePrelude von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private int writePrelude(SXSSFSheet sheet)`


Schreibt die Zeilen oberhalb der Daten (Titelzeilen, Gruppenkopf, Spaltenköpfe). Schützt vor
einem Limit, das keinen Platz für Daten lässt.

Rückgabewert: `int`, erste Datenzeile (0-basiert).

Geworfene Exceptions: `IllegalStateException("maxRowsPerSheet=" + ... + " leaves no room for
data rows: " + ... + " title/header rows plus " + ... + " reserved summary/footer rows")`, wenn
bereits die Vorspann-Zeilen (Titel/Gruppen/Köpfe) das reservierte Zeilenkontingent erreichen —
laut Kommentar im Code nur mit dem Test-Seam (künstlich kleine Limits) praktisch erreichbar.

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
