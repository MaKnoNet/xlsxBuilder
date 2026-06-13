package de.makno.xlsxbuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.makno.xlsxbuilder.XlsxTestReader.Grid;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies the JDBC adapter {@link DataProviders#ofResultSet} against an in-memory H2 DB. */
class ResultSetDataProviderTest {

    @TempDir
    Path tempDir;

    private record EmpRow(int id, String name) {}

    @Test
    void ofResultSetStreamsRowsAndClosesResultSet() throws Exception {
        Path out = tempDir.resolve("jdbc.xlsx");

        try (Connection conn = DriverManager.getConnection("jdbc:h2:mem:ofrs;DB_CLOSE_DELAY=-1", "sa", "")) {
            try (Statement setup = conn.createStatement()) {
                setup.execute("CREATE TABLE emp (id INT, name VARCHAR)");
                setup.execute("INSERT INTO emp VALUES (1,'Alpha'),(2,'Beta'),(3,'Gamma')");
            }

            Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = st.executeQuery("SELECT id, name FROM emp ORDER BY id");

            WorkbookBuilder.create()
                    .sheet(XlsxBuilder.<EmpRow>create()
                            .column("ID", EmpRow::id)
                            .ofType(ColumnType.INTEGER)
                            .column("Name", EmpRow::name)
                            .data(DataProviders.ofResultSet(rs, r -> new EmpRow(r.getInt("id"), r.getString("name")))))
                    .write(out);

            assertTrue(rs.isClosed(), "Adapter.close() must close the ResultSet");
            st.close();
        }

        Grid g = XlsxTestReader.read(out);
        assertEquals(4, g.rowCount(), "header + 3 data rows");
        assertEquals(1, g.number(1, 0));
        assertEquals("Alpha", g.string(1, 1));
        assertEquals("Beta", g.string(2, 1));
        assertEquals("Gamma", g.string(3, 1));
    }

    @Test
    void wrapsSqlExceptionFromResultSetNext() {
        ResultSet rs = proxyResultSet(name -> name.equals("next"), false);
        DataProvider<String> provider = DataProviders.ofResultSet(rs, r -> r.getString(1));

        DataAccessException ex = assertThrows(DataAccessException.class, provider::hasNext);
        assertInstanceOf(SQLException.class, ex.getCause(), "the original SQLException must be the cause");
    }

    @Test
    void wrapsSqlExceptionFromMapper() {
        ResultSet rs = proxyResultSet(name -> false, true); // next() -> true, nothing throws
        DataProvider<String> provider = DataProviders.ofResultSet(rs, r -> {
            throw new SQLException("mapping failed");
        });

        assertTrue(provider.hasNext());
        DataAccessException ex = assertThrows(DataAccessException.class, provider::next);
        assertInstanceOf(SQLException.class, ex.getCause());
    }

    @Test
    void wrapsSqlExceptionFromResultSetClose() {
        ResultSet rs = proxyResultSet(name -> name.equals("close"), false);
        DataProvider<String> provider = DataProviders.ofResultSet(rs, r -> r.getString(1));

        DataAccessException ex = assertThrows(DataAccessException.class, provider::close);
        assertInstanceOf(SQLException.class, ex.getCause());
    }

    /**
     * A minimal {@link ResultSet} proxy that throws a {@link SQLException} from the methods matched by
     * {@code throwFrom} and returns {@code nextResult} from {@code next()}; all other methods return a
     * type-appropriate default. Lets the SQLException-wrapping paths be exercised without a mock library.
     */
    private static ResultSet proxyResultSet(Predicate<String> throwFrom, boolean nextResult) {
        return (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(), new Class<?>[] {ResultSet.class}, (proxy, method, args) -> {
                    String name = method.getName();
                    if (throwFrom.test(name)) {
                        throw new SQLException("simulated failure in " + name + "()");
                    }
                    if (name.equals("next")) {
                        return nextResult;
                    }
                    Class<?> returnType = method.getReturnType();
                    if (returnType == boolean.class) {
                        return false;
                    }
                    if (returnType == int.class) {
                        return 0;
                    }
                    if (returnType == long.class) {
                        return 0L;
                    }
                    return null;
                });
    }
}
