package de.makno.xlsxbuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.makno.xlsxbuilder.XlsxTestReader.Grid;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.Property;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class XlsxBuilderTest {

    @TempDir
    Path tempDir;

    private record Person(String name, int age, boolean active) {}

    private record DeptRow(String dept, int salary) {}

    @Test
    void writesGroupedHeaderRowAboveColumnHeaders() throws Exception {
        Path out = tempDir.resolve("grouped.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .sheetName("People")
                        .column("Name", Person::name)
                        .column("Age", Person::age)
                        .ofType(ColumnType.INTEGER)
                        .column("Active", Person::active)
                        .ofType(ColumnType.BOOLEAN)
                        .columnGroups(List.of(new ColumnGroup("Stammdaten", 2), new ColumnGroup("Status", 1)))
                        .data(DataProviders.ofIterable(List.of(new Person("Alice", 30, true)))))
                .write(out);

        // Group row (0), then the column headers (1), then data (2).
        Grid g = XlsxTestReader.read(out);
        assertEquals("Stammdaten", g.string(0, 0));
        assertEquals("Status", g.string(0, 2));
        assertEquals(List.of("Name", "Age", "Active"), g.strings(1), "column headers moved down one row");
        assertEquals("Alice", g.string(2, 0));

        // The 2-column group is a merged region across columns 0..1 in row 0.
        try (Workbook wb = WorkbookFactory.create(Files.newInputStream(out))) {
            Sheet sheet = wb.getSheetAt(0);
            boolean merged =
                    sheet.getMergedRegions().stream().anyMatch(r -> r.equals(new CellRangeAddress(0, 0, 0, 1)));
            assertTrue(merged, "group 'Stammdaten' must be merged across columns 0..1");
        }
    }

    @Test
    void rejectsColumnGroupsNotCoveringAllColumns() {
        Path out = tempDir.resolve("badgroups.xlsx");
        WorkbookBuilder workbook = WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .column("Name", Person::name)
                        .column("Age", Person::age)
                        .ofType(ColumnType.INTEGER)
                        .columnGroups(List.of(new ColumnGroup("Only one", 1))) // 1 of 2 columns
                        .data(DataProviders.ofIterable(List.of(new Person("A", 1, true)))));

        assertThrows(IllegalArgumentException.class, () -> workbook.write(out));
    }

    @Test
    void writesHeaderAndColumns() throws Exception {
        List<Person> data = List.of(new Person("Alice", 30, true), new Person("Bob", 25, false));
        Path out = tempDir.resolve("basic.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .sheetName("People")
                        .column("Name", Person::name)
                        .column("Age", Person::age)
                        .ofType(ColumnType.INTEGER)
                        .column("Active", Person::active)
                        .ofType(ColumnType.BOOLEAN)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals("People", g.sheetName());
        assertEquals(3, g.rowCount(), "header + 2 data rows");
        assertEquals(List.of("Name", "Age", "Active"), g.strings(0));

        assertEquals("Alice", g.string(1, 0));
        assertEquals(30, g.number(1, 1));
        assertTrue(g.bool(1, 2));

        assertEquals("Bob", g.string(2, 0));
        assertEquals(25, g.number(2, 1));
        assertFalse(g.bool(2, 2));
    }

    @Test
    void producesReadableXlsx() throws Exception {
        Path out = tempDir.resolve("struct.xlsx");
        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .column("Name", Person::name)
                        .data(DataProviders.ofIterable(List.of(new Person("X", 1, true)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals("Sheet1", g.sheetName(), "default sheet name");
        assertEquals(2, g.rowCount());
        assertEquals("X", g.string(1, 0));
    }

    @Test
    void sortsDescendingByNumericColumn() throws Exception {
        List<Person> data = List.of(new Person("A", 30, true), new Person("B", 25, true), new Person("C", 40, true));
        Path out = tempDir.resolve("sortDesc.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .column("Name", Person::name)
                        .column("Age", Person::age)
                        .ofType(ColumnType.INTEGER)
                        .sortBy("Age", SortOrder.DESC)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        List<Long> ages = new ArrayList<>();
        for (int i = 1; i < g.rowCount(); i++) {
            ages.add(g.number(i, 1));
        }
        assertEquals(List.of(40L, 30L, 25L), ages);
    }

    @Test
    void appliesMultiLevelSort() throws Exception {
        List<DeptRow> data =
                List.of(new DeptRow("B", 100), new DeptRow("A", 50), new DeptRow("A", 80), new DeptRow("B", 90));
        Path out = tempDir.resolve("multiSort.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<DeptRow>create()
                        .column("Department", DeptRow::dept)
                        .column("Salary", DeptRow::salary)
                        .ofType(ColumnType.INTEGER)
                        .sortBy("Department", SortOrder.ASC)
                        .sortBy("Salary", SortOrder.DESC)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        List<String> ordered = new ArrayList<>();
        for (int i = 1; i < g.rowCount(); i++) {
            ordered.add(g.string(i, 0) + ":" + g.number(i, 1));
        }
        assertEquals(List.of("A:80", "A:50", "B:100", "B:90"), ordered);
    }

    @Test
    void externalMergeSortAcrossManyRuns() throws Exception {
        // 1000 mixed values, chunk size 100 => 10 runs + k-way merge.
        List<Integer> shuffled = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            shuffled.add(i);
        }
        Collections.shuffle(shuffled, new java.util.Random(7));
        Path out = tempDir.resolve("externalSort.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Integer>create()
                        .column("n", i -> i)
                        .ofType(ColumnType.INTEGER)
                        .sortBy("n", SortOrder.ASC)
                        .sortChunkSize(100)
                        .data(DataProviders.ofIterable(shuffled)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(1001, g.rowCount(), "header + 1000 data rows");
        long previous = Long.MIN_VALUE;
        for (int i = 1; i < g.rowCount(); i++) {
            long v = g.number(i, 0);
            assertTrue(v > previous, "values must be strictly ascending");
            previous = v;
        }
        assertEquals(999, previous);
    }

    @Test
    void externalMergeSortWithMultiplePasses() throws Exception {
        // 600 values at chunk size 2 => 300 runs. At fan-in 16 this forces multi-pass
        // pre-merging (300 -> 19 -> 2 runs) before the final k-way merge runs.
        List<Integer> shuffled = new ArrayList<>();
        for (int i = 0; i < 600; i++) {
            shuffled.add(i);
        }
        Collections.shuffle(shuffled, new java.util.Random(11));
        Path out = tempDir.resolve("multiPassSort.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Integer>create()
                        .column("n", i -> i)
                        .ofType(ColumnType.INTEGER)
                        .sortBy("n", SortOrder.ASC)
                        .sortChunkSize(2)
                        .data(DataProviders.ofIterable(shuffled)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(601, g.rowCount(), "header + 600 data rows");
        long previous = Long.MIN_VALUE;
        for (int i = 1; i < g.rowCount(); i++) {
            long v = g.number(i, 0);
            assertTrue(v > previous, "values must be strictly ascending across all merge passes");
            previous = v;
        }
        assertEquals(599, previous);
    }

    @Test
    void unsortedPreservesInputOrder() throws Exception {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            data.add(i);
        }
        Path out = tempDir.resolve("unsorted.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Integer>create()
                        .column("n", i -> i)
                        .ofType(ColumnType.INTEGER)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(501, g.rowCount());
        for (int i = 1; i < g.rowCount(); i++) {
            assertEquals(i - 1, g.number(i, 0));
        }
    }

    @Test
    void formatsDateAndDecimal() throws Exception {
        record Sale(LocalDate date, BigDecimal amount) {}
        LocalDate date = LocalDate.of(2026, 5, 30);
        Path out = tempDir.resolve("formats.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Sale>create()
                        .column("Datum", Sale::date)
                        .ofType(ColumnType.DATE)
                        .column("Betrag", Sale::amount)
                        .ofType(ColumnType.DECIMAL)
                        .data(DataProviders.ofIterable(List.of(new Sale(date, new BigDecimal("1234.56"))))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertTrue(g.isDateFormatted(1, 0), "date cell must have a date format");
        assertEquals(date, g.dateTime(1, 0).toLocalDate());
        assertEquals(1234.56, g.dbl(1, 1), 0.0001);
    }

    @Test
    void appliesCustomFormatsForDecimalDateAndTime() throws Exception {
        record R(BigDecimal betrag, LocalDate datum, LocalTime zeit) {}
        LocalDate date = LocalDate.of(2026, 5, 30);
        LocalTime time = LocalTime.of(9, 30, 15);
        Path out = tempDir.resolve("customFormats.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<R>create()
                        .column("Betrag", R::betrag)
                        .ofType(ColumnType.DECIMAL)
                        .formatForType("#,##0.00")
                        .column("Datum", R::datum)
                        .ofType(ColumnType.DATE)
                        .formatForType("dd.mm.yyyy")
                        .column("Zeit", R::zeit)
                        .ofType(ColumnType.TIME)
                        .formatForType("hh:mm:ss")
                        .data(DataProviders.ofIterable(List.of(new R(new BigDecimal("1234.5"), date, time)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);

        assertEquals("#,##0.00", g.format(1, 0), "DECIMAL format");
        assertEquals(1234.5, g.dbl(1, 0), 0.0001);

        assertEquals("dd.mm.yyyy", g.format(1, 1), "DATE format");
        assertEquals(date, g.dateTime(1, 1).toLocalDate());

        assertEquals("hh:mm:ss", g.format(1, 2), "TIME format");
        assertEquals(time, g.dateTime(1, 2).toLocalTime());
    }

    @Test
    void summaryRowCanUseSumFormula() throws Exception {
        record Item(String name, int wert) {}
        List<Item> data = List.of(new Item("A", 10), new Item("B", 30), new Item("C", 20));
        Path out = tempDir.resolve("summaryFormula.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .header("Report") // title row -> the data area is shifted
                        .column("Name", Item::name)
                        .column("Value", Item::wert)
                        .ofType(ColumnType.INTEGER)
                        .sumColumn("Value")
                        .summaryLabel("Name", "Total")
                        .summaryAsFormula(true)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // title(1) + header(2) + data(3-5) + sum(6)
        assertEquals(6, g.rowCount());
        assertEquals("Total", g.string(5, 0));
        // value is column B; data rows are Excel rows 3..5.
        assertEquals("SUM(B3:B5)", g.formula(5, 1));
    }

    @Test
    void writesFormulaColumnWithRowPlaceholder() throws Exception {
        record P(int a, int b) {}
        Path out = tempDir.resolve("formula.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<P>create()
                        .column("A", P::a)
                        .ofType(ColumnType.INTEGER)
                        .column("B", P::b)
                        .ofType(ColumnType.INTEGER)
                        .column("Total", p -> "A{row}+B{row}")
                        .ofType(ColumnType.FORMULA)
                        .data(DataProviders.ofIterable(List.of(new P(2, 3), new P(10, 20)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // header = row 1; data rows are Excel rows 2 and 3.
        assertEquals("A2+B2", g.formula(1, 2));
        assertEquals("A3+B3", g.formula(2, 2));
    }

    @Test
    void appendsSummaryRowWithSums() throws Exception {
        record Item(String name, int menge, BigDecimal betrag) {}
        List<Item> data = List.of(
                new Item("A", 2, new BigDecimal("10.50")),
                new Item("B", 3, new BigDecimal("5.25")),
                new Item("C", 1, new BigDecimal("4.25")));
        Path out = tempDir.resolve("summary.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .column("Name", Item::name)
                        .column("Menge", Item::menge)
                        .ofType(ColumnType.INTEGER)
                        .column("Betrag", Item::betrag)
                        .ofType(ColumnType.DECIMAL)
                        .sumColumn("Menge")
                        .sumColumn("Betrag")
                        .summaryLabel("Name", "Total")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(5, g.rowCount(), "header + 3 data + 1 summary row");
        assertEquals("Total", g.string(4, 0));
        assertEquals(6, g.number(4, 1));
        assertEquals(20.00, g.dbl(4, 2), 0.0001);
    }

    @Test
    void summaryRowSumsAcrossSortedAndSpilledData() throws Exception {
        // the sum must cover ALL rows, even when sorted/spilled externally.
        List<Integer> data = new ArrayList<>();
        long expectedSum = 0;
        for (int i = 1; i <= 1000; i++) {
            data.add(i);
            expectedSum += i;
        }
        Collections.shuffle(data, new java.util.Random(3));
        Path out = tempDir.resolve("summarySorted.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Integer>create()
                        .column("n", i -> (long) i)
                        .ofType(ColumnType.LONG)
                        .sortBy("n", SortOrder.ASC)
                        .sortChunkSize(100)
                        .sumColumn("n")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(1002, g.rowCount(), "header + 1000 data + summary row");
        assertEquals(expectedSum, g.number(1001, 0));
    }

    @Test
    void setsColumnWidthsSoFormattedValuesAreVisible() throws Exception {
        record R(LocalDate datum) {}
        Path out = tempDir.resolve("widths.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<R>create()
                        .column("Hire date", R::datum)
                        .ofType(ColumnType.DATE)
                        .formatForType("dd.mm.yyyy")
                        .data(DataProviders.ofIterable(List.of(new R(LocalDate.of(2026, 12, 31))))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // clearly wider than the POI default width (~2048) so that no "#####" appears.
        assertTrue(g.columnWidth(0) >= 3000, "date column must be wide enough");
    }

    @Test
    void widthsAccountForLongStringsAndSummarySum() throws Exception {
        record Item(String name, int wert) {}
        String longName = "A very long employee name XYZ-12345"; // 35 characters
        List<Item> data = List.of(new Item(longName, 2_000_000), new Item("Short", 3_000_000));
        Path out = tempDir.resolve("widths2.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .column("Name", Item::name)
                        .column("Value", Item::wert)
                        .ofType(ColumnType.INTEGER)
                        .formatForType("#,##0")
                        .sumColumn("Value")
                        .summaryLabel("Name", "Total")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // name column at least as wide as the longest name.
        assertTrue(g.columnWidth(0) >= longName.length() * 256, "name column must fit the longest name");
        // sum = 5,000,000 -> "5.000.000" (9 characters incl. thousands separators).
        assertTrue(g.columnWidth(1) >= 9 * 256, "value column must fit the sum");
    }

    @Test
    void addsTitleHeaderRowsMergedAcrossWidth() throws Exception {
        List<Person> data = List.of(new Person("Alice", 30, true));
        Path out = tempDir.resolve("header.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .header("Employee report", "As of May 2026")
                        .column("Name", Person::name)
                        .column("Age", Person::age)
                        .ofType(ColumnType.INTEGER)
                        .column("Active", Person::active)
                        .ofType(ColumnType.BOOLEAN)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // 2 title rows + column headers + 1 data row
        assertEquals(4, g.rowCount());
        assertEquals("Employee report", g.string(0, 0));
        assertTrue(g.bold(0, 0), "title is bold");
        assertEquals("As of May 2026", g.string(1, 0));
        assertEquals(List.of("Name", "Age", "Active"), g.strings(2));
        assertEquals("Alice", g.string(3, 0));
        assertEquals(30, g.number(3, 1));
        assertTrue(g.bool(3, 2));

        // title merged across the full width (3 columns -> A..C).
        assertEquals(List.of("A1:C1", "A2:C2"), g.mergeRefs());
    }

    @Test
    void combinesHeaderSortAndSummary() throws Exception {
        record Item(String name, int wert) {}
        List<Item> data = List.of(new Item("A", 10), new Item("B", 30), new Item("C", 20));
        Path out = tempDir.resolve("combined.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .header("Report")
                        .column("Name", Item::name)
                        .column("Value", Item::wert)
                        .ofType(ColumnType.INTEGER)
                        .sortBy("Value", SortOrder.DESC)
                        .sumColumn("Value")
                        .summaryLabel("Name", "Total")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // title + column headers + 3 data + summary row
        assertEquals(6, g.rowCount());
        assertEquals("Report", g.string(0, 0));
        assertEquals(List.of("Name", "Value"), g.strings(1));
        assertEquals("B", g.string(2, 0));
        assertEquals(30, g.number(2, 1));
        assertEquals("C", g.string(3, 0));
        assertEquals(20, g.number(3, 1));
        assertEquals("A", g.string(4, 0));
        assertEquals(10, g.number(4, 1));
        assertEquals("Total", g.string(5, 0));
        assertEquals(60, g.number(5, 1));
    }

    @Test
    void convertsRawValueToTargetColumnType() throws Exception {
        // raw value int (seconds since midnight) -> write as time of day (TIME).
        record Task(String name, int sekunden) {}
        Path out = tempDir.resolve("convert.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Task>create()
                        .column("Name", Task::name)
                        .column("Start", Task::sekunden)
                        .ofType(ColumnType.TIME)
                        .convertToColumnType((Integer s) -> LocalTime.ofSecondOfDay(s))
                        .data(DataProviders.ofIterable(List.of(new Task("A", 34215))))) // 09:30:15
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertTrue(g.isDateFormatted(1, 1), "converted cell is formatted as a time");
        assertEquals(LocalTime.of(9, 30, 15), g.dateTime(1, 1).toLocalTime());
    }

    @Test
    void writesMultipleSheets() throws Exception {
        record Emp(String name, int age) {}
        record Dept(String code) {}
        Path out = tempDir.resolve("multi.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Emp>create()
                        .sheetName("Employees")
                        .column("Name", Emp::name)
                        .column("Age", Emp::age)
                        .ofType(ColumnType.INTEGER)
                        .data(DataProviders.ofIterable(List.of(new Emp("Alice", 30), new Emp("Bob", 25)))))
                .sheet(XlsxBuilder.<Dept>create()
                        .sheetName("Departments")
                        .column("Code", Dept::code)
                        .data(DataProviders.ofIterable(List.of(new Dept("IT"), new Dept("HR")))))
                .write(out);

        assertEquals(List.of("Employees", "Departments"), XlsxTestReader.sheetNames(out));

        Grid s0 = XlsxTestReader.read(out, 0);
        assertEquals(List.of("Name", "Age"), s0.strings(0));
        assertEquals("Alice", s0.string(1, 0));
        assertEquals(30, s0.number(1, 1));

        Grid s1 = XlsxTestReader.read(out, 1);
        assertEquals(List.of("Code"), s1.strings(0));
        assertEquals("IT", s1.string(1, 0));
        assertEquals("HR", s1.string(2, 0));
    }

    @Test
    void deduplicatesSheetNames() throws Exception {
        record R(String v) {}
        Path out = tempDir.resolve("dupe.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<R>create()
                        .sheetName("Daten")
                        .column("V", R::v)
                        .data(DataProviders.ofIterable(List.of(new R("a")))))
                .sheet(XlsxBuilder.<R>create()
                        .sheetName("Daten")
                        .column("V", R::v)
                        .data(DataProviders.ofIterable(List.of(new R("b")))))
                .write(out);

        List<String> names = XlsxTestReader.sheetNames(out);
        assertEquals(2, names.size());
        assertEquals("Daten", names.get(0));
        assertNotEquals("Daten", names.get(1), "second sheet must get a unique name");
    }

    // ========== Group A – Exception / Validation ==========

    @Test
    void throwsIfNoColumnsConfigured() {
        assertThrows(IllegalStateException.class, () -> WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create().data(DataProviders.ofIterable(List.of())))
                .write(tempDir.resolve("noColumns.xlsx")));
    }

    @Test
    void throwsIfNoDataProviderSet() {
        assertThrows(IllegalStateException.class, () -> WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create().column("Name", Person::name))
                .write(tempDir.resolve("noProvider.xlsx")));
    }

    @Test
    void throwsIfSumColumnIsNotNumeric() {
        assertThrows(IllegalArgumentException.class, () -> WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .column("Name", Person::name)
                        .sumColumn("Name")
                        .data(DataProviders.ofIterable(List.of(new Person("A", 1, true)))))
                .write(tempDir.resolve("badSum.xlsx")));
    }

    @Test
    void throwsIfSortChunkSizeLessThanOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> XlsxBuilder.<Person>create().column("Name", Person::name).sortChunkSize(0));
    }

    @Test
    void throwsIfWorkbookHasNoSheets() {
        assertThrows(
                IllegalStateException.class, () -> WorkbookBuilder.create().write(tempDir.resolve("noSheets.xlsx")));
    }

    @Test
    void throwsIfSortKeyColumnUnknown() {
        assertThrows(IllegalArgumentException.class, () -> WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .column("Name", Person::name)
                        .sortBy("NichtVorhanden", SortOrder.ASC)
                        .data(DataProviders.ofIterable(List.of(new Person("A", 1, true)))))
                .write(tempDir.resolve("badSortKey.xlsx")));
    }

    // ========== Group B – Null handling in the comparator ==========

    @Test
    void sortsNullsLastAscending() throws Exception {
        record NullRow(String label) {}
        List<NullRow> data = List.of(new NullRow("B"), new NullRow(null), new NullRow("A"));
        Path out = tempDir.resolve("nullsAsc.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<NullRow>create()
                        .column("Label", NullRow::label)
                        .sortBy("Label", SortOrder.ASC)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // ASC nulls-last: "A", "B", null
        assertEquals("A", g.string(1, 0));
        assertEquals("B", g.string(2, 0));
        assertNull(g.string(3, 0), "null value ends up as an empty cell at the end");
    }

    @Test
    void sortsNullsFirstInDescending() throws Exception {
        // null is treated internally as the "greatest value" (nulls-last for ASC).
        // with DESC (sign flip) null therefore appears at the beginning.
        record NullRow(String label) {}
        List<NullRow> data = List.of(new NullRow("B"), new NullRow(null), new NullRow("A"));
        Path out = tempDir.resolve("nullsDesc.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<NullRow>create()
                        .column("Label", NullRow::label)
                        .sortBy("Label", SortOrder.DESC)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // DESC: null (= greatest value) comes first, then B, then A
        assertNull(g.string(1, 0), "null kommt bei DESC-Sortierung zuerst");
        assertEquals("B", g.string(2, 0));
        assertEquals("A", g.string(3, 0));
    }

    // ========== Group C – More types and XlsxWriter branches ==========

    @Test
    void writesDateTimeColumn() throws Exception {
        record Event(String name, LocalDateTime when) {}
        LocalDateTime dt = LocalDateTime.of(2026, 3, 15, 14, 30);
        Path out = tempDir.resolve("datetime.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Event>create()
                        .column("Name", Event::name)
                        .column("Zeitpunkt", Event::when)
                        .ofType(ColumnType.DATETIME)
                        .data(DataProviders.ofIterable(List.of(new Event("Test", dt)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertTrue(g.isDateFormatted(1, 1), "DATETIME cell must be formatted as a date");
        assertEquals(dt, g.dateTime(1, 1));
    }

    @Test
    void writesDoubleColumn() throws Exception {
        record Measurement(String label, double value) {}
        Path out = tempDir.resolve("doubleCol.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Measurement>create()
                        .column("Label", Measurement::label)
                        .column("Value", Measurement::value)
                        .ofType(ColumnType.DOUBLE)
                        .data(DataProviders.ofIterable(List.of(new Measurement("pi", 3.14159)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(3.14159, g.dbl(1, 1), 0.00001);
    }

    @Test
    void writesFormulaColumnWithoutRowPlaceholder() throws Exception {
        record R(int a, int b) {}
        Path out = tempDir.resolve("staticFormula.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<R>create()
                        .column("A", R::a)
                        .ofType(ColumnType.INTEGER)
                        .column("B", R::b)
                        .ofType(ColumnType.INTEGER)
                        // static formula without a {row} placeholder
                        .column("Total", r -> "A2+B2")
                        .ofType(ColumnType.FORMULA)
                        .data(DataProviders.ofIterable(List.of(new R(5, 7)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // the formula must not contain {row} -> unchanged text is set as the formula.
        assertEquals("A2+B2", g.formula(1, 2));
    }

    @Test
    void headerWithSingleColumnNoMerge() throws Exception {
        Path out = tempDir.resolve("singleColHeader.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .header("Nur eine Spalte")
                        .column("Name", Person::name)
                        .data(DataProviders.ofIterable(List.of(new Person("Alice", 30, true)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals("Nur eine Spalte", g.string(0, 0));
        // only 1 column -> no merge region
        assertTrue(g.mergeRefs().isEmpty(), "a single column must not create a merge");
    }

    @Test
    void writesEmptyDataSource() throws Exception {
        Path out = tempDir.resolve("empty.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .column("Name", Person::name)
                        .column("Age", Person::age)
                        .ofType(ColumnType.INTEGER)
                        .data(DataProviders.ofIterable(List.of())))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // only the header row, no data rows
        assertEquals(1, g.rowCount(), "empty source produces only the header row");
        assertEquals(List.of("Name", "Age"), g.strings(0));
    }

    @Test
    void summaryWithPrecomputedDecimal() throws Exception {
        record Item(String name, BigDecimal betrag) {}
        List<Item> data = List.of(new Item("X", new BigDecimal("10.50")), new Item("Y", new BigDecimal("5.25")));
        Path out = tempDir.resolve("sumDecimal.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .column("Name", Item::name)
                        .column("Betrag", Item::betrag)
                        .ofType(ColumnType.DECIMAL)
                        .formatForType("#,##0.00")
                        .sumColumn("Betrag")
                        .summaryLabel("Name", "Gesamt")
                        // summaryAsFormula(false) is the default -> pre-computed value
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(4, g.rowCount(), "header + 2 data + summary row");
        assertEquals("Gesamt", g.string(3, 0));
        assertEquals(15.75, g.dbl(3, 1), 0.001);
    }

    // ========== Group D – DataProviders & ExternalMergeSort ==========

    @Test
    void dataProviderOfStreamAdapter() throws Exception {
        Path out = tempDir.resolve("stream.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<String>create()
                        .column("Value", s -> s)
                        .data(DataProviders.ofStream(Stream.of("Alpha", "Beta", "Gamma"))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(4, g.rowCount(), "header + 3 data rows");
        assertEquals("Alpha", g.string(1, 0));
        assertEquals("Beta", g.string(2, 0));
        assertEquals("Gamma", g.string(3, 0));
    }

    @Test
    void dataProviderOfIteratorAdapter() throws Exception {
        Path out = tempDir.resolve("iterator.xlsx");
        var iterator = List.of("Eins", "Zwei").iterator();

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<String>create().column("Value", s -> s).data(DataProviders.ofIterator(iterator)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(3, g.rowCount(), "header + 2 data rows");
        assertEquals("Eins", g.string(1, 0));
        assertEquals("Zwei", g.string(2, 0));
    }

    @Test
    void externalSortWithEmptyInput() throws Exception {
        Path out = tempDir.resolve("emptySorted.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Integer>create()
                        .column("n", i -> i)
                        .ofType(ColumnType.INTEGER)
                        .sortBy("n", SortOrder.ASC)
                        .data(DataProviders.ofIterable(List.of())))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // sorting with an empty source -> no run, MergeIterator empty, only the header row
        assertEquals(1, g.rowCount(), "empty sorted source produces only the header row");
    }

    @Test
    void externalSortChunkSizeValidation() {
        // instantiate ExternalMergeSort directly (package-private, same package).
        var comparator = new RowComparator(
                List.of(new Column<>("n", ColumnType.INTEGER, i -> i)), List.of(new SortKey("n", SortOrder.ASC)));
        assertThrows(IllegalArgumentException.class, () -> new ExternalMergeSort(comparator, 0));
    }

    @Test
    void usesConfiguredSortTempDir() throws Exception {
        // a dedicated (not-yet-existing) sort temp directory -> is created and emptied
        // again after writing (the per-sort subdirectory disappears).
        Path customTmp = tempDir.resolve("sortwork");
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 300; i++) {
            data.add(i);
        }
        Collections.shuffle(data, new java.util.Random(5));
        Path out = tempDir.resolve("customTmp.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Integer>create()
                        .column("n", i -> i)
                        .ofType(ColumnType.INTEGER)
                        .sortBy("n", SortOrder.ASC)
                        .sortChunkSize(50) // forces spilling into the configured directory
                        .sortTempDir(customTmp)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(301, g.rowCount());
        for (int i = 1; i < g.rowCount(); i++) {
            assertEquals(i - 1, g.number(i, 0));
        }
        assertTrue(Files.isDirectory(customTmp), "Konfiguriertes Temp-Verzeichnis wurde angelegt");
        try (var entries = Files.list(customTmp)) {
            assertEquals(0, entries.count(), "sort subdirectory must be cleaned up");
        }
    }

    @Test
    void concurrentBuildsAreIsolated() throws Exception {
        // many builders in parallel: each thread writes its own file with its own instances.
        // verifies that there is no shared state (no cross-talk between threads).
        int tasks = 16;
        int rowsPerTask = 200;
        ExecutorService pool = Executors.newFixedThreadPool(8);
        List<Future<?>> futures = new ArrayList<>();
        try {
            for (int t = 0; t < tasks; t++) {
                final int id = t;
                futures.add(pool.submit(() -> {
                    List<Integer> data = new ArrayList<>();
                    for (int i = 0; i < rowsPerTask; i++) {
                        data.add(id * 1_000 + i); // per-task unique value range
                    }
                    Collections.shuffle(data, new java.util.Random(id));
                    Path out = tempDir.resolve("concurrent-" + id + ".xlsx");

                    WorkbookBuilder.create()
                            .sheet(XlsxBuilder.<Integer>create()
                                    .sheetName("S" + id)
                                    .column("n", i -> i)
                                    .ofType(ColumnType.INTEGER)
                                    .sortBy("n", SortOrder.ASC)
                                    .sortChunkSize(32) // forces spilling + merge per thread
                                    .data(DataProviders.ofIterable(data)))
                            .write(out);

                    Grid g = XlsxTestReader.read(out);
                    assertEquals(rowsPerTask + 1, g.rowCount(), "Task " + id);
                    for (int i = 0; i < rowsPerTask; i++) {
                        assertEquals((long) id * 1_000 + i, g.number(i + 1, 0), "Task " + id + " Zeile " + i);
                    }
                    return null;
                }));
            }
        } finally {
            pool.shutdown();
        }
        assertTrue(pool.awaitTermination(60, TimeUnit.SECONDS), "all tasks must finish");
        for (Future<?> f : futures) {
            f.get(); // propagates any AssertionErrors from the threads -> test fails
        }
    }

    @Test
    void streamProviderThrowsWhenExhausted() {
        // ofStream().next() without a further element -> NoSuchElementException (guard as in ofIterator).
        DataProvider<String> provider = DataProviders.ofStream(Stream.of("einziger"));
        assertEquals("einziger", provider.next());
        assertThrows(NoSuchElementException.class, provider::next);
    }

    @Test
    void rowCodecRoundTripsAllValueTypes() throws Exception {
        // covers all type tags of the RowCodec, incl. UTF-8 string and Java-serialization fallback.
        Object[] values = {
            null,
            "Käse äöü ß€",
            42, // Integer
            9_000_000_000L, // Long
            3.14159, // Double
            true, // Boolean
            new BigDecimal("12345.6789"), // BigDecimal
            LocalDate.of(2026, 6, 2),
            LocalDateTime.of(2026, 6, 2, 14, 30, 15),
            LocalTime.of(9, 30, 15),
            new java.util.Date(1_700_000_000_000L) // Fallback (Serializable, kein eigenes Tag)
        };
        Row original = new Row(values);

        var buffer = new java.io.ByteArrayOutputStream();
        try (var out = new java.io.DataOutputStream(buffer)) {
            RowCodec.writeRow(out, original);
        }
        Row restored;
        try (var in = new java.io.DataInputStream(new java.io.ByteArrayInputStream(buffer.toByteArray()))) {
            restored = RowCodec.readRow(in);
        }

        assertEquals(values.length, restored.size());
        for (int i = 0; i < values.length; i++) {
            assertEquals(values[i], restored.get(i), "Wert an Index " + i);
        }
        // the runtime type must be preserved exactly (otherwise the Integer vs. Long comparison would break).
        assertTrue(restored.get(2) instanceof Integer, "Integer stays Integer");
        assertTrue(restored.get(3) instanceof Long, "Long stays Long");
    }

    @Test
    void emitsPerformanceLogsOnSortedBuild() throws Exception {
        // attaches an in-memory appender to the builder logger and checks that a sorted run
        // produces the performance log lines (sort, sheet, workbook) at DEBUG.
        List<String> messages = java.util.Collections.synchronizedList(new ArrayList<>());
        AbstractAppender appender = new AbstractAppender("perfCapture", null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                messages.add(event.getMessage().getFormattedMessage());
            }
        };
        appender.start();
        String loggerName = "de.makno.xlsxbuilder";
        org.apache.logging.log4j.core.Logger logger =
                (org.apache.logging.log4j.core.Logger) LogManager.getLogger(loggerName);
        Level previous = logger.getLevel();
        logger.addAppender(appender);
        Configurator.setLevel(loggerName, Level.DEBUG);
        try {
            List<Integer> data = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                data.add(i);
            }
            Collections.shuffle(data, new java.util.Random(1));
            Path out = tempDir.resolve("perfLog.xlsx");
            WorkbookBuilder.create()
                    .sheet(XlsxBuilder.<Integer>create()
                            .sheetName("L")
                            .column("n", i -> i)
                            .ofType(ColumnType.INTEGER)
                            .sortBy("n", SortOrder.ASC)
                            .sortChunkSize(10) // forces spilling -> ExternalMergeSort log
                            .data(DataProviders.ofIterable(data)))
                    .write(out);
        } finally {
            logger.removeAppender(appender);
            appender.stop();
            Configurator.setLevel(loggerName, previous);
        }

        assertTrue(
                messages.stream().anyMatch(m -> m.contains("External Merge Sort")),
                "sort performance log missing: " + messages);
        assertTrue(
                messages.stream().anyMatch(m -> m.contains("Sheet '")), "sheet performance log missing: " + messages);
        assertTrue(
                messages.stream().anyMatch(m -> m.contains("Workbook:")),
                "workbook performance log missing: " + messages);
    }

    // ========== Filter ==========

    @Test
    void filterSkipsNonMatchingObjects() throws Exception {
        List<Person> data = List.of(
                new Person("A", 30, true),
                new Person("B", 25, false),
                new Person("C", 40, true),
                new Person("D", 20, false),
                new Person("E", 50, true));
        Path out = tempDir.resolve("filter.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .column("Name", Person::name)
                        .column("Age", Person::age)
                        .ofType(ColumnType.INTEGER)
                        .filter(Person::active) // only active employees
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(4, g.rowCount(), "header + 3 active records");
        assertEquals("A", g.string(1, 0));
        assertEquals("C", g.string(2, 0));
        assertEquals("E", g.string(3, 0));
    }

    @Test
    void filterCombinesWithSortAndSummary() throws Exception {
        record Item(String name, int wert) {}
        List<Item> data =
                List.of(new Item("A", 5), new Item("B", 20), new Item("C", 15), new Item("D", 8), new Item("E", 30));
        Path out = tempDir.resolve("filterSortSum.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .column("Name", Item::name)
                        .column("Value", Item::wert)
                        .ofType(ColumnType.INTEGER)
                        .filter(i -> i.wert() > 10) // keeps B(20), C(15), E(30)
                        .sortBy("Value", SortOrder.DESC)
                        .sumColumn("Value")
                        .summaryLabel("Name", "Total")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // header + 3 filtered data rows + summary row
        assertEquals(5, g.rowCount());
        assertEquals(30, g.number(1, 1));
        assertEquals(20, g.number(2, 1));
        assertEquals(15, g.number(3, 1));
        assertEquals("Total", g.string(4, 0));
        assertEquals(65, g.number(4, 1), "sum only over the filtered rows");
    }

    // ========== Null-value handler ==========

    @Test
    void nullTextPerColumnAndDefault() throws Exception {
        record R(String a, String b, Integer c) {}
        Path out = tempDir.resolve("nullText.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<R>create()
                        .defaultNullText("-")
                        .column("A", R::a)
                        .column("B", R::b)
                        .nullText("n/a") // column override
                        .column("C", R::c)
                        .ofType(ColumnType.INTEGER)
                        .data(DataProviders.ofIterable(List.of(new R(null, null, null)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals("-", g.string(1, 0), "column A: sheet-wide default");
        assertEquals("n/a", g.string(1, 1), "column B: column override");
        assertEquals("-", g.string(1, 2), "column C (INTEGER): default as text");
    }

    @Test
    void noNullTextLeavesCellEmpty() throws Exception {
        record R(String a) {}
        Path out = tempDir.resolve("noNullText.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<R>create()
                        .column("A", R::a)
                        .data(DataProviders.ofIterable(java.util.Arrays.asList(new R(null)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(2, g.rowCount(), "header + 1 (empty) data row");
        assertNull(g.string(1, 0), "without null text the cell stays empty");
    }

    @Test
    void nullWritesExplicitBlankCell() throws Exception {
        // without a placeholder an explicit empty cell (Excel cell type BLANK/"Empty") is created -
        // not simply omitted. So the cell exists and has type BLANK.
        record R(String a, Integer b) {}
        Path out = tempDir.resolve("blankCell.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<R>create()
                        .column("A", R::a)
                        .column("B", R::b)
                        .ofType(ColumnType.INTEGER)
                        .data(DataProviders.ofIterable(java.util.Arrays.asList(new R(null, null)))))
                .write(out);

        try (Workbook wb = WorkbookFactory.create(Files.newInputStream(out))) {
            var dataRow = wb.getSheetAt(0).getRow(1); // first data row (after the header)
            assertNotNull(dataRow.getCell(0), "cell A must exist as Empty");
            assertEquals(CellType.BLANK, dataRow.getCell(0).getCellType());
            assertNotNull(dataRow.getCell(1), "cell B must exist as Empty");
            assertEquals(CellType.BLANK, dataRow.getCell(1).getCellType());
        }
    }

    // ========== Footer / placeholders ==========

    @Test
    void writesFooterRowsMergedAfterSummary() throws Exception {
        record Item(String name, int wert) {}
        List<Item> data = List.of(new Item("A", 10), new Item("B", 20));
        Path out = tempDir.resolve("footer.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .column("Name", Item::name)
                        .column("Value", Item::wert)
                        .ofType(ColumnType.INTEGER)
                        .sumColumn("Value")
                        .summaryLabel("Name", "Total")
                        .footer("End of report")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // header(0) + 2 data(1,2) + sum(3) + footer(4)
        assertEquals(5, g.rowCount());
        assertEquals("End of report", g.string(4, 0));
        assertTrue(g.mergeRefs().contains("A5:B5"), "footer merged across the width: " + g.mergeRefs());
    }

    @Test
    void resolvesHeaderAndFooterPlaceholders() throws Exception {
        record Item(String name, int wert) {}
        List<Item> data = List.of(new Item("A", 10), new Item("B", 30));
        Path out = tempDir.resolve("placeholders.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .header("Report {company}", "As of {date}")
                        .column("Name", Item::name)
                        .column("Value", Item::wert)
                        .ofType(ColumnType.INTEGER)
                        .sumColumn("Value")
                        .summaryLabel("Name", "Total")
                        .footer("Rows: {rowCount}, Total Value: {sum:Value}")
                        .placeholder("company", "ACME")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // title(0,1) + header(2) + data(3,4) + sum(5) + footer(6)
        assertEquals("Report ACME", g.string(0, 0), "custom placeholder");
        assertEquals("As of " + java.time.LocalDate.now(), g.string(1, 0), "eingebautes {date}");
        assertEquals("Rows: 2, Total Value: 40", g.string(6, 0), "dynamic footer placeholders");
    }

    // ========== Pipeline parallelism ==========

    @Test
    void parallelProducesSameOutputAsSequential() throws Exception {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            data.add(String.format("N%03d", (i * 137) % 500)); // permutation -> 500 unique values
        }
        Path seq = tempDir.resolve("seq.xlsx");
        Path par = tempDir.resolve("par.xlsx");
        writeSortedStrings(seq, data, false);
        writeSortedStrings(par, data, true);

        Grid gs = XlsxTestReader.read(seq);
        Grid gp = XlsxTestReader.read(par);
        assertEquals(gs.rowCount(), gp.rowCount(), "same row count");
        for (int r = 0; r < gs.rowCount(); r++) {
            assertEquals(gs.string(r, 0), gp.string(r, 0), "row " + r + " identical");
        }
    }

    private void writeSortedStrings(Path out, List<String> data, boolean parallel) throws Exception {
        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<String>create()
                        .column("Value", s -> s)
                        .sortBy("Value", SortOrder.ASC)
                        .sortChunkSize(32) // forces spilling -> sort + prefetch run in parallel
                        .parallel(parallel)
                        .data(DataProviders.ofIterable(data)))
                .write(out);
    }

    @Test
    void parallelPropagatesSourceErrors() {
        DataProvider<String> failing = new DataProvider<>() {
            private int n = 0;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public String next() {
                if (n++ > 5) {
                    throw new IllegalStateException("boom");
                }
                return "x";
            }
        };
        assertThrows(IllegalStateException.class, () -> WorkbookBuilder.create()
                .sheet(XlsxBuilder.<String>create()
                        .column("V", s -> s)
                        .parallel(true)
                        .data(failing))
                .write(tempDir.resolve("fail.xlsx")));
    }

    @Test
    void comparatorRejectsIncompatibleValueTypes() {
        // two rows with incompatible value types in the sort column -> meaningful exception
        // instead of a raw ClassCastException.
        var comparator = new RowComparator(
                List.of(new Column<>("v", ColumnType.STRING, x -> x)), List.of(new SortKey("v", SortOrder.ASC)));
        Row textRow = new Row(new Object[] {"abc"});
        Row numberRow = new Row(new Object[] {123});
        assertThrows(IllegalArgumentException.class, () -> comparator.compare(textRow, numberRow));
    }

    // ========== Column-headers toggle ==========

    @Test
    void writesWithoutColumnHeaders() throws Exception {
        // columnHeaders(false) -> the first row is directly a data row, no header.
        List<Person> data = List.of(new Person("Alice", 30, true), new Person("Bob", 25, false));
        Path out = tempDir.resolve("noHeader.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .column("Name", Person::name)
                        .column("Age", Person::age)
                        .ofType(ColumnType.INTEGER)
                        .columnHeaders(false)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(2, g.rowCount(), "only 2 data rows, no header row");
        assertEquals("Alice", g.string(0, 0));
        assertEquals(30, g.number(0, 1));
        assertEquals("Bob", g.string(1, 0));
    }

    @Test
    void summaryFormulaWithoutColumnHeaders() throws Exception {
        // without a header the data starts in Excel row 1 -> formula must be SUM(B1:B2).
        record Item(String name, int wert) {}
        List<Item> data = List.of(new Item("A", 10), new Item("B", 20));
        Path out = tempDir.resolve("noHeaderSum.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .column("Name", Item::name)
                        .column("Value", Item::wert)
                        .ofType(ColumnType.INTEGER)
                        .sumColumn("Value")
                        .summaryAsFormula(true)
                        .columnHeaders(false)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // without a header: data in Excel rows 1-2, summary row in row 3
        assertEquals(3, g.rowCount());
        assertEquals("SUM(B1:B2)", g.formula(2, 1));
    }

    // ========== Single-use guard ==========

    @Test
    void rejectsReuseAfterWrite() throws Exception {
        // the same XlsxBuilder instance must not be written twice (forward-only source).
        XlsxBuilder<Person> sheet = XlsxBuilder.<Person>create()
                .column("Name", Person::name)
                .data(DataProviders.ofIterable(List.of(new Person("Alice", 30, true))));

        WorkbookBuilder.create().sheet(sheet).write(tempDir.resolve("once.xlsx"));

        assertThrows(
                IllegalStateException.class,
                () -> WorkbookBuilder.create().sheet(sheet).write(tempDir.resolve("again.xlsx")),
                "writing the same instance a second time must fail");
    }

    @Test
    void rejectsWorkbookReuse() throws Exception {
        WorkbookBuilder wb = WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Person>create()
                        .column("Name", Person::name)
                        .data(DataProviders.ofIterable(List.of(new Person("Alice", 30, true)))));

        wb.write(tempDir.resolve("wb-once.xlsx"));

        assertThrows(
                IllegalStateException.class,
                () -> wb.write(tempDir.resolve("wb-again.xlsx")),
                "a second write(...) call on the same WorkbookBuilder must fail");
    }

    // ========== Lazy/computed placeholders (placeholderResolver) ==========

    @Test
    void resolvesLazyPlaceholderInHeader() throws Exception {
        record Item(String name, int wert) {}
        List<Item> data = List.of(new Item("A", 10));
        Path out = tempDir.resolve("lazyHeader.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .header("Build {version}, User {user}")
                        .column("Name", Item::name)
                        .column("Value", Item::wert)
                        .ofType(ColumnType.INTEGER)
                        .placeholderResolver(key -> "version".equals(key) ? "1.2.3" : null)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals("Build 1.2.3, User {user}", g.string(0, 0), "resolver resolves {version}; unknown {user} stays");
    }

    @Test
    void staticPlaceholderWinsOverResolver() throws Exception {
        record Item(String name, int wert) {}
        List<Item> data = List.of(new Item("A", 10));
        Path out = tempDir.resolve("lazyPrecedence.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .header("Env {env}")
                        .column("Name", Item::name)
                        .column("Value", Item::wert)
                        .ofType(ColumnType.INTEGER)
                        .placeholder("env", "PROD")
                        .placeholderResolver(key -> "OVERRIDDEN")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals("Env PROD", g.string(0, 0), "static map takes precedence over the resolver");
    }

    // ========== Temp directory for sort runs ==========

    private record TempItem(String name, int wert) {}

    private static XlsxBuilder<TempItem> sortedTempSheet() {
        return XlsxBuilder.<TempItem>create()
                .column("Name", TempItem::name)
                .column("Value", TempItem::wert)
                .ofType(ColumnType.INTEGER)
                .sortBy("Name", SortOrder.ASC)
                .data(DataProviders.ofIterable(List.of(new TempItem("B", 1), new TempItem("A", 2))));
    }

    @Test
    void workbookTempDirIsUsedForSortRuns() throws Exception {
        // The (not-yet-existing) workbook temp dir is created as the base for the External Merge Sort.
        Path custom = tempDir.resolve("wb-temp");
        Path out = tempDir.resolve("wbTemp.xlsx");

        WorkbookBuilder.create().tempDir(custom).sheet(sortedTempSheet()).write(out);

        assertTrue(Files.isDirectory(custom), "workbook tempDir is used as the base for the sort runs");
    }

    @Test
    void perSheetSortTempDirOverridesWorkbookDefault() throws Exception {
        // The sheet's own sortTempDir wins; the workbook default is not used for that sheet.
        Path wbDefault = tempDir.resolve("wb-default");
        Path sheetDir = tempDir.resolve("sheet-override");
        Path out = tempDir.resolve("override.xlsx");

        WorkbookBuilder.create()
                .tempDir(wbDefault)
                .sheet(sortedTempSheet().sortTempDir(sheetDir))
                .write(out);

        assertTrue(Files.isDirectory(sheetDir), "per-sheet sortTempDir is used");
        assertFalse(Files.exists(wbDefault), "the sheet's own sortTempDir overrides the workbook default");
    }

    // ========== Parallel pipeline: exception hardening ==========

    @Test
    void prefetchLogsWarningWhenProducerDoesNotStop() {
        // A source whose next() blocks uninterruptibly (on a lock the test holds) makes close()'s join
        // time out, so the producer is still alive -> a WARN is logged instead of failing silently.
        List<String> warns = java.util.Collections.synchronizedList(new ArrayList<>());
        AbstractAppender appender = new AbstractAppender("warnCapture", null, null, true, Property.EMPTY_ARRAY) {
            @Override
            public void append(LogEvent event) {
                warns.add(event.getMessage().getFormattedMessage());
            }
        };
        appender.start();
        String loggerName = "de.makno.xlsxbuilder";
        org.apache.logging.log4j.core.Logger logger =
                (org.apache.logging.log4j.core.Logger) LogManager.getLogger(loggerName);
        Level previous = logger.getLevel();
        logger.addAppender(appender);
        Configurator.setLevel(loggerName, Level.WARN);

        java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
        lock.lock();
        try {
            java.util.Iterator<Row> blocked = new java.util.Iterator<>() {
                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Row next() {
                    lock.lock(); // uninterruptible block until the test releases the lock
                    lock.unlock();
                    return new Row(new Object[] {"x"});
                }
            };
            // Producer cannot finish next() while the test holds the lock -> it stays alive.
            PrefetchingRowIterator it = new PrefetchingRowIterator(blocked, 100); // short join timeout
            it.close(); // join(100) times out, producer still alive -> WARN
            assertTrue(
                    warns.stream().anyMatch(m -> m.contains("Prefetch producer")),
                    "warning for a non-stopping producer is missing: " + warns);
        } finally {
            lock.unlock();
            logger.removeAppender(appender);
            appender.stop();
            Configurator.setLevel(loggerName, previous);
        }
    }

    // ========== Atomic write(Path) ==========

    /** Data source whose read fails – simulates e.g. a dropped DB connection mid-export. */
    private static DataProvider<String> failingProvider() {
        return new DataProvider<>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public String next() {
                throw new IllegalStateException("source broke mid-read");
            }
        };
    }

    @Test
    void failedWriteLeavesExistingTargetIntact() throws Exception {
        Path out = tempDir.resolve("existing.xlsx");
        Files.writeString(out, "ORIGINAL CONTENT");

        assertThrows(IllegalStateException.class, () -> WorkbookBuilder.create()
                .sheet(XlsxBuilder.<String>create().column("V", s -> s).data(failingProvider()))
                .write(out));

        assertEquals("ORIGINAL CONTENT", Files.readString(out), "a failed write must not touch the target file");
        assertNoPartFiles();
    }

    @Test
    void failedWriteCreatesNoTargetFile() throws Exception {
        Path out = tempDir.resolve("never-written.xlsx");

        assertThrows(IllegalStateException.class, () -> WorkbookBuilder.create()
                .sheet(XlsxBuilder.<String>create().column("V", s -> s).data(failingProvider()))
                .write(out));

        assertFalse(Files.exists(out), "no partial file may appear at the target path");
        assertNoPartFiles();
    }

    private void assertNoPartFiles() throws IOException {
        try (Stream<Path> entries = Files.list(tempDir)) {
            List<String> parts = entries.map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".part"))
                    .toList();
            assertTrue(parts.isEmpty(), "temp files must be cleaned up: " + parts);
        }
    }

    // ========== Robustness: argument checks, overflow, serialization ==========

    @Test
    void rejectsNullArgumentsInAdaptersAndPlaceholders() {
        assertThrows(NullPointerException.class, () -> DataProviders.ofIterable(null));
        assertThrows(NullPointerException.class, () -> DataProviders.ofIterator(null));
        assertThrows(NullPointerException.class, () -> DataProviders.ofStream(null));
        assertThrows(
                NullPointerException.class, () -> XlsxBuilder.<Person>create().placeholders(null));
    }

    @Test
    void summaryOverflowFailsExplicitly() {
        // two Long.MAX_VALUE values: the BigDecimal accumulator holds the exact sum, but it no longer
        // fits into the LONG cell -> longValueExact must fail honestly instead of truncating silently.
        record Big(long v) {}
        List<Big> data = List.of(new Big(Long.MAX_VALUE), new Big(Long.MAX_VALUE));

        assertThrows(ArithmeticException.class, () -> WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Big>create()
                        .column("V", Big::v)
                        .ofType(ColumnType.LONG)
                        .sumColumn("V")
                        .data(DataProviders.ofIterable(data)))
                .write(tempDir.resolve("overflow.xlsx")));
    }

    @Test
    void sortingNonSerializableValueFailsWithHelpfulMessage() {
        // Comparable (sortable) but not Serializable: spilling the sorted run must fail with a message
        // naming the value type and the Serializable requirement - not with a bare
        // NotSerializableException from deep inside the codec.
        final class Opaque implements Comparable<Opaque> {
            private final int v;

            Opaque(int v) {
                this.v = v;
            }

            @Override
            public int compareTo(Opaque o) {
                return Integer.compare(v, o.v);
            }
        }
        List<Opaque> data = List.of(new Opaque(2), new Opaque(1));

        IOException e = assertThrows(IOException.class, () -> WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Opaque>create()
                        .column("V", o -> o)
                        .sortBy("V", SortOrder.ASC)
                        .data(DataProviders.ofIterable(data)))
                .write(tempDir.resolve("notSerializable.xlsx")));
        assertTrue(e.getMessage().contains("Opaque"), "message must name the value type: " + e.getMessage());
        assertTrue(e.getMessage().contains("Serializable"), "message must explain the requirement: " + e.getMessage());
    }

    @Test
    void validationFailureDoesNotMarkBuilderConsumed() {
        // A pure configuration error (groups do not cover all columns) must not flip the sheet into the
        // "already written" state: a retry reports the actual configuration error again.
        XlsxBuilder<Person> sheet = XlsxBuilder.<Person>create()
                .column("Name", Person::name)
                .column("Age", Person::age)
                .ofType(ColumnType.INTEGER)
                .columnGroups(List.of(new ColumnGroup("Only one", 1))) // 1 of 2 columns
                .data(DataProviders.ofIterable(List.of(new Person("A", 1, true))));

        assertThrows(
                IllegalArgumentException.class,
                () -> WorkbookBuilder.create().sheet(sheet).write(tempDir.resolve("invalid1.xlsx")));
        assertThrows(
                IllegalArgumentException.class,
                () -> WorkbookBuilder.create().sheet(sheet).write(tempDir.resolve("invalid2.xlsx")),
                "retry must report the configuration error again, not 'already written'");
    }

    // ========== Excel row limit / sheet split ==========

    @Test
    void rowLimitThrowsByDefault() {
        // seam limit 10 with a header row -> at most 9 data rows fit; 15 rows exceed that.
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            data.add(i);
        }

        RowLimitExceededException e = assertThrows(RowLimitExceededException.class, () -> WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Integer>create()
                        .sheetName("Big")
                        .column("n", i -> i)
                        .ofType(ColumnType.INTEGER)
                        .maxRowsPerSheet(10)
                        .data(DataProviders.ofIterable(data)))
                .write(tempDir.resolve("limit.xlsx")));
        assertTrue(e.getMessage().contains("Big"), "message must name the sheet: " + e.getMessage());
        assertTrue(e.getMessage().contains("splitOnRowLimit"), "message must point to the switch: " + e.getMessage());
    }

    @Test
    void rowLimitSplitsIntoMultipleSheets() throws Exception {
        // seam limit 10 with title + group + column headers (3 prelude rows) -> 7 data rows per part
        // sheet; 18 values -> 3 part sheets (7 + 7 + 4) with the prelude repeated on each.
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 18; i++) {
            data.add(i);
        }
        Path out = tempDir.resolve("split.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Integer>create()
                        .sheetName("S")
                        .header("T")
                        .columnGroups(List.of(new ColumnGroup("G", 1)))
                        .column("n", i -> i)
                        .ofType(ColumnType.INTEGER)
                        .maxRowsPerSheet(10)
                        .splitOnRowLimit(true)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        assertEquals(List.of("S", "S (2)", "S (3)"), XlsxTestReader.sheetNames(out));
        assertEquals(10, XlsxTestReader.read(out, 0).rowCount(), "first part sheet is filled to the limit");
        List<Long> values = new ArrayList<>();
        for (int s = 0; s < 3; s++) {
            Grid g = XlsxTestReader.read(out, s);
            assertEquals("T", g.string(0, 0), "title repeated on part sheet " + s);
            assertEquals("G", g.string(1, 0), "group header repeated on part sheet " + s);
            assertEquals(List.of("n"), g.strings(2), "column headers repeated on part sheet " + s);
            assertTrue(g.columnWidth(0) >= 3000, "column width applied on part sheet " + s);
            for (int r = 3; r < g.rowCount(); r++) {
                values.add(g.number(r, 0));
            }
        }
        List<Long> expected = new ArrayList<>();
        for (long i = 0; i < 18; i++) {
            expected.add(i);
        }
        assertEquals(expected, values, "data must continue seamlessly across the part sheets");
    }

    @Test
    void splitWritesSummaryAndFooterOnlyOnLastSheet() throws Exception {
        // seam limit 8: 1 header row + 2 reserved trailer rows (summary + footer) -> 5 data rows per
        // part sheet; 12 values -> 3 part sheets (5 + 5 + 2); the totals must cover ALL rows.
        record Item(String name, int wert) {}
        List<Item> data = new ArrayList<>();
        int expectedSum = 0;
        for (int i = 1; i <= 12; i++) {
            data.add(new Item("I" + i, i));
            expectedSum += i; // 78
        }
        Path out = tempDir.resolve("splitSummary.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .sheetName("Part")
                        .column("Name", Item::name)
                        .column("Value", Item::wert)
                        .ofType(ColumnType.INTEGER)
                        .sumColumn("Value")
                        .summaryLabel("Name", "Total")
                        .footer("Rows: {rowCount}, Sum: {sum:Value}")
                        .maxRowsPerSheet(8)
                        .splitOnRowLimit(true)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        assertEquals(3, XlsxTestReader.sheetNames(out).size());
        Grid first = XlsxTestReader.read(out, 0);
        assertEquals(6, first.rowCount(), "first part sheet: header + 5 data rows, no summary/footer");
        Grid last = XlsxTestReader.read(out, 2);
        // header + 2 data rows + summary + footer
        assertEquals(5, last.rowCount());
        assertEquals("Total", last.string(3, 0));
        assertEquals(expectedSum, last.number(3, 1), "summary must cover all part sheets");
        assertEquals("Rows: 12, Sum: 78", last.string(4, 0), "footer totals across all part sheets");
    }

    @Test
    void splitSummaryFormulaSpansSheets() throws Exception {
        // seam limit 7: 1 header row + 1 reserved summary row -> 5 data rows per part sheet;
        // 8 values -> 2 part sheets (5 + 3). The formula must reference both sheets' data ranges.
        record Item(String name, int wert) {}
        List<Item> data = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            data.add(new Item("I" + i, i)); // sum 36
        }
        Path out = tempDir.resolve("splitFormula.xlsx");

        WorkbookBuilder.create()
                .sheet(XlsxBuilder.<Item>create()
                        .sheetName("Part")
                        .column("Name", Item::name)
                        .column("Value", Item::wert)
                        .ofType(ColumnType.INTEGER)
                        .sumColumn("Value")
                        .summaryAsFormula(true)
                        .maxRowsPerSheet(7)
                        .splitOnRowLimit(true)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid last = XlsxTestReader.read(out, 1);
        // data on "Part" = Excel rows 2..6, on "Part (2)" = Excel rows 2..4; summary at row index 4.
        assertEquals("SUM(Part!B2:B6,'Part (2)'!B2:B4)", last.formula(4, 1));

        // the cross-sheet formula must actually evaluate to the total across both sheets.
        try (Workbook wb = WorkbookFactory.create(Files.newInputStream(out))) {
            var evaluator = wb.getCreationHelper().createFormulaEvaluator();
            var cell = wb.getSheetAt(1).getRow(4).getCell(1);
            assertEquals(36.0, evaluator.evaluate(cell).getNumberValue(), 0.0001);
        }
    }
}
