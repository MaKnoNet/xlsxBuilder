package de.makno.xlsxbuilder.builder;

import java.util.List;
import java.util.function.Predicate;

/**
 * Immutable execution job for a single sheet: all building blocks compiled by the {@link XlsxBuilder}
 * that the {@link SheetRenderer} needs in order to write. Separates the fluent configuration
 * ({@link XlsxBuilder}) from the execution ({@link SheetRenderer}).
 *
 * @param sheetName    sheet name
 * @param columns      column definitions (extractor/type/format/null text)
 * @param filter       optional record filter on the raw object (or {@code null} = all)
 * @param dataProvider forward-only data source (closed by the renderer)
 * @param sort         sort configuration (empty {@code sortKeys} = unsorted)
 * @param summary      summary-row configuration (or {@code null})
 * @param layout       title/footer/placeholder/null layout
 * @param parallel     enable the producer/consumer pipeline?
 * @param <T>          type of the data source's records
 */
record RenderJob<T>(
        String sheetName,
        List<Column<T>> columns,
        Predicate<? super T> filter,
        DataProvider<T> dataProvider,
        SortSpec sort,
        SummarySpec summary,
        SheetWriteOptions layout,
        boolean parallel) {}
