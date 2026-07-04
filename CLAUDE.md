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

## End-of-Session-Routine (Pflicht bei Code-/Architekturänderungen)

Vor Abschluss einer Session, die Code oder Architektur geändert hat:

1. **OKF-Konzepte aktualisieren:** alle Konzepte unter `docs/okf/xlsxbuilder/`, die von den
   Änderungen berührt sind (neue Komponente → neues Konzept mit `type`-Frontmatter;
   API-Änderung → `# Schema` anpassen; Muster-/Konventionsänderung → `architecture/` bzw.
   `conventions/`).
2. **`docs/okf/xlsxbuilder/log.md`:** Eintrag unter dem heutigen ISO-Datum ergänzen
   (neueste zuerst).
3. **`graphify update .`** ausführen (deterministisch, kein LLM nötig – Kontrolle vor dem
   Commit; der Hook macht es sonst beim Commit).
4. Alles zusammen mit den Code-Änderungen committen.

Frontmatter-Minimum je Konzeptdatei: `type` (Pflicht), `title`, `description`, `resource`
(repo-relativer Pfad), `tags`, `timestamp`. Querverweise bundle-root-absolut,
z. B. `/components/beispiel.md`.
