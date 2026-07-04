# Konzepte

* [DataProvider<T> und DataProviders](/components/data-provider.md) - Forward-only, single-use data source abstraction with adapters for Iterable, Stream and JDBC ResultSet — the entry point for out-of-core exports.
* [WorkbookBuilder](/components/workbook-builder.md) - Combines any number of XlsxBuilder sheets (each with its own data type) into one .xlsx file and owns the file/workbook lifecycle.
* [XlsxBuilder<T>](/components/xlsx-builder.md) - Fluent configuration of one worksheet — columns, types, formats, converters, sorting, summary row, title/footer, grouped headers; execution is delegated to SheetRenderer.
