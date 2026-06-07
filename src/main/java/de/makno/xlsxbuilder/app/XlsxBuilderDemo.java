package de.makno.xlsxbuilder.app;

import de.makno.xlsxbuilder.builder.ColumnType;
import de.makno.xlsxbuilder.builder.DataProvider;
import de.makno.xlsxbuilder.builder.DataProviders;
import de.makno.xlsxbuilder.builder.WorkbookBuilder;
import de.makno.xlsxbuilder.builder.XlsxBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Demo: creates, out-of-core, a sorted {@code .xlsx} with many rows and one column per
 * {@link ColumnType}. The records are generated <em>lazily</em> via a {@link DataProvider}
 * ({@link EmployeeData#generator(long)}) – the full data set is never held in memory.
 *
 * <p>Usage: {@code XlsxBuilderDemo [rowCount] [outputFile]} (default: 1_000_000 / employees.xlsx).
 */
public final class XlsxBuilderDemo {

    private XlsxBuilderDemo() {}

    /** Data type of the info sheet – shows that each sheet can have its own type. */
    public record Info(String schluessel, String wert) {}

    public static void main(String[] args) throws IOException {
        long rowCount = args.length > 0 ? Long.parseLong(args[0]) : 1_000_000L;
        Path out = Path.of(args.length > 1 ? args[1] : "employees.xlsx");

        Runtime runtime = Runtime.getRuntime();
        long start = System.nanoTime();

        System.out.printf("Creating %s.%n", out.toAbsolutePath());

        // Two employee sheets (same type; demonstrates multiple sheets + name deduplication) plus one
        // info sheet with its own data type.
        WorkbookBuilder.create()
                .sheet(EmployeeData.sheet("Mitarbeiter", EmployeeData.generator(rowCount)))
                .sheet(buildInfoSheet(rowCount))
                .sheet(EmployeeData.sheet("Mitarbeiter_1", EmployeeData.generator(rowCount)))
                .write(out);

        double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long fileMb = Files.size(out) / (1024 * 1024);

        System.out.printf(
                "Done: %,d rows -> %s (%d MB) in %.1fs, used heap ~%d MB, max heap %d MB%n",
                rowCount, out.toAbsolutePath(), fileMb, seconds, usedMb, runtime.maxMemory() / (1024 * 1024));
    }

    /** Builds the info sheet (different data type) – a small static metadata table. */
    private static XlsxBuilder<Info> buildInfoSheet(long rowCount) {
        return XlsxBuilder.<Info>create()
                .sheetName("Info")
                .column("Schlüssel", Info::schluessel)
                .column("Wert", Info::wert)
                .data(DataProviders.ofIterable(List.of(
                        new Info("Bericht", "Mitarbeiterbericht"),
                        new Info("Zeilen", String.format("%,d", rowCount)),
                        new Info("Erstellt am", LocalDate.now().toString()))));
    }
}
