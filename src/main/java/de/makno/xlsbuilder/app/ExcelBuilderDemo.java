package de.makno.xlsbuilder.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import de.makno.xlsbuilder.builder.ColumnType;
import de.makno.xlsbuilder.builder.DataProvider;
import de.makno.xlsbuilder.builder.DataProviders;
import de.makno.xlsbuilder.builder.ExcelBuilder;
import de.makno.xlsbuilder.builder.WorkbookBuilder;

/**
 * Demo: erzeugt out-of-core eine sortierte {@code .xlsx} mit vielen Zeilen und je einer Spalte pro
 * {@link ColumnType}. Die Datensätze werden über einen {@link DataProvider} <em>lazy</em> generiert
 * ({@link EmployeeData#generator(long)}) – es liegt nie die gesamte Datenmenge im Speicher.
 *
 * <p>Aufruf: {@code ExcelBuilderDemo [zeilenanzahl] [ausgabedatei]} (Default: 1_000_000 / employees.xlsx).
 */
public final class ExcelBuilderDemo {

    private ExcelBuilderDemo() {
    }

    /** Datentyp des Info-Blatts – zeigt, dass jedes Blatt einen eigenen Typ haben kann. */
    public record Info(String schluessel, String wert) {
    }

    public static void main(String[] args) throws IOException {
        long rowCount = args.length > 0 ? Long.parseLong(args[0]) : 1_000_000L;
        Path out = Path.of(args.length > 1 ? args[1] : "employees.xlsx");

        Runtime runtime = Runtime.getRuntime();
        long start = System.nanoTime();

        System.out.printf("%s wird erstellt.%n", out.toAbsolutePath());

        // Zwei Mitarbeiter-Blätter (gleicher Typ; demonstriert mehrere Blätter + Namens-Deduplizierung)
        // plus ein Info-Blatt mit eigenem Datentyp.
        WorkbookBuilder.create()
                .sheet(EmployeeData.sheet("Mitarbeiter", EmployeeData.generator(rowCount)))
                .sheet(buildInfoSheet(rowCount))
                .sheet(EmployeeData.sheet("Mitarbeiter_1", EmployeeData.generator(rowCount)))
                .write(out);

        double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long fileMb = Files.size(out) / (1024 * 1024);

        System.out.printf(
                "Fertig: %,d Zeilen -> %s (%d MB) in %.1fs, belegter Heap ~%d MB, max Heap %d MB%n",
                rowCount, out.toAbsolutePath(), fileMb, seconds, usedMb,
                runtime.maxMemory() / (1024 * 1024));
    }

    /** Baut das Info-Blatt (anderer Datentyp) – kleine statische Metadaten-Tabelle. */
    private static ExcelBuilder<Info> buildInfoSheet(long rowCount) {
        return ExcelBuilder.<Info>create()
                .sheetName("Info")
                .column("Schlüssel", Info::schluessel)
                .column("Wert", Info::wert)
                .data(DataProviders.ofIterable(List.of(
                        new Info("Bericht", "Mitarbeiterbericht"),
                        new Info("Zeilen", String.format("%,d", rowCount)),
                        new Info("Erstellt am", LocalDate.now().toString()))));
    }
}
