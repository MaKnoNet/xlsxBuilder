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

    /** {@code checkInSeconds} ist die Kommt-Zeit als Sekunden seit Mitternacht (Rohwert int). */
    public record Employee(String name, String department, BigDecimal salary, LocalDate hireDate,
                           int checkInSeconds) {
    }

    public static void main(String[] args) throws IOException {
        long rowCount = args.length > 0 ? Long.parseLong(args[0]) : 1_000_000L;
        Path out = Path.of(args.length > 1 ? args[1] : "employees.xlsx");

        Runtime runtime = Runtime.getRuntime();
        long start = System.nanoTime();

        ExcelBuilder.<Employee>create()
                .sheetName("Mitarbeiter")
                .header("Mitarbeiterbericht", "Erstellt am " + LocalDate.now())
                .column("Name", Employee::name)
                .column("Abteilung", Employee::department)
                .column("Gehalt", Employee::salary).ofType(ColumnType.DECIMAL).formatForType("#,##0.00 \"€\"")
                .column("Eintritt", Employee::hireDate).ofType(ColumnType.DATE).formatForType("dd.mm.yyyy")
                // Rohwert int (Sekunden seit Mitternacht) wird zur Uhrzeit konvertiert.
                .column("Kommt", Employee::checkInSeconds).ofType(ColumnType.TIME).formatForType("hh:mm")
                .convertToColumnType((Integer s) -> LocalTime.ofSecondOfDay(s))
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
                // Kommt-Zeit zwischen 06:00 und 09:59 als Sekunden seit Mitternacht.
                int checkInSeconds = (6 + random.nextInt(4)) * 3600 + random.nextInt(60) * 60;
                return new Employee(name, dept, salary, hire, checkInSeconds);
            }
        };
    }
}
