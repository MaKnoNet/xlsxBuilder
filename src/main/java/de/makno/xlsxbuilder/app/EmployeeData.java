package de.makno.xlsxbuilder.app;

import de.makno.xlsxbuilder.builder.ColumnType;
import de.makno.xlsxbuilder.builder.DataProvider;
import de.makno.xlsxbuilder.builder.SortOrder;
import de.makno.xlsxbuilder.builder.XlsxBuilder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * Shared helpers around {@link Employee}: a deterministic lazy generator, the unified sheet
 * configuration (columns/sorting/summary) and the mapping of a JDBC {@link ResultSet} row. Shared by
 * {@link XlsxBuilderDemo} (in-memory) and {@link DbBenchmark} (from H2).
 */
public final class EmployeeData {

    private static final String[] DEPARTMENTS = {"Vertrieb", "Technik", "Marketing", "Personal", "Finanzen", "Support"};

    private EmployeeData() {}

    /** Lazy generator: produces {@code count} records only on demand, never as a complete list. */
    public static DataProvider<Employee> generator(long count) {
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
                String dept = DEPARTMENTS[random.nextInt(DEPARTMENTS.length)];
                int age = 20 + random.nextInt(45);
                double rating = Math.round(random.nextDouble() * 50) / 10.0; // 0.0 .. 5.0
                BigDecimal salary = BigDecimal.valueOf(30_000 + random.nextInt(90_000))
                        .add(BigDecimal.valueOf(random.nextInt(100), 2))
                        .setScale(2, RoundingMode.HALF_UP);
                boolean active = random.nextBoolean();
                LocalDate hire = LocalDate.of(2000, 1, 1).plusDays(random.nextInt(9000));
                LocalDateTime lastLogin =
                        LocalDateTime.of(2026, 1, 1, 0, 0).plusMinutes(random.nextInt(525_600)); // sometime in 2026
                // check-in time between 06:00 and 09:59 as seconds since midnight.
                int checkInSeconds = (6 + random.nextInt(4)) * 3600 + random.nextInt(60) * 60;
                return new Employee(id, name, dept, age, rating, salary, active, hire, lastLogin, checkInSeconds);
            }
        };
    }

    /** Unified sheet configuration: one column per {@link ColumnType}, sorted, with a summary row. */
    public static XlsxBuilder<Employee> sheet(String sheetName, DataProvider<Employee> data) {
        return XlsxBuilder.<Employee>create()
                .sheetName(sheetName)
                .header("Mitarbeiterbericht", "Erstellt am " + LocalDate.now())
                .column("ID", Employee::id)
                .ofType(ColumnType.LONG)
                .column("Name", Employee::name) // STRING (default)
                .column("Abteilung", Employee::department) // STRING
                .column("Alter", Employee::age)
                .ofType(ColumnType.INTEGER)
                .column("Bewertung", Employee::rating)
                .ofType(ColumnType.DOUBLE)
                .formatForType("0.0")
                .column("Gehalt", Employee::salary)
                .ofType(ColumnType.DECIMAL)
                .formatForType("#,##0.00 \"€\"")
                .column("Aktiv", Employee::active)
                .ofType(ColumnType.BOOLEAN)
                .column("Eintritt", Employee::hireDate)
                .ofType(ColumnType.DATE)
                .formatForType("dd.mm.yyyy")
                .column("Letzter Login", Employee::lastLogin)
                .ofType(ColumnType.DATETIME)
                .formatForType("dd.mm.yyyy hh:mm")
                // raw value int (seconds since midnight) is converted to a time of day (TIME).
                .column("Kommt", Employee::checkInSeconds)
                .ofType(ColumnType.TIME)
                .formatForType("hh:mm")
                .convertToColumnType((Integer s) -> LocalTime.ofSecondOfDay(s))
                // formula column: bonus = 10% of salary (column F); {row} = current row number.
                .column("Bonus", e -> "F{row}*0.1")
                .ofType(ColumnType.FORMULA)
                .formatForType("#,##0.00 \"€\"")
                .sortBy("Abteilung", SortOrder.ASC)
                .sortBy("Gehalt", SortOrder.DESC)
                .sortChunkSize(100_000)
                .sumColumn("Gehalt")
                .summaryLabel("Name", "Summe")
                .summaryAsFormula(true) // summary row as a real =SUM(...) formula
                .data(data);
    }

    /** Maps the current row of a {@link ResultSet} (columns of the {@code employee} table) to {@link Employee}. */
    public static Employee map(ResultSet rs) throws SQLException {
        return new Employee(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("department"),
                rs.getInt("age"),
                rs.getDouble("rating"),
                rs.getBigDecimal("salary"),
                rs.getBoolean("active"),
                rs.getDate("hire_date").toLocalDate(),
                rs.getTimestamp("last_login").toLocalDateTime(),
                rs.getInt("check_in_seconds"));
    }
}
