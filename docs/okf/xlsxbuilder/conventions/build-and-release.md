---
type: Convention
title: Build, test and release
description: Gradle commands, publishing to mavenLocal for downstream consumers (VaadinExcelExport), and the release checklist including knowledge-base refresh before tagging.
resource: build.gradle
tags: [convention, gradle, build, release, maven]
timestamp: '2026-07-04T17:45:00+02:00'
---

# Commands

| Purpose | Command |
|---|---|
| Build + tests | `./gradlew build` |
| Publish for downstream consumers | `./gradlew publishToMavenLocal` (consumed as `de.makno.xlsxbuilder:xlsxbuilder`, e.g. by VaadinExcelExport) |
| Activate team git hooks | `./gradlew installGitHooks` (runs automatically with the first build) |

# Rules

- **Java 21** (Gradle toolchain); single-module build (`rootProject.name = 'xlsxbuilder'`),
  demo code lives in the `de.makno.xlsxbuilder.app` package.
- License: Apache 2.0.
- **Release:** refresh the knowledge base before tagging (update OKF concepts + `log.md`,
  run `graphify update .`), commit, then tag.

# Citations

[1] [README - Build & run](https://github.com/MaKnoNet/xlsxBuilder/blob/main/README.md)
