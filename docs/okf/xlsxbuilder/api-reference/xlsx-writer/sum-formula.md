---
type: API Reference
title: XlsxWriter.sumFormula(...)
description: Methode sumFormula von XlsxWriter - siehe Signatur(en) unten.
resource: src/main/java/de/makno/xlsxbuilder/XlsxWriter.java
tags: [api-reference, method]
timestamp: '2026-07-08T09:00:00+02:00'
---

## `private String sumFormula(int columnIndex)`


Baut den `SUM(...)`-Ausdruck über alle Datenbereiche. Ohne Split bleibt das der einfache
Bereich auf demselben Sheet (`SUM(B3:B5)`); mit Split wird jeder Bereich mit seinem Sheet-Namen
qualifiziert (in Anführungszeichen, falls nötig), z. B. `SUM('Employees'!B3:B1048570,'Employees
(2)'!B2:B123)`. Keine Exceptions, reine String-Konstruktion.

# Citations

[1] [XlsxWriter (Übersicht)](./xlsx-writer.md)
