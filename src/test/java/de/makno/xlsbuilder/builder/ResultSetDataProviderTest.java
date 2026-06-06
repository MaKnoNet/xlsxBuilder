package de.makno.xlsbuilder.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.makno.xlsbuilder.builder.XlsxTestReader.Grid;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifiziert den JDBC-Adapter {@link DataProviders#ofResultSet} gegen eine In-Memory-H2-DB. */
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
                    .sheet(ExcelBuilder.<EmpRow>create()
                            .column("ID", EmpRow::id)
                            .ofType(ColumnType.INTEGER)
                            .column("Name", EmpRow::name)
                            .data(DataProviders.ofResultSet(rs, r -> new EmpRow(r.getInt("id"), r.getString("name")))))
                    .write(out);

            assertTrue(rs.isClosed(), "Adapter.close() muss das ResultSet schließen");
            st.close();
        }

        Grid g = XlsxTestReader.read(out);
        assertEquals(4, g.rowCount(), "Kopf + 3 Datenzeilen");
        assertEquals(1, g.number(1, 0));
        assertEquals("Alpha", g.string(1, 1));
        assertEquals("Beta", g.string(2, 1));
        assertEquals("Gamma", g.string(3, 1));
    }
}
