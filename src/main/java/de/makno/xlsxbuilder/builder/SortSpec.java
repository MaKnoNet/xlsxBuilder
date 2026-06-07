package de.makno.xlsxbuilder.builder;

import java.nio.file.Path;
import java.util.List;

/**
 * Unveränderliche Sortier-Konfiguration eines Blatts: die (mehrstufigen) Sortierschlüssel sowie die
 * Out-of-core-Parameter des External Merge Sort. Leere {@code sortKeys} bedeuten „nicht sortieren".
 *
 * @param sortKeys      mehrstufige Sortierschlüssel in Priorität (leer = keine Sortierung)
 * @param sortChunkSize Zeilen je Sortier-Chunk, die im Speicher gehalten werden, bevor ausgelagert wird
 * @param sortTempDir   Verzeichnis für ausgelagerte Sortier-Runs (oder {@code null} = System-Temp)
 */
record SortSpec(List<SortKey> sortKeys, int sortChunkSize, Path sortTempDir) {}
