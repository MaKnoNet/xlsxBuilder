package de.makno.xlsbuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Demo: erzeugt out-of-core eine sortierte {@code .xlsx} mit vielen Zeilen und je einer Spalte pro
 * {@link ColumnType}. Die Datensätze werden über einen {@link DataProvider} <em>lazy</em> generiert –
 * es liegt nie die gesamte Datenmenge im Speicher.
 *
 * <p>Aufruf: {@code App [zeilenanzahl] [ausgabedatei]} (Default: 1_000_000 / employees.xlsx).
 */
public final class App {

    /**
     * Felder decken alle Spaltentypen ab. {@code checkInSeconds} ist die Kommt-Zeit als Sekunden seit
     * Mitternacht (Rohwert int), der per Konverter in eine Uhrzeit umgewandelt wird.
     */
    public record Employee(long id, String name, String department, int age, double rating,
                           BigDecimal salary, boolean active, LocalDate hireDate,
                           LocalDateTime lastLogin, int checkInSeconds) {
    }

    /** Datentyp des zweiten Blatts „Info" – zeigt, dass jedes Blatt einen eigenen Typ haben kann. */
    public record Info(String schluessel, String wert) {
    }

    public static void main(String[] args) throws IOException {
        long rowCount = args.length > 0 ? Long.parseLong(args[0]) : 1_000_000L;
        Path out = Path.of(args.length > 1 ? args[1] : "employees.xlsx");

        Runtime runtime = Runtime.getRuntime();
        long start = System.nanoTime();

        System.out.printf("%s wird erstellt.",out.toAbsolutePath());
        System.out.println();
        
        // Blatt 1: Mitarbeiter (Employee) – je eine Spalte pro Typ, sortiert, mit Summenzeile.
        ExcelBuilder<Employee> mitarbeiter = ExcelBuilder.<Employee>create()
                .sheetName("Mitarbeiter")
                .header("Mitarbeiterbericht", "Erstellt am " + LocalDate.now())
                .column("ID", Employee::id).ofType(ColumnType.LONG)
                .column("Name", Employee::name)                                  // STRING (Default)
                .column("Abteilung", Employee::department)                       // STRING
                .column("Alter", Employee::age).ofType(ColumnType.INTEGER)
                .column("Bewertung", Employee::rating).ofType(ColumnType.DOUBLE).formatForType("0.0")
                .column("Gehalt", Employee::salary).ofType(ColumnType.DECIMAL).formatForType("#,##0.00 \"€\"")
                .column("Aktiv", Employee::active).ofType(ColumnType.BOOLEAN)
                .column("Eintritt", Employee::hireDate).ofType(ColumnType.DATE).formatForType("dd.mm.yyyy")
                .column("Letzter Login", Employee::lastLogin).ofType(ColumnType.DATETIME)
                .formatForType("dd.mm.yyyy hh:mm")
                // Rohwert int (Sekunden seit Mitternacht) wird zur Uhrzeit (TIME) konvertiert.
                .column("Kommt", Employee::checkInSeconds).ofType(ColumnType.TIME).formatForType("hh:mm")
                .convertToColumnType((Integer s) -> LocalTime.ofSecondOfDay(s))
                // Formelspalte: Bonus = 10 % vom Gehalt (Spalte F); {row} = aktuelle Zeilennummer.
                .column("Bonus", e -> "F{row}*0.1").ofType(ColumnType.FORMULA).formatForType("#,##0.00 \"€\"")
                .sortBy("Abteilung", SortOrder.ASC)
                .sortBy("Gehalt", SortOrder.DESC)
                .sortChunkSize(100_000)
                .sumColumn("Gehalt")
                .summaryLabel("Name", "Summe")
                .summaryAsFormula(true) // Summenzeile als echte =SUMME(...)-Formel
                .data(employeeGenerator(rowCount));

        // Blatt 2: Info (anderer Datentyp) – kleine statische Metadaten-Tabelle.
        ExcelBuilder<Info> info = ExcelBuilder.<Info>create()
                .sheetName("Info")
                .column("Schlüssel", Info::schluessel)
                .column("Wert", Info::wert)
                .data(DataProviders.ofIterable(List.of(
                        new Info("Bericht", "Mitarbeiterbericht"),
                        new Info("Zeilen", String.format("%,d", rowCount)),
                        new Info("Erstellt am", LocalDate.now().toString()))));

        
        
        ExcelBuilder<Employee> mitarbeiter_1 = ExcelBuilder.<Employee>create()
                .sheetName("Mitarbeiter_1")
                .header("Mitarbeiterbericht", "Erstellt am " + LocalDate.now())
                .column("ID", Employee::id).ofType(ColumnType.LONG)
                .column("Name", Employee::name)                                  // STRING (Default)
                .column("Abteilung", Employee::department)                       // STRING
                .column("Alter", Employee::age).ofType(ColumnType.INTEGER)
                .column("Bewertung", Employee::rating).ofType(ColumnType.DOUBLE).formatForType("0.0")
                .column("Gehalt", Employee::salary).ofType(ColumnType.DECIMAL).formatForType("#,##0.00 \"€\"")
                .column("Aktiv", Employee::active).ofType(ColumnType.BOOLEAN)
                .column("Eintritt", Employee::hireDate).ofType(ColumnType.DATE).formatForType("dd.mm.yyyy")
                .column("Letzter Login", Employee::lastLogin).ofType(ColumnType.DATETIME)
                .formatForType("dd.mm.yyyy hh:mm")
                // Rohwert int (Sekunden seit Mitternacht) wird zur Uhrzeit (TIME) konvertiert.
                .column("Kommt", Employee::checkInSeconds).ofType(ColumnType.TIME).formatForType("hh:mm")
                .convertToColumnType((Integer s) -> LocalTime.ofSecondOfDay(s))
                // Formelspalte: Bonus = 10 % vom Gehalt (Spalte F); {row} = aktuelle Zeilennummer.
                .column("Bonus", e -> "F{row}*0.1").ofType(ColumnType.FORMULA).formatForType("#,##0.00 \"€\"")
                .sortBy("Abteilung", SortOrder.ASC)
                .sortBy("Gehalt", SortOrder.DESC)
                .sortChunkSize(100_000)
                .sumColumn("Gehalt")
                .summaryLabel("Name", "Summe")
                .summaryAsFormula(true) // Summenzeile als echte =SUMME(...)-Formel
                .data(employeeGenerator(rowCount));


        WorkbookBuilder.create()
        .sheet(mitarbeiter)
        .sheet(info)
        .sheet(mitarbeiter_1)
        .write(out);

        
        double seconds = (System.nanoTime() - start) / 1_000_000_000.0;
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long fileMb = Files.size(out) / (1024 * 1024);

        System.out.printf(
                "Fertig: %,d Zeilen -> %s (%d MB) in %.1fs, belegter Heap ~%d MB, max Heap %d MB%n",
                rowCount, out.toAbsolutePath(), fileMb, seconds, usedMb,
                runtime.maxMemory() / (1024 * 1024));
        System.out.println();
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
                long id = 1_000_000L + produced;
                String name = "Mitarbeiter-" + produced;
                String dept = departments[random.nextInt(departments.length)];
                int age = 20 + random.nextInt(45);
                double rating = Math.round(random.nextDouble() * 50) / 10.0; // 0.0 .. 5.0
                BigDecimal salary = BigDecimal.valueOf(30_000 + random.nextInt(90_000))
                        .add(BigDecimal.valueOf(random.nextInt(100), 2))
                        .setScale(2, RoundingMode.HALF_UP);
                boolean active = random.nextBoolean();
                LocalDate hire = LocalDate.of(2000, 1, 1).plusDays(random.nextInt(9000));
                LocalDateTime lastLogin = LocalDateTime.of(2026, 1, 1, 0, 0)
                        .plusMinutes(random.nextInt(525_600)); // irgendwann im Jahr 2026
                // Kommt-Zeit zwischen 06:00 und 09:59 als Sekunden seit Mitternacht.
                int checkInSeconds = (6 + random.nextInt(4)) * 3600 + random.nextInt(60) * 60;
                return new Employee(id, name, dept, age, rating, salary, active, hire, lastLogin,
                        checkInSeconds);
            }
        };
    }
}
