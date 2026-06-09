package de.makno.xlsxbuilder;

import java.nio.file.Path;
import java.util.List;

/**
 * Immutable sort configuration of a sheet: the (multi-level) sort keys plus the out-of-core parameters
 * of the External Merge Sort. Empty {@code sortKeys} means "do not sort".
 *
 * @param sortKeys      multi-level sort keys in priority order (empty = no sorting)
 * @param sortChunkSize rows per sort chunk kept in memory before spilling to disk
 * @param sortTempDir   directory for spilled sort runs (or {@code null} = system temp)
 */
record SortSpec(List<SortKey> sortKeys, int sortChunkSize, Path sortTempDir) {}
