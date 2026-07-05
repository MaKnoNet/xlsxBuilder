# Projekt-Konventionen - xlsxBuilder

Ergaenzen die globalen Konventionen (~/.claude/CLAUDE.md).

---

# Knowledge Base (graphify + OKF)

Dieses Repo enthält eine automatisch mitgeführte Wissensdatenbank:

- **Wissensgraph:** `graphify-out/graph.json` (+ `GRAPH_REPORT.md`, `graph.html`) –
  deterministisch aus dem Java-AST erzeugt, wird per Pre-Commit-Hook aktuell gehalten.
- **OKF-Bundle:** `docs/okf/xlsxbuilder/` – kuratierte Konzept-Dokumente (Architektur,
  Komponenten, Konventionen) mit YAML-Frontmatter nach der Open-Knowledge-Format-Spec v0.1.
- **`index.md`-Dateien sind GENERIERT** (`tools/kb/generate_okf_index.py`) – NIE von Hand
  editieren; der Pre-Commit-Hook regeneriert sie.

## Graph-First-Regel

Bei Fragen zur Codebasis (Struktur, Abhängigkeiten, „wo ist X?", „was nutzt Y?") ZUERST den
Wissensgraphen befragen (`graphify query "<Frage>"` bzw. `GRAPH_REPORT.md`), dann erst frei
suchen.

## Pre-Commit-Routine (Pflicht vor JEDEM Commit mit Code-/Architekturänderungen)

Nicht erst am Sitzungsende bündeln – vor **jedem einzelnen** Commit, der Code oder
Architektur ändert:

1. **OKF-Konzepte aktualisieren:** alle Konzepte unter `docs/okf/xlsxbuilder/`, die von den
   Änderungen dieses Commits berührt sind (neue Komponente → neues Konzept mit
   `type`-Frontmatter; Muster-/Konventionsänderung → `architecture/` bzw. `conventions/`).
   **Bei jeder Änderung an einer Methoden-Signatur, einem Null-Check oder einer geworfenen
   Exception:** die zugehörige `api-reference/<klasse>.md`-Datei aktualisieren — gegen den
   tatsächlichen Code verifizieren, nicht nur den Javadoc-Kommentar übernehmen (siehe
   [Entwicklerdoku](/conventions/okf-entwicklerdoku.md)).
2. **`docs/okf/xlsxbuilder/log.md`:** Eintrag unter dem heutigen ISO-Datum ergänzen
   (neueste zuerst).
3. **`graphify update .`** ausführen (deterministisch, kein LLM nötig – Kontrolle vor dem
   Commit; der Hook macht es sonst beim Commit).
4. **Erst dann committen** – Doku-Änderung und Code-Änderung landen im selben Commit,
   nie in getrennten "Doku hinterher"-Commits.

Trivial-Commits ohne Code-/Architekturrelevanz (Formatierung, reine Kommentar-Tippfehler
o. Ä.) sind von dieser Routine ausgenommen.

Frontmatter-Minimum je Konzeptdatei: `type` (Pflicht), `title`, `description`, `resource`
(repo-relativer Pfad), `tags`, `timestamp`. Querverweise bundle-root-absolut,
z. B. `/components/beispiel.md`.
