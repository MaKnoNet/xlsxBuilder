# Update Log

## 2026-07-08

* **Restructure**: `api-reference/` von einer flachen Datei pro Klasse auf **einen
  Ordner pro Klasse mit einer Datei pro Methode** umgestellt (26 Klassen-Ordner). Jede
  Methoden-Überladung (gleicher Name, andere Signatur, z. B. `Placeholders.resolve(...)`,
  `WorkbookBuilder.write(...)`) landet zusammen in einer Datei. Alle Konstruktoren einer
  Klasse in `constructor.md` gebündelt. Vier neue Pflichtabschnitte je Klassen-
  Übersichtsdatei ergänzt: `# Felder` (jedes Feld mit Bedeutung/null-Verhalten),
  `# Thread-Safety`, `# Serialisierung` (`serialVersionUID` bei `Row`,
  `DataAccessException`, `RowLimitExceededException` — alle `1L`) und
  `# equals/hashCode/toString` (Records komponentenbasiert, sonst Identitätssemantik von
  `Object` explizit festgehalten). Dabei entdeckt und nachgetragen: die
  paketinterne Test-Seam-Methode `maxRowsPerSheet(int)` fehlte komplett in der alten
  `xlsx-builder.md`. Alle Cross-Links im Bundle auf die neuen Pfade migriert, teils
  methodengenau statt nur auf die Klassen-Übersicht verlinkt.

## 2026-07-07 (2)

* **Update**: alle 26 `api-reference/*.md`-Dateien um einen verifizierten
  Abschnitt `# Vererbungshierarchie` ergänzt (Superklasse/Interfaces vorwärts,
  bekannte Implementierer/Subklassen rückwärts, per Grep über den gesamten
  Quellbaum geprüft). Bemerkenswerteste Befunde: `DataProvider` — zentrales
  Interface der Bibliothek — hat **keine** benannte Implementierung, nur
  anonyme Klassen in `DataProviders`; `ResultSetRowMapper`/`SplitSheetNamer`
  haben ebenfalls null Implementierer (nur Lambda-Nutzung); `ExternalMergeSort`
  enthält drei private verschachtelte Klassen mit eigenen `implements`-Klauseln.

## 2026-07-07

* **Creation**: new `api-reference/` category (26 files, one per class in
  `src/main/java/de/makno/xlsxbuilder/`) — exhaustive, code-verified constructor/method
  reference (parameters, null-handling, return semantics, actually-thrown exceptions),
  complementing the narrative `components/`/`architecture/` docs. Cross-checked against
  the real implementation rather than trusting Javadoc comments blindly; found and
  corrected 10 discrepancies between Javadoc claims and actual behavior, e.g. missing
  `Objects.requireNonNull` checks in `ExternalMergeSort`/`Placeholders`/`RowComparator`,
  an undocumented fallback in `XlsxWriter.writeSummaryRow` (formula mode silently
  precomputes when there are zero data rows), and a misleading shared error message
  across `XlsxBuilder.ofType`/`formatForType`/`nullText`/`convertToColumnType`. Full
  details in the respective `api-reference/*.md` files.
* **Update**: [Entwicklerdoku](/conventions/okf-entwicklerdoku.md) und `AGENTS.md`
  um die `api-reference/`-Konvention (Zweck, Abgrenzung zu `components/`, Pflicht zur
  Code-Verifikation) ergänzt.

## 2026-07-06 (2)

* **Update**: project conventions migrated from `CLAUDE.md` to `AGENTS.md` (vendor-neutral
  standard, keeps instructions portable across different AI coding tools). `CLAUDE.md` is
  now just a thin `@AGENTS.md` import. Affects the "single source of truth" references in
  this [developer guide](/conventions/okf-entwicklerdoku.md).

## 2026-07-06

* **Update**: `CLAUDE.md` routine tightened from "end-of-session" to "pre-commit" —
  affected OKF concepts are now updated before every single commit with code/
  architecture changes instead of batched at session end;
  [developer guide](/conventions/okf-entwicklerdoku.md) adjusted accordingly.
* **Creation**: new concept
  [Entwicklerdoku – OKF-Wissensdatenbank pflegen](/conventions/okf-entwicklerdoku.md) —
  bundle structure, frontmatter convention, step-by-step "add a new concept",
  automation/hybrid strategy, known pitfalls.

## 2026-07-05

* **Update**: semantic gap-fill against graphify-out/GRAPH_REPORT.md —
  [out-of-core-pipeline.md](/architecture/out-of-core-pipeline.md) extended with
  `RowCodec` (compact run-file serialization) and `PrefetchingRowIterator`
  (read/sort ∥ write); [data-provider.md](/components/data-provider.md) extended with
  `ResultSetRowMapper`, `Row`, `CloseableIterator`, `RenderJob`.
* **Creation**: new concept
  [configuration-models.md](/components/configuration-models.md) — the immutable
  value-object group `SheetWriteOptions`/`SortSpec`/`SummarySpec`/`ColumnGroup`/
  `SplitSheetNamer`/`Placeholders`.
* **Creation**: new concept [error-handling.md](/architecture/error-handling.md) —
  `RowLimitExceededException`/`DataAccessException`, tied back to the atomic write.

## 2026-07-04

* **Update**: Rebased onto current `main`; concepts refreshed for the new features —
  atomic `write(Path)` (temp file + move) in
  [WorkbookBuilder](/components/workbook-builder.md), Excel row-limit handling
  (`RowLimitExceededException` / `splitOnRowLimit`) in
  [XlsxBuilder](/components/xlsx-builder.md); knowledge graph rebuilt
  (591 nodes, 1775 edges, 46 communities).
* **Initialization**: OKF bundle created — components
  ([XlsxBuilder](/components/xlsx-builder.md),
  [WorkbookBuilder](/components/workbook-builder.md),
  [DataProvider](/components/data-provider.md)), architecture
  ([out-of-core pipeline](/architecture/out-of-core-pipeline.md),
  [concurrency contract](/architecture/concurrency-contract.md)) and conventions
  ([build & release](/conventions/build-and-release.md)) derived from README.md.
* **Creation**: graphify knowledge graph built (481 nodes, 1444 edges, 32 communities);
  kept current automatically by the pre-commit hook from now on.
