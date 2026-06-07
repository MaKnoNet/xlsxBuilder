package de.makno.xlsxbuilder.app;

import de.makno.xlsxbuilder.builder.DataProvider;
import de.makno.xlsxbuilder.builder.DataProviders;
import de.makno.xlsxbuilder.builder.WorkbookBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Performance-Benchmark mit echter SQL-Datenquelle: befüllt eine eingebettete H2-Datenbank einmalig
 * mit Mitarbeiter-Testdaten und exportiert sie anschließend <em>streamend</em> (forward-only
 * {@link ResultSet} via {@link DataProviders#ofResultSet}) nach {@code .xlsx}. So werden DB-Streaming,
 * External Merge Sort und SXSSF gemeinsam unter Last (1 Mio. Zeilen) gemessen.
 *
 * <p>Aufruf: {@code DbBenchmark [zeilenanzahl] [ausgabedatei]} (Default: 1_000_000 /
 * build/employees-db.xlsx). Empfohlen mit begrenztem Heap, z. B. {@code -Xmx256m}, um Out-of-core
 * nachzuweisen.
 */
public final class DbBenchmark {

    private static final Path DB_DIR = Path.of("build", "benchdb");
    private static final String JDBC_URL = "jdbc:h2:./build/benchdb/employees";
    private static final int BATCH_SIZE = 10_000;

    private DbBenchmark() {}

    public static void main(String[] args) throws IOException, SQLException {
        long rowCount = args.length > 0 ? Long.parseLong(args[0]) : 1_000_000L;
        Path out = Path.of(args.length > 1 ? args[1] : "build/employees-db.xlsx");
        boolean parallel = args.length > 2 && "parallel".equalsIgnoreCase(args[2]);

        Files.createDirectories(DB_DIR);
        Runtime runtime = Runtime.getRuntime();

        try (Connection conn = DriverManager.getConnection(JDBC_URL, "sa", "")) {
            ensureSchema(conn);

            long existing = rowCount(conn);
            if (existing == rowCount) {
                System.out.printf("DB enthält bereits %,d Zeilen – Seeding übersprungen.%n", existing);
            } else {
                long seedStart = System.nanoTime();
                seed(conn, rowCount);
                System.out.printf(
                        "Seeding: %,d Zeilen in H2 in %.1fs.%n",
                        rowCount, (System.nanoTime() - seedStart) / 1_000_000_000.0);
            }

            long exportStart = System.nanoTime();
            export(conn, out, parallel);

            double seconds = (System.nanoTime() - exportStart) / 1_000_000_000.0;
            long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            long fileMb = Files.size(out) / (1024 * 1024);
            System.out.printf(
                    "Export%s: %,d Zeilen DB -> %s (%d MB) in %.1fs, belegter Heap ~%d MB, max Heap %d MB%n",
                    parallel ? " (parallel)" : "",
                    rowCount,
                    out.toAbsolutePath(),
                    fileMb,
                    seconds,
                    usedMb,
                    runtime.maxMemory() / (1024 * 1024));
        }
    }

    private static void ensureSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute(
                    """
                    CREATE TABLE IF NOT EXISTS employee (
                        id               BIGINT PRIMARY KEY,
                        name             VARCHAR(100),
                        department       VARCHAR(50),
                        age              INT,
                        rating           DOUBLE,
                        salary           DECIMAL(12,2),
                        active           BOOLEAN,
                        hire_date        DATE,
                        last_login       TIMESTAMP,
                        check_in_seconds INT
                    )""");
        }
    }

    private static long rowCount(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM employee")) {
            rs.next();
            return rs.getLong(1);
        }
    }

    /** Befüllt die Tabelle deterministisch mit genau {@code count} Zeilen (vorher geleert). */
    private static void seed(Connection conn, long count) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("TRUNCATE TABLE employee");
        }
        String sql = "INSERT INTO employee (id, name, department, age, rating, salary, active,"
                + " hire_date, last_login, check_in_seconds) VALUES (?,?,?,?,?,?,?,?,?,?)";
        boolean previousAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try (PreparedStatement ps = conn.prepareStatement(sql);
                DataProvider<Employee> gen = EmployeeData.generator(count)) {
            long n = 0;
            while (gen.hasNext()) {
                Employee e = gen.next();
                ps.setLong(1, e.id());
                ps.setString(2, e.name());
                ps.setString(3, e.department());
                ps.setInt(4, e.age());
                ps.setDouble(5, e.rating());
                ps.setBigDecimal(6, e.salary());
                ps.setBoolean(7, e.active());
                ps.setDate(8, Date.valueOf(e.hireDate()));
                ps.setTimestamp(9, Timestamp.valueOf(e.lastLogin()));
                ps.setInt(10, e.checkInSeconds());
                ps.addBatch();
                if (++n % BATCH_SIZE == 0) {
                    ps.executeBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } finally {
            conn.setAutoCommit(previousAutoCommit);
        }
    }

    /** Liest die Tabelle forward-only/streamend und exportiert sie über den Builder nach {@code out}. */
    private static void export(Connection conn, Path out, boolean parallel) throws SQLException, IOException {
        try (Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            st.setFetchSize(1_000);
            // Kein ORDER BY in SQL -> H2 liefert den Tabellen-Scan lazy; sortiert wird out-of-core
            // im Builder (External Merge Sort).
            ResultSet rs = st.executeQuery("SELECT * FROM employee");
            WorkbookBuilder.create()
                    .sheet(EmployeeData.sheet("Mitarbeiter", DataProviders.ofResultSet(rs, EmployeeData::map))
                            .parallel(parallel))
                    .write(out); // schließt das ResultSet via DataProvider.close()
        }
    }
}
