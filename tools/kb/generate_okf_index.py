#!/usr/bin/env python3
"""Deterministischer index.md-Generator fuer OKF-Bundles unter docs/okf/.

Wird vom Pre-Commit-Hook aufgerufen; benoetigt nur die Python-Standardbibliothek.
Idempotent: Dateien werden nur geschrieben, wenn sich der Inhalt aendert.
Zusaetzlich: Warn-only-Konformitaetschecks (OKF v0.1) - blockiert NIE (Exit 0,
ausser bei I/O-Fehlern).
"""

from __future__ import annotations

import sys
from pathlib import Path

OKF_ROOT = Path("docs") / "okf"
OKF_VERSION = "0.1"
RESERVED = {"index.md", "log.md"}


def read_frontmatter(path: Path) -> dict[str, str]:
    """Liest title/description/type aus dem YAML-Frontmatter (naiver Zeilen-Parser)."""
    fields: dict[str, str] = {}
    try:
        lines = path.read_text(encoding="utf-8").splitlines()
    except OSError:
        return fields
    if not lines or lines[0].strip() != "---":
        return fields
    for line in lines[1:]:
        if line.strip() == "---":
            break
        for key in ("title", "description", "type"):
            prefix = key + ":"
            if line.startswith(prefix):
                fields[key] = line[len(prefix):].strip().strip("'\"")
    return fields


def bundle_relative_link(bundle_root: Path, target: Path) -> str:
    """Bundle-root-absoluter Link gemaess OKF-Spec (Abschnitt 5.1)."""
    return "/" + target.relative_to(bundle_root).as_posix()


def render_index(bundle_root: Path, directory: Path) -> str:
    """Erzeugt den index.md-Inhalt fuer ein Verzeichnis (deterministisch sortiert)."""
    subdirs = sorted(
        (d for d in directory.iterdir() if d.is_dir()),
        key=lambda d: d.name.casefold(),
    )
    concepts = sorted(
        (f for f in directory.iterdir() if f.is_file() and f.suffix == ".md" and f.name not in RESERVED),
        key=lambda f: f.name.casefold(),
    )
    log_file = directory / "log.md"

    parts: list[str] = []
    if directory == bundle_root:
        parts.append("---")
        parts.append(f'okf_version: "{OKF_VERSION}"')
        parts.append("---")
        parts.append("")

    if subdirs:
        parts.append("# Unterbereiche")
        parts.append("")
        for sub in subdirs:
            link = bundle_relative_link(bundle_root, sub) + "/"
            parts.append(f"* [{sub.name}]({link})")
        parts.append("")

    if concepts:
        parts.append("# Konzepte")
        parts.append("")
        for concept in concepts:
            fm = read_frontmatter(concept)
            title = fm.get("title") or concept.stem
            description = fm.get("description", "")
            link = bundle_relative_link(bundle_root, concept)
            suffix = f" - {description}" if description else ""
            parts.append(f"* [{title}]({link}){suffix}")
        parts.append("")

    if log_file.is_file():
        parts.append("# Historie")
        parts.append("")
        parts.append(f"* [Update-Log]({bundle_relative_link(bundle_root, log_file)})")
        parts.append("")

    return "\n".join(parts)


def write_if_changed(path: Path, content: str) -> bool:
    """Schreibt nur bei Aenderung (haelt Hook-Ausgabe und git status ruhig)."""
    try:
        if path.is_file() and path.read_text(encoding="utf-8") == content:
            return False
    except OSError:
        pass
    path.write_text(content, encoding="utf-8", newline="\n")
    return True


def check_conformance(bundle_root: Path) -> list[str]:
    """Warn-only OKF-Checks: type-Pflichtfeld, keine relativen ../-Links."""
    warnings: list[str] = []
    for md in bundle_root.rglob("*.md"):
        if md.name in RESERVED:
            continue
        fm = read_frontmatter(md)
        if not fm.get("type"):
            warnings.append(f"{md.as_posix()}: Frontmatter-Pflichtfeld 'type' fehlt")
        try:
            if "](../" in md.read_text(encoding="utf-8"):
                warnings.append(f"{md.as_posix()}: relativer ../-Link (bundle-root-absolut verwenden)")
        except OSError:
            pass
    return warnings


def main() -> int:
    if not OKF_ROOT.is_dir():
        return 0
    changed = 0
    for bundle_root in sorted(OKF_ROOT.iterdir(), key=lambda d: d.name.casefold()):
        if not bundle_root.is_dir():
            continue
        directories = [bundle_root] + sorted(
            (d for d in bundle_root.rglob("*") if d.is_dir()),
            key=lambda d: d.as_posix().casefold(),
        )
        for directory in directories:
            if write_if_changed(directory / "index.md", render_index(bundle_root, directory)):
                changed += 1
        for warning in check_conformance(bundle_root):
            print(f"[kb] WARNUNG (OKF): {warning}")
    if changed:
        print(f"[kb] OKF-Indizes regeneriert: {changed} Datei(en)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
