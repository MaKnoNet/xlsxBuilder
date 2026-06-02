package de.makno.xlsbuilder.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.makno.xlsbuilder.builder.XlsxTestReader.Grid;

class ExcelBuilderTest {

    @TempDir
    Path tempDir;

    private record Person(String name, int age, boolean active) {
    }

    private record DeptRow(String dept, int salary) {
    }

    @Test
    void writesHeaderAndColumns() throws Exception {
        List<Person> data = List.of(
                new Person("Alice", 30, true),
                new Person("Bob", 25, false));
        Path out = tempDir.resolve("basic.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Person>create()
                        .sheetName("Leute")
                        .column("Name", Person::name)
                        .column("Alter", Person::age).ofType(ColumnType.INTEGER)
                        .column("Aktiv", Person::active).ofType(ColumnType.BOOLEAN)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals("Leute", g.sheetName());
        assertEquals(3, g.rowCount(), "Kopfzeile + 2 Datenzeilen");
        assertEquals(List.of("Name", "Alter", "Aktiv"), g.strings(0));

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
                .sheet(ExcelBuilder.<Person>create()
                        .column("Name", Person::name)
                        .data(DataProviders.ofIterable(List.of(new Person("X", 1, true)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals("Sheet1", g.sheetName(), "Default-Blattname");
        assertEquals(2, g.rowCount());
        assertEquals("X", g.string(1, 0));
    }

    @Test
    void sortsDescendingByNumericColumn() throws Exception {
        List<Person> data = List.of(
                new Person("A", 30, true),
                new Person("B", 25, true),
                new Person("C", 40, true));
        Path out = tempDir.resolve("sortDesc.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Person>create()
                        .column("Name", Person::name)
                        .column("Alter", Person::age).ofType(ColumnType.INTEGER)
                        .sortBy("Alter", SortOrder.DESC)
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
        List<DeptRow> data = List.of(
                new DeptRow("B", 100),
                new DeptRow("A", 50),
                new DeptRow("A", 80),
                new DeptRow("B", 90));
        Path out = tempDir.resolve("multiSort.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<DeptRow>create()
                        .column("Abteilung", DeptRow::dept)
                        .column("Gehalt", DeptRow::salary).ofType(ColumnType.INTEGER)
                        .sortBy("Abteilung", SortOrder.ASC)
                        .sortBy("Gehalt", SortOrder.DESC)
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
        // 1000 gemischte Werte, Chunk-Größe 100 => 10 Runs + k-way-Merge.
        List<Integer> shuffled = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            shuffled.add(i);
        }
        Collections.shuffle(shuffled, new java.util.Random(7));
        Path out = tempDir.resolve("externalSort.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Integer>create()
                        .column("n", i -> i).ofType(ColumnType.INTEGER)
                        .sortBy("n", SortOrder.ASC)
                        .sortChunkSize(100)
                        .data(DataProviders.ofIterable(shuffled)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(1001, g.rowCount(), "Kopfzeile + 1000 Datenzeilen");
        long previous = Long.MIN_VALUE;
        for (int i = 1; i < g.rowCount(); i++) {
            long v = g.number(i, 0);
            assertTrue(v > previous, "Werte müssen streng aufsteigend sein");
            previous = v;
        }
        assertEquals(999, previous);
    }

    @Test
    void externalMergeSortWithMultiplePasses() throws Exception {
        // 600 Werte bei Chunk-Größe 2 => 300 Runs. Bei Fan-in 16 erzwingt das mehrstufiges
        // Vormerging (300 -> 19 -> 2 Runs), bevor der finale k-way-Merge läuft.
        List<Integer> shuffled = new ArrayList<>();
        for (int i = 0; i < 600; i++) {
            shuffled.add(i);
        }
        Collections.shuffle(shuffled, new java.util.Random(11));
        Path out = tempDir.resolve("multiPassSort.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Integer>create()
                        .column("n", i -> i).ofType(ColumnType.INTEGER)
                        .sortBy("n", SortOrder.ASC)
                        .sortChunkSize(2)
                        .data(DataProviders.ofIterable(shuffled)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(601, g.rowCount(), "Kopfzeile + 600 Datenzeilen");
        long previous = Long.MIN_VALUE;
        for (int i = 1; i < g.rowCount(); i++) {
            long v = g.number(i, 0);
            assertTrue(v > previous, "Werte müssen über alle Merge-Stufen streng aufsteigend sein");
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
                .sheet(ExcelBuilder.<Integer>create()
                        .column("n", i -> i).ofType(ColumnType.INTEGER)
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
        record Sale(LocalDate date, BigDecimal amount) {
        }
        LocalDate date = LocalDate.of(2026, 5, 30);
        Path out = tempDir.resolve("formats.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Sale>create()
                        .column("Datum", Sale::date).ofType(ColumnType.DATE)
                        .column("Betrag", Sale::amount).ofType(ColumnType.DECIMAL)
                        .data(DataProviders.ofIterable(List.of(new Sale(date, new BigDecimal("1234.56"))))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertTrue(g.isDateFormatted(1, 0), "Datumszelle muss ein Datumsformat haben");
        assertEquals(date, g.dateTime(1, 0).toLocalDate());
        assertEquals(1234.56, g.dbl(1, 1), 0.0001);
    }

    @Test
    void appliesCustomFormatsForDecimalDateAndTime() throws Exception {
        record R(BigDecimal betrag, LocalDate datum, LocalTime zeit) {
        }
        LocalDate date = LocalDate.of(2026, 5, 30);
        LocalTime time = LocalTime.of(9, 30, 15);
        Path out = tempDir.resolve("customFormats.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<R>create()
                        .column("Betrag", R::betrag).ofType(ColumnType.DECIMAL).formatForType("#,##0.00")
                        .column("Datum", R::datum).ofType(ColumnType.DATE).formatForType("dd.mm.yyyy")
                        .column("Zeit", R::zeit).ofType(ColumnType.TIME).formatForType("hh:mm:ss")
                        .data(DataProviders.ofIterable(List.of(new R(new BigDecimal("1234.5"), date, time)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);

        assertEquals("#,##0.00", g.format(1, 0), "DECIMAL-Format");
        assertEquals(1234.5, g.dbl(1, 0), 0.0001);

        assertEquals("dd.mm.yyyy", g.format(1, 1), "DATE-Format");
        assertEquals(date, g.dateTime(1, 1).toLocalDate());

        assertEquals("hh:mm:ss", g.format(1, 2), "TIME-Format");
        assertEquals(time, g.dateTime(1, 2).toLocalTime());
    }

    @Test
    void summaryRowCanUseSumFormula() throws Exception {
        record Item(String name, int wert) {
        }
        List<Item> data = List.of(new Item("A", 10), new Item("B", 30), new Item("C", 20));
        Path out = tempDir.resolve("summaryFormula.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Item>create()
                        .header("Bericht") // Titelzeile -> Datenbereich ist versetzt
                        .column("Name", Item::name)
                        .column("Wert", Item::wert).ofType(ColumnType.INTEGER)
                        .sumColumn("Wert")
                        .summaryLabel("Name", "Summe")
                        .summaryAsFormula(true)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // Titel(1) + Überschrift(2) + Daten(3-5) + Summe(6)
        assertEquals(6, g.rowCount());
        assertEquals("Summe", g.string(5, 0));
        // Wert ist Spalte B; Datenzeilen sind Excel-Zeilen 3..5.
        assertEquals("SUM(B3:B5)", g.formula(5, 1));
    }

    @Test
    void writesFormulaColumnWithRowPlaceholder() throws Exception {
        record P(int a, int b) {
        }
        Path out = tempDir.resolve("formula.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<P>create()
                        .column("A", P::a).ofType(ColumnType.INTEGER)
                        .column("B", P::b).ofType(ColumnType.INTEGER)
                        .column("Summe", p -> "A{row}+B{row}").ofType(ColumnType.FORMULA)
                        .data(DataProviders.ofIterable(List.of(new P(2, 3), new P(10, 20)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // Kopfzeile = Zeile 1; Datenzeilen sind Excel-Zeilen 2 und 3.
        assertEquals("A2+B2", g.formula(1, 2));
        assertEquals("A3+B3", g.formula(2, 2));
    }

    @Test
    void appendsSummaryRowWithSums() throws Exception {
        record Item(String name, int menge, BigDecimal betrag) {
        }
        List<Item> data = List.of(
                new Item("A", 2, new BigDecimal("10.50")),
                new Item("B", 3, new BigDecimal("5.25")),
                new Item("C", 1, new BigDecimal("4.25")));
        Path out = tempDir.resolve("summary.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Item>create()
                        .column("Name", Item::name)
                        .column("Menge", Item::menge).ofType(ColumnType.INTEGER)
                        .column("Betrag", Item::betrag).ofType(ColumnType.DECIMAL)
                        .sumColumn("Menge")
                        .sumColumn("Betrag")
                        .summaryLabel("Name", "Summe")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(5, g.rowCount(), "Kopf + 3 Daten + 1 Summenzeile");
        assertEquals("Summe", g.string(4, 0));
        assertEquals(6, g.number(4, 1));
        assertEquals(20.00, g.dbl(4, 2), 0.0001);
    }

    @Test
    void summaryRowSumsAcrossSortedAndSpilledData() throws Exception {
        // Summe muss über ALLE Zeilen gebildet werden, auch wenn extern sortiert/ausgelagert wird.
        List<Integer> data = new ArrayList<>();
        long expectedSum = 0;
        for (int i = 1; i <= 1000; i++) {
            data.add(i);
            expectedSum += i;
        }
        Collections.shuffle(data, new java.util.Random(3));
        Path out = tempDir.resolve("summarySorted.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Integer>create()
                        .column("n", i -> (long) i).ofType(ColumnType.LONG)
                        .sortBy("n", SortOrder.ASC)
                        .sortChunkSize(100)
                        .sumColumn("n")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(1002, g.rowCount(), "Kopf + 1000 Daten + Summenzeile");
        assertEquals(expectedSum, g.number(1001, 0));
    }

    @Test
    void setsColumnWidthsSoFormattedValuesAreVisible() throws Exception {
        record R(LocalDate datum) {
        }
        Path out = tempDir.resolve("widths.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<R>create()
                        .column("Eintritt", R::datum).ofType(ColumnType.DATE).formatForType("dd.mm.yyyy")
                        .data(DataProviders.ofIterable(List.of(new R(LocalDate.of(2026, 12, 31))))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // Deutlich breiter als die POI-Standardbreite (~2048), damit kein "#####" entsteht.
        assertTrue(g.columnWidth(0) >= 3000, "Datumsspalte muss breit genug sein");
    }

    @Test
    void widthsAccountForLongStringsAndSummarySum() throws Exception {
        record Item(String name, int wert) {
        }
        String longName = "Ein sehr langer Mitarbeitername XYZ"; // 35 Zeichen
        List<Item> data = List.of(new Item(longName, 2_000_000), new Item("Kurz", 3_000_000));
        Path out = tempDir.resolve("widths2.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Item>create()
                        .column("Name", Item::name)
                        .column("Wert", Item::wert).ofType(ColumnType.INTEGER).formatForType("#,##0")
                        .sumColumn("Wert")
                        .summaryLabel("Name", "Summe")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // Name-Spalte mindestens so breit wie der längste Name.
        assertTrue(g.columnWidth(0) >= longName.length() * 256,
                "Name-Spalte muss den längsten Namen fassen");
        // Summe = 5.000.000 -> "5.000.000" (9 Zeichen inkl. Tausenderpunkte).
        assertTrue(g.columnWidth(1) >= 9 * 256, "Wert-Spalte muss die Summe fassen");
    }

    @Test
    void addsTitleHeaderRowsMergedAcrossWidth() throws Exception {
        List<Person> data = List.of(new Person("Alice", 30, true));
        Path out = tempDir.resolve("header.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Person>create()
                        .header("Mitarbeiterbericht", "Stand: Mai 2026")
                        .column("Name", Person::name)
                        .column("Alter", Person::age).ofType(ColumnType.INTEGER)
                        .column("Aktiv", Person::active).ofType(ColumnType.BOOLEAN)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // 2 Titelzeilen + Spaltenüberschriften + 1 Datenzeile
        assertEquals(4, g.rowCount());
        assertEquals("Mitarbeiterbericht", g.string(0, 0));
        assertTrue(g.bold(0, 0), "Titel ist fett formatiert");
        assertEquals("Stand: Mai 2026", g.string(1, 0));
        assertEquals(List.of("Name", "Alter", "Aktiv"), g.strings(2));
        assertEquals("Alice", g.string(3, 0));
        assertEquals(30, g.number(3, 1));
        assertTrue(g.bool(3, 2));

        // Titel über die volle Breite (3 Spalten -> A..C) zusammengeführt.
        assertEquals(List.of("A1:C1", "A2:C2"), g.mergeRefs());
    }

    @Test
    void combinesHeaderSortAndSummary() throws Exception {
        record Item(String name, int wert) {
        }
        List<Item> data = List.of(
                new Item("A", 10),
                new Item("B", 30),
                new Item("C", 20));
        Path out = tempDir.resolve("combined.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Item>create()
                        .header("Bericht")
                        .column("Name", Item::name)
                        .column("Wert", Item::wert).ofType(ColumnType.INTEGER)
                        .sortBy("Wert", SortOrder.DESC)
                        .sumColumn("Wert")
                        .summaryLabel("Name", "Summe")
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // Titel + Spaltenüberschriften + 3 Daten + Summenzeile
        assertEquals(6, g.rowCount());
        assertEquals("Bericht", g.string(0, 0));
        assertEquals(List.of("Name", "Wert"), g.strings(1));
        assertEquals("B", g.string(2, 0));
        assertEquals(30, g.number(2, 1));
        assertEquals("C", g.string(3, 0));
        assertEquals(20, g.number(3, 1));
        assertEquals("A", g.string(4, 0));
        assertEquals(10, g.number(4, 1));
        assertEquals("Summe", g.string(5, 0));
        assertEquals(60, g.number(5, 1));
    }

    @Test
    void convertsRawValueToTargetColumnType() throws Exception {
        // Rohwert int (Sekunden seit Mitternacht) -> als Uhrzeit (TIME) schreiben.
        record Task(String name, int sekunden) {
        }
        Path out = tempDir.resolve("convert.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Task>create()
                        .column("Name", Task::name)
                        .column("Start", Task::sekunden).ofType(ColumnType.TIME)
                        .convertToColumnType((Integer s) -> LocalTime.ofSecondOfDay(s))
                        .data(DataProviders.ofIterable(List.of(new Task("A", 34215))))) // 09:30:15
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertTrue(g.isDateFormatted(1, 1), "Konvertierte Zelle ist als Uhrzeit formatiert");
        assertEquals(LocalTime.of(9, 30, 15), g.dateTime(1, 1).toLocalTime());
    }

    @Test
    void writesMultipleSheets() throws Exception {
        record Emp(String name, int age) {
        }
        record Dept(String code) {
        }
        Path out = tempDir.resolve("multi.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Emp>create().sheetName("Mitarbeiter")
                        .column("Name", Emp::name)
                        .column("Alter", Emp::age).ofType(ColumnType.INTEGER)
                        .data(DataProviders.ofIterable(List.of(new Emp("Alice", 30), new Emp("Bob", 25)))))
                .sheet(ExcelBuilder.<Dept>create().sheetName("Abteilungen")
                        .column("Kürzel", Dept::code)
                        .data(DataProviders.ofIterable(List.of(new Dept("IT"), new Dept("HR")))))
                .write(out);

        assertEquals(List.of("Mitarbeiter", "Abteilungen"), XlsxTestReader.sheetNames(out));

        Grid s0 = XlsxTestReader.read(out, 0);
        assertEquals(List.of("Name", "Alter"), s0.strings(0));
        assertEquals("Alice", s0.string(1, 0));
        assertEquals(30, s0.number(1, 1));

        Grid s1 = XlsxTestReader.read(out, 1);
        assertEquals(List.of("Kürzel"), s1.strings(0));
        assertEquals("IT", s1.string(1, 0));
        assertEquals("HR", s1.string(2, 0));
    }

    @Test
    void deduplicatesSheetNames() throws Exception {
        record R(String v) {
        }
        Path out = tempDir.resolve("dupe.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<R>create().sheetName("Daten").column("V", R::v)
                        .data(DataProviders.ofIterable(List.of(new R("a")))))
                .sheet(ExcelBuilder.<R>create().sheetName("Daten").column("V", R::v)
                        .data(DataProviders.ofIterable(List.of(new R("b")))))
                .write(out);

        List<String> names = XlsxTestReader.sheetNames(out);
        assertEquals(2, names.size());
        assertEquals("Daten", names.get(0));
        assertNotEquals("Daten", names.get(1), "zweites Blatt muss eindeutigen Namen erhalten");
    }

    // ========== Gruppe A – Exception / Validation ==========

    @Test
    void throwsIfNoColumnsConfigured() {
        assertThrows(
                IllegalStateException.class,
                () -> WorkbookBuilder.create()
                        .sheet(ExcelBuilder.<Person>create()
                                .data(DataProviders.ofIterable(List.of())))
                        .write(tempDir.resolve("noColumns.xlsx")));
    }

    @Test
    void throwsIfNoDataProviderSet() {
        assertThrows(
                IllegalStateException.class,
                () -> WorkbookBuilder.create()
                        .sheet(ExcelBuilder.<Person>create().column("Name", Person::name))
                        .write(tempDir.resolve("noProvider.xlsx")));
    }

    @Test
    void throwsIfSumColumnIsNotNumeric() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WorkbookBuilder.create()
                        .sheet(ExcelBuilder.<Person>create()
                                .column("Name", Person::name)
                                .sumColumn("Name")
                                .data(DataProviders.ofIterable(
                                        List.of(new Person("A", 1, true)))))
                        .write(tempDir.resolve("badSum.xlsx")));
    }

    @Test
    void throwsIfSortChunkSizeLessThanOne() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ExcelBuilder.<Person>create()
                        .column("Name", Person::name)
                        .sortChunkSize(0));
    }

    @Test
    void throwsIfWorkbookHasNoSheets() {
        assertThrows(
                IllegalStateException.class,
                () -> WorkbookBuilder.create().write(tempDir.resolve("noSheets.xlsx")));
    }

    @Test
    void throwsIfSortKeyColumnUnknown() {
        assertThrows(
                IllegalArgumentException.class,
                () -> WorkbookBuilder.create()
                        .sheet(ExcelBuilder.<Person>create()
                                .column("Name", Person::name)
                                .sortBy("NichtVorhanden", SortOrder.ASC)
                                .data(DataProviders.ofIterable(
                                        List.of(new Person("A", 1, true)))))
                        .write(tempDir.resolve("badSortKey.xlsx")));
    }

    // ========== Gruppe B – Null-Handling im Comparator ==========

    @Test
    void sortsNullsLastAscending() throws Exception {
        record NullRow(String label) {
        }
        List<NullRow> data = List.of(new NullRow("B"), new NullRow(null), new NullRow("A"));
        Path out = tempDir.resolve("nullsAsc.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<NullRow>create()
                        .column("Label", NullRow::label)
                        .sortBy("Label", SortOrder.ASC)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // ASC nulls-last: "A", "B", null
        assertEquals("A", g.string(1, 0));
        assertEquals("B", g.string(2, 0));
        assertNull(g.string(3, 0), "null-Wert landet als leere Zelle am Ende");
    }

    @Test
    void sortsNullsFirstInDescending() throws Exception {
        // Null wird intern als „größter Wert" behandelt (nulls-last bei ASC).
        // Bei DESC (Vorzeichen-Flip) erscheint null daher am Anfang.
        record NullRow(String label) {
        }
        List<NullRow> data = List.of(new NullRow("B"), new NullRow(null), new NullRow("A"));
        Path out = tempDir.resolve("nullsDesc.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<NullRow>create()
                        .column("Label", NullRow::label)
                        .sortBy("Label", SortOrder.DESC)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // DESC: null (= größter Wert) steht ganz vorne, dann B, dann A
        assertNull(g.string(1, 0), "null kommt bei DESC-Sortierung zuerst");
        assertEquals("B", g.string(2, 0));
        assertEquals("A", g.string(3, 0));
    }

    // ========== Gruppe C – Weitere Typen und XlsxWriter-Zweige ==========

    @Test
    void writesDateTimeColumn() throws Exception {
        record Event(String name, LocalDateTime when) {
        }
        LocalDateTime dt = LocalDateTime.of(2026, 3, 15, 14, 30);
        Path out = tempDir.resolve("datetime.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Event>create()
                        .column("Name", Event::name)
                        .column("Zeitpunkt", Event::when).ofType(ColumnType.DATETIME)
                        .data(DataProviders.ofIterable(List.of(new Event("Test", dt)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertTrue(g.isDateFormatted(1, 1), "DATETIME-Zelle muss als Datum formatiert sein");
        assertEquals(dt, g.dateTime(1, 1));
    }

    @Test
    void writesDoubleColumn() throws Exception {
        record Measurement(String label, double value) {
        }
        Path out = tempDir.resolve("doubleCol.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Measurement>create()
                        .column("Label", Measurement::label)
                        .column("Wert", Measurement::value).ofType(ColumnType.DOUBLE)
                        .data(DataProviders.ofIterable(
                                List.of(new Measurement("pi", 3.14159)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(3.14159, g.dbl(1, 1), 0.00001);
    }

    @Test
    void writesFormulaColumnWithoutRowPlaceholder() throws Exception {
        record R(int a, int b) {
        }
        Path out = tempDir.resolve("staticFormula.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<R>create()
                        .column("A", R::a).ofType(ColumnType.INTEGER)
                        .column("B", R::b).ofType(ColumnType.INTEGER)
                        // Statische Formel ohne {row}-Platzhalter
                        .column("Summe", r -> "A2+B2").ofType(ColumnType.FORMULA)
                        .data(DataProviders.ofIterable(List.of(new R(5, 7)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // Formel darf kein {row} enthalten → unveränderter Text wird als Formel gesetzt.
        assertEquals("A2+B2", g.formula(1, 2));
    }

    @Test
    void headerWithSingleColumnNoMerge() throws Exception {
        Path out = tempDir.resolve("singleColHeader.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Person>create()
                        .header("Nur eine Spalte")
                        .column("Name", Person::name)
                        .data(DataProviders.ofIterable(List.of(new Person("Alice", 30, true)))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals("Nur eine Spalte", g.string(0, 0));
        // Nur 1 Spalte → kein Merge-Bereich
        assertTrue(g.mergeRefs().isEmpty(), "Einzel-Spalte darf keinen Merge erzeugen");
    }

    @Test
    void writesEmptyDataSource() throws Exception {
        Path out = tempDir.resolve("empty.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Person>create()
                        .column("Name", Person::name)
                        .column("Alter", Person::age).ofType(ColumnType.INTEGER)
                        .data(DataProviders.ofIterable(List.of())))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // Nur Kopfzeile, keine Datenzeilen
        assertEquals(1, g.rowCount(), "Leere Quelle erzeugt nur Kopfzeile");
        assertEquals(List.of("Name", "Alter"), g.strings(0));
    }

    @Test
    void summaryWithPrecomputedDecimal() throws Exception {
        record Item(String name, BigDecimal betrag) {
        }
        List<Item> data = List.of(
                new Item("X", new BigDecimal("10.50")),
                new Item("Y", new BigDecimal("5.25")));
        Path out = tempDir.resolve("sumDecimal.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Item>create()
                        .column("Name", Item::name)
                        .column("Betrag", Item::betrag)
                                .ofType(ColumnType.DECIMAL)
                                .formatForType("#,##0.00")
                        .sumColumn("Betrag")
                        .summaryLabel("Name", "Gesamt")
                        // summaryAsFormula(false) ist der Default → vorberechneter Wert
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(4, g.rowCount(), "Kopf + 2 Daten + Summenzeile");
        assertEquals("Gesamt", g.string(3, 0));
        assertEquals(15.75, g.dbl(3, 1), 0.001);
    }

    // ========== Gruppe D – DataProviders & ExternalMergeSort ==========

    @Test
    void dataProviderOfStreamAdapter() throws Exception {
        Path out = tempDir.resolve("stream.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<String>create()
                        .column("Wert", s -> s)
                        .data(DataProviders.ofStream(Stream.of("Alpha", "Beta", "Gamma"))))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(4, g.rowCount(), "Kopf + 3 Datenzeilen");
        assertEquals("Alpha", g.string(1, 0));
        assertEquals("Beta", g.string(2, 0));
        assertEquals("Gamma", g.string(3, 0));
    }

    @Test
    void dataProviderOfIteratorAdapter() throws Exception {
        Path out = tempDir.resolve("iterator.xlsx");
        var iterator = List.of("Eins", "Zwei").iterator();

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<String>create()
                        .column("Wert", s -> s)
                        .data(DataProviders.ofIterator(iterator)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(3, g.rowCount(), "Kopf + 2 Datenzeilen");
        assertEquals("Eins", g.string(1, 0));
        assertEquals("Zwei", g.string(2, 0));
    }

    @Test
    void externalSortWithEmptyInput() throws Exception {
        Path out = tempDir.resolve("emptySorted.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Integer>create()
                        .column("n", i -> i).ofType(ColumnType.INTEGER)
                        .sortBy("n", SortOrder.ASC)
                        .data(DataProviders.ofIterable(List.of())))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // Sortierung mit leerer Quelle → kein Run, MergeIterator leer, nur Kopfzeile
        assertEquals(1, g.rowCount(), "Leere sortierte Quelle erzeugt nur Kopfzeile");
    }

    @Test
    void externalSortChunkSizeValidation() {
        // ExternalMergeSort direkt (package-private, gleicher Package) instanziieren.
        var comparator = new RowComparator(
                List.of(new Column<>("n", ColumnType.INTEGER, i -> i)),
                List.of(new SortKey("n", SortOrder.ASC)));
        assertThrows(IllegalArgumentException.class, () -> new ExternalMergeSort(comparator, 0));
    }

    @Test
    void streamProviderThrowsWhenExhausted() {
        // ofStream().next() ohne weiteres Element -> NoSuchElementException (Guard wie bei ofIterator).
        DataProvider<String> provider = DataProviders.ofStream(Stream.of("einziger"));
        assertEquals("einziger", provider.next());
        assertThrows(NoSuchElementException.class, provider::next);
    }

    @Test
    void rowCodecRoundTripsAllValueTypes() throws Exception {
        // Deckt alle Typtags des RowCodec ab, inkl. UTF-8-String und Java-Serialisierungs-Fallback.
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
        try (var in =
                new java.io.DataInputStream(new java.io.ByteArrayInputStream(buffer.toByteArray()))) {
            restored = RowCodec.readRow(in);
        }

        assertEquals(values.length, restored.size());
        for (int i = 0; i < values.length; i++) {
            assertEquals(values[i], restored.get(i), "Wert an Index " + i);
        }
        // Laufzeittyp muss exakt erhalten bleiben (sonst bräche der Vergleich Integer vs. Long).
        assertTrue(restored.get(2) instanceof Integer, "Integer bleibt Integer");
        assertTrue(restored.get(3) instanceof Long, "Long bleibt Long");
    }

    @Test
    void comparatorRejectsIncompatibleValueTypes() {
        // Zwei Zeilen mit inkompatiblen Werttypen in der Sortierspalte -> aussagekräftige Exception
        // statt roher ClassCastException.
        var comparator = new RowComparator(
                List.of(new Column<>("v", ColumnType.STRING, x -> x)),
                List.of(new SortKey("v", SortOrder.ASC)));
        Row textRow = new Row(new Object[] {"abc"});
        Row numberRow = new Row(new Object[] {123});
        assertThrows(
                IllegalArgumentException.class, () -> comparator.compare(textRow, numberRow));
    }

    // ========== Spaltenüberschriften-Schalter ==========

    @Test
    void writesWithoutColumnHeaders() throws Exception {
        // columnHeaders(false) → erste Zeile ist direkt eine Datenzeile, keine Überschrift.
        List<Person> data = List.of(new Person("Alice", 30, true), new Person("Bob", 25, false));
        Path out = tempDir.resolve("noHeader.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Person>create()
                        .column("Name", Person::name)
                        .column("Alter", Person::age).ofType(ColumnType.INTEGER)
                        .columnHeaders(false)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(2, g.rowCount(), "Nur 2 Datenzeilen, keine Kopfzeile");
        assertEquals("Alice", g.string(0, 0));
        assertEquals(30, g.number(0, 1));
        assertEquals("Bob", g.string(1, 0));
    }

    @Test
    void summaryFormulaWithoutColumnHeaders() throws Exception {
        // Ohne Kopfzeile beginnen die Daten in Excel-Zeile 1 → Formel muss SUM(B1:B2) lauten.
        record Item(String name, int wert) {
        }
        List<Item> data = List.of(new Item("A", 10), new Item("B", 20));
        Path out = tempDir.resolve("noHeaderSum.xlsx");

        WorkbookBuilder.create()
                .sheet(ExcelBuilder.<Item>create()
                        .column("Name", Item::name)
                        .column("Wert", Item::wert).ofType(ColumnType.INTEGER)
                        .sumColumn("Wert")
                        .summaryAsFormula(true)
                        .columnHeaders(false)
                        .data(DataProviders.ofIterable(data)))
                .write(out);

        Grid g = XlsxTestReader.read(out);
        // Ohne Kopfzeile: Daten in Excel-Zeilen 1–2, Summenzeile in Zeile 3
        assertEquals(3, g.rowCount());
        assertEquals("SUM(B1:B2)", g.formula(2, 1));
    }
}
