# Konzepte

* [CloseableIterator](/api-reference/closeable-iterator.md) - Iterator, das Ressourcen (z. B. offene Run-Dateien) hält und ohne geprüfte Exception schließbar ist.
* [ColumnGroup](/api-reference/column-group.md) - Unveränderlicher Record für eine Zelle der optionalen gruppierten Kopfzeile — Label plus Spanne über eine Anzahl Spalten.
* [ColumnType](/api-reference/column-type.md) - Enum für den logischen Typ einer Spalte — steuert Zelltyp/-format beim Schreiben und die Sortierbarkeit.
* [Column](/api-reference/column.md) - Paketinterner, unveränderlicher Value-Type für eine Tabellenspalte — Name, logischer Typ, Format, Null-Text, Extractor und optionaler Converter.
* [DataAccessException](/api-reference/data-access-exception.md) - Ungechecktes Wrapping einer SQLException, die in DataProvider-Methoden ohne geprüfte Exception-Signatur auftritt.
* [DataProvider](/api-reference/data-provider.md) - Vorwärts-lesbare, einmal durchlaufbare Datenquellen-Abstraktion für XlsxBuilder mit Default-close().
* [DataProviders](/api-reference/data-providers.md) - Factory-Klasse mit Adaptern für Iterator/Iterable/Stream/JDBC-ResultSet als DataProvider.
* [ExternalMergeSort](/api-reference/external-merge-sort.md) - Paketinterne externe Sortierung über Row-Objekte mit fan-in-begrenztem k-way Merge; Temp-Dateien werden bei close() gelöscht.
* [Placeholders](/api-reference/placeholders.md) - Paketinterner Utility-Ersatz für {key}-Platzhalter in Titel-/Kopf-/Fußzeilen; unbekannte Tokens bleiben sichtbar stehen.
* [PrefetchingRowIterator](/api-reference/prefetching-row-iterator.md) - Paketinterner CloseableIterator, der Lesen/Sortieren und Schreiben über einen daemon Hintergrundthread und eine begrenzte BlockingQueue überlappt.
* [RenderJob](/api-reference/render-job.md) - Paketinterner, unveränderlicher Record — die vollständige Ausführungsbeschreibung eines Sheets, kompiliert vom XlsxBuilder für den SheetRenderer.
* [ResultSetRowMapper](/api-reference/result-set-row-mapper.md) - Funktionales Interface zum Mappen der aktuellen ResultSet-Zeile auf ein Objekt T, konsumiert von DataProviders.ofResultSet.
* [RowCodec](/api-reference/row-codec.md) - Paketinterne, kompakte typmarkierte (De-)Serialisierung einer Row für die Run-Dateien der ExternalMergeSort, mit gehärteten Deserialisierungs-Limits im Java-Fallback.
* [RowComparator](/api-reference/row-comparator.md) - Paketinterner Comparator<Row> aus einer Liste von SortKeys — null-sicher, mehrstufig, ASC/DESC.
* [RowLimitExceededException](/api-reference/row-limit-exceeded-exception.md) - Wird geworfen, wenn ein Sheet die maximale Zeilenzahl pro Worksheet überschreitet und kein Split konfiguriert ist.
* [Row](/api-reference/row.md) - Paketinterne projizierte Datenzeile — bereits extrahierte Zellwerte, ein Wert je Spalte; Serializable für den Spill der ExternalMergeSort.
* [SheetRenderer](/api-reference/sheet-renderer.md) - Paketinterner, zustandsloser Ausführer eines RenderJob — Projektion, optionale Out-of-Core-Sortierung, optionales Prefetching, Schreiben via XlsxWriter.
* [SheetWriteOptions](/api-reference/sheet-write-options.md) - Paketinterner, unveränderlicher Record — bündelt alle Layout-Parameter für das Schreiben eines Sheets, mit defensiven Kopien im kompakten Konstruktor.
* [SortKey](/api-reference/sort-key.md) - Öffentlicher, unveränderlicher Record — eine Sortierstufe (Spaltenname + Richtung); mehrere Keys ergeben eine mehrstufige Sortierung.
* [SortOrder](/api-reference/sort-order.md) - Öffentliches Enum für die Sortierrichtung — ASC oder DESC.
* [SortSpec](/api-reference/sort-spec.md) - Paketinterner, unveränderlicher Record — die Sortier-Konfiguration eines Sheets (mehrstufige Sortierschlüssel + Out-of-Core-Parameter der External Merge Sort).
* [SplitSheetNamer](/api-reference/split-sheet-namer.md) - Öffentliches funktionales Interface zum Benennen von Folge-Sheets, die bei splitOnRowLimit(true) entstehen.
* [SummarySpec](/api-reference/summary-spec.md) - Paketinterner, unveraenderlicher Record - Konfiguration der optionalen Summenzeile, mit defensiver Kopie des veraenderlichen sum-Arrays.
* [WorkbookBuilder](/api-reference/workbook-builder.md) - Oeffentlicher, nicht thread-sicherer Single-Use-Builder, der beliebig viele XlsxBuilder-Sheets zu einer .xlsx-Datei kombiniert und den Datei-/Workbook-Lifecycle inkl. atomarem Schreiben besitzt.
* [XlsxBuilder](/api-reference/xlsx-builder.md) - Oeffentlicher, fluenter, nicht thread-sicherer Single-Use-Builder fuer genau ein Sheet mit eigenem Datentyp T - Spalten, Sortierung, Summenzeile, Layout, Split-Handling und Parallelitaet.
* [XlsxWriter](/api-reference/xlsx-writer.md) - Paketinterner Schreiber einer .xlsx-Datei mit Apache POI im Streaming-Modus (SXSSF) — Zeilenlimit-Handling mit Split, Summenzeile, Spaltenbreiten-Schätzung.
