package com.xlsbuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.xlsbuilder.XlsxTestReader.Grid;

import de.makno.xlsbuilder.ColumnType;
import de.makno.xlsbuilder.DataProviders;
import de.makno.xlsbuilder.ExcelBuilder;
import de.makno.xlsbuilder.SortOrder;

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

        ExcelBuilder.<Person>create()
                .sheetName("Leute")
                .column("Name", ColumnType.STRING, Person::name)
                .column("Alter", ColumnType.INTEGER, Person::age)
                .column("Aktiv", ColumnType.BOOLEAN, Person::active)
                .write(DataProviders.ofIterable(data), out);

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
        ExcelBuilder.<Person>create()
                .column("Name", ColumnType.STRING, Person::name)
                .write(DataProviders.ofIterable(List.of(new Person("X", 1, true))), out);

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

        ExcelBuilder.<Person>create()
                .column("Name", ColumnType.STRING, Person::name)
                .column("Alter", ColumnType.INTEGER, Person::age)
                .sortBy("Alter", SortOrder.DESC)
                .write(DataProviders.ofIterable(data), out);

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

        ExcelBuilder.<DeptRow>create()
                .column("Abteilung", ColumnType.STRING, DeptRow::dept)
                .column("Gehalt", ColumnType.INTEGER, DeptRow::salary)
                .sortBy("Abteilung", SortOrder.ASC)
                .sortBy("Gehalt", SortOrder.DESC)
                .write(DataProviders.ofIterable(data), out);

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

        ExcelBuilder.<Integer>create()
                .column("n", ColumnType.INTEGER, i -> i)
                .sortBy("n", SortOrder.ASC)
                .sortChunkSize(100)
                .write(DataProviders.ofIterable(shuffled), out);

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
    void unsortedPreservesInputOrder() throws Exception {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            data.add(i);
        }
        Path out = tempDir.resolve("unsorted.xlsx");

        ExcelBuilder.<Integer>create()
                .column("n", ColumnType.INTEGER, i -> i)
                .write(DataProviders.ofIterable(data), out);

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

        ExcelBuilder.<Sale>create()
                .column("Datum", ColumnType.DATE, Sale::date)
                .column("Betrag", ColumnType.DECIMAL, Sale::amount)
                .write(DataProviders.ofIterable(List.of(new Sale(date, new BigDecimal("1234.56")))), out);

        Grid g = XlsxTestReader.read(out);
        assertTrue(g.isDateFormatted(1, 0), "Datumszelle muss ein Datumsformat haben");
        assertEquals(date, g.dateTime(1, 0).toLocalDate());
        assertEquals(1234.56, g.dbl(1, 1), 0.0001);
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

        ExcelBuilder.<Item>create()
                .column("Name", ColumnType.STRING, Item::name)
                .column("Menge", ColumnType.INTEGER, Item::menge)
                .column("Betrag", ColumnType.DECIMAL, Item::betrag)
                .sumColumn("Menge")
                .sumColumn("Betrag")
                .summaryLabel("Name", "Summe")
                .write(DataProviders.ofIterable(data), out);

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

        ExcelBuilder.<Integer>create()
                .column("n", ColumnType.LONG, i -> (long) i)
                .sortBy("n", SortOrder.ASC)
                .sortChunkSize(100)
                .sumColumn("n")
                .write(DataProviders.ofIterable(data), out);

        Grid g = XlsxTestReader.read(out);
        assertEquals(1002, g.rowCount(), "Kopf + 1000 Daten + Summenzeile");
        assertEquals(expectedSum, g.number(1001, 0));
    }

    @Test
    void addsTitleHeaderRowsMergedAcrossWidth() throws Exception {
        List<Person> data = List.of(new Person("Alice", 30, true));
        Path out = tempDir.resolve("header.xlsx");

        ExcelBuilder.<Person>create()
                .header("Mitarbeiterbericht", "Stand: Mai 2026")
                .column("Name", ColumnType.STRING, Person::name)
                .column("Alter", ColumnType.INTEGER, Person::age)
                .column("Aktiv", ColumnType.BOOLEAN, Person::active)
                .write(DataProviders.ofIterable(data), out);

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

        ExcelBuilder.<Item>create()
                .header("Bericht")
                .column("Name", ColumnType.STRING, Item::name)
                .column("Wert", ColumnType.INTEGER, Item::wert)
                .sortBy("Wert", SortOrder.DESC)
                .sumColumn("Wert")
                .summaryLabel("Name", "Summe")
                .write(DataProviders.ofIterable(data), out);

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
}
