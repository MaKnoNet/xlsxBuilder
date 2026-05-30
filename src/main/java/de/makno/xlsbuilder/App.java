package de.makno.xlsbuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Demo: erzeugt out-of-core eine sortierte {@code .xlsx} mit vielen Zeilen.
 * Die Datensätze werden über einen {@link DataProvider} <em>lazy</em> generiert – es liegt nie die
 * gesamte Datenmenge im Speicher.
 *
 * <p>Aufruf: {@code App [zeilenanzahl] [ausgabedatei]} (Default: 1_000_000 / employees.xlsx).
 */
public final class App {

    public record Employee(String name, String department, BigDecimal salary, LocalDate hireDate,
                           LocalTime checkIn) {
    }

    public static void main(String[] args) throws IOException {
        long rowCount = args.length > 0 ? Long.parseLong(args[0]) : 1_000_000L;
        Path out = Path.of(args.length > 1 ? args[1] : "employees.xlsx");

        Runtime runtime = Runtime.getRuntime();
        long start = System.nanoTime();

        ExcelBuilder.<Employee>create()
                .sheetName("Mitarbeiter")
                .header("Mitarbeiterbericht", "Erstellt am " + LocalDate.now())
                .column("Name", ColumnType.STRING, Employee::name)
                .column("Abteilung", ColumnType.STRING, Employee::department)
                .column("Gehalt", ColumnType.DECIMAL, "#,##0.00 \"€\"", Employee::salary)
                .column("Eintritt", ColumnType.DATE, "dd.mm.yyyy", Employee::hireDate)
                .column("Kommt", ColumnType.TIME, "hh:mm", Employee::checkIn)
                .sortBy("Abteilung", SortOrder.ASC)
                .sortBy("Gehalt", SortOrder.DESC)
                .sortChunkSize(100_000)
                .sumColumn("Gehalt")
                .summaryLabel("Name", "Summe")
                .write(employeeGenerator(rowCount), out);

        double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long fileMb = Files.size(out) / (1024 * 1024);

        System.out.printf(
                "Fertig: %,d Zeilen -> %s (%d MB) in %.1fs, belegter Heap ~%d MB, max Heap %d MB%n",
                rowCount, out.toAbsolutePath(), fileMb, seconds, usedMb,
                runtime.maxMemory() / (1024 * 1024));
    }

    /** Lazy-Generator: erzeugt Datensätze erst beim Abruf, nie als komplette Liste im Speicher. */
    private static DataProvider<Employee> employeeGenerator(long count) {
        String[] departments = {"Vertrieb", "Technik", "Marketing", "Personal", "Finanzen", "Support"};
        Random random = new Random(42);
        return new DataProvider<>() {
            private long produced = 0;

            @Override
            public boolean hasNext() {
                return produced < count;
            }

            @Override
            public Employee next() {
                if (produced >= count) {
                    throw new NoSuchElementException();
                }
                produced++;
                String name = "Mitarbeiter-" + produced;
                String dept = departments[random.nextInt(departments.length)];
                BigDecimal salary = BigDecimal.valueOf(30_000 + random.nextInt(90_000))
                        .add(BigDecimal.valueOf(random.nextInt(100), 2))
                        .setScale(2, RoundingMode.HALF_UP);
                LocalDate hire = LocalDate.of(2000, 1, 1).plusDays(random.nextInt(9000));
                LocalTime checkIn = LocalTime.of(6 + random.nextInt(4), random.nextInt(60));
                return new Employee(name, dept, salary, hire, checkIn);
            }
        };
    }
}
