package com.xlsbuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

        List<List<XlsxTestReader.Cell>> rows = XlsxTestReader.readAll(out);
        assertEquals(3, rows.size(), "Kopfzeile + 2 Datenzeilen");
        assertEquals(List.of("Name", "Alter", "Aktiv"), values(rows.get(0)));
        assertEquals(List.of("Alice", "30", "1"), values(rows.get(1)));
        assertEquals(List.of("Bob", "25", "0"), values(rows.get(2)));
    }

    @Test
    void producesStructurallyValidXlsx() throws Exception {
        Path out = tempDir.resolve("struct.xlsx");
        ExcelBuilder.<Person>create()
                .column("Name", ColumnType.STRING, Person::name)
                .write(DataProviders.ofIterable(List.of(new Person("X", 1, true))), out);

        var entries = XlsxTestReader.entryNames(out);
        assertTrue(entries.contains("[Content_Types].xml"));
        assertTrue(entries.contains("xl/workbook.xml"));
        assertTrue(entries.contains("xl/worksheets/sheet1.xml"));
        assertTrue(entries.contains("xl/styles.xml"));
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

        List<List<XlsxTestReader.Cell>> rows = XlsxTestReader.readAll(out);
        List<String> ages = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            ages.add(rows.get(i).get(1).value());
        }
        assertEquals(List.of("40", "30", "25"), ages);
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

        List<List<XlsxTestReader.Cell>> rows = XlsxTestReader.readAll(out);
        List<String> ordered = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {
            ordered.add(rows.get(i).get(0).value() + ":" + rows.get(i).get(1).value());
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

        long[] previous = {Long.MIN_VALUE};
        long count = XlsxTestReader.forEachDataRow(out, (rowNum, cells) -> {
            long v = Long.parseLong(cells.get(0));
            assertTrue(v > previous[0], "Werte müssen streng aufsteigend sein");
            previous[0] = v;
        });
        assertEquals(1000, count);
        assertEquals(999, previous[0]);
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

        long[] expected = {0};
        long count = XlsxTestReader.forEachDataRow(out, (rowNum, cells) ->
                assertEquals(expected[0]++, Long.parseLong(cells.get(0))));
        assertEquals(500, count);
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

        List<List<XlsxTestReader.Cell>> rows = XlsxTestReader.readAll(out);
        XlsxTestReader.Cell dateCell = rows.get(1).get(0);
        XlsxTestReader.Cell amountCell = rows.get(1).get(1);

        long expectedSerial = ChronoUnit.DAYS.between(LocalDate.of(1899, 12, 30), date);
        assertEquals(1, dateCell.styleIndex(), "Datumszelle muss den Datums-Style referenzieren");
        assertEquals(expectedSerial, (long) Double.parseDouble(dateCell.value()));

        assertEquals("", amountCell.type(), "Dezimalzelle ist numerisch (kein t-Attribut)");
        assertEquals("1234.56", amountCell.value());
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

        List<List<XlsxTestReader.Cell>> rows = XlsxTestReader.readAll(out);
        assertEquals(5, rows.size(), "Kopf + 3 Daten + 1 Summenzeile");
        assertEquals(List.of("Summe", "6", "20.00"), values(rows.get(4)));
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

        List<List<XlsxTestReader.Cell>> rows = XlsxTestReader.readAll(out);
        assertEquals(1002, rows.size(), "Kopf + 1000 Daten + Summenzeile");
        assertEquals(Long.toString(expectedSum), rows.get(1001).get(0).value());
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

        List<List<XlsxTestReader.Cell>> rows = XlsxTestReader.readAll(out);
        // 2 Titelzeilen + Spaltenüberschriften + 1 Datenzeile
        assertEquals(4, rows.size());
        assertEquals("Mitarbeiterbericht", rows.get(0).get(0).value());
        assertEquals(3, rows.get(0).get(0).styleIndex(), "Titel nutzt den Titel-Style");
        assertEquals("Stand: Mai 2026", rows.get(1).get(0).value());
        assertEquals(List.of("Name", "Alter", "Aktiv"), values(rows.get(2)));
        assertEquals(List.of("Alice", "30", "1"), values(rows.get(3)));

        // Titel über die volle Breite (3 Spalten -> A..C) zusammengeführt.
        assertEquals(List.of("A1:C1", "A2:C2"), XlsxTestReader.mergeRefs(out));
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

        List<List<XlsxTestReader.Cell>> rows = XlsxTestReader.readAll(out);
        // Titel + Spaltenüberschriften + 3 Daten + Summenzeile
        assertEquals(6, rows.size());
        assertEquals("Bericht", rows.get(0).get(0).value());
        assertEquals(List.of("Name", "Wert"), values(rows.get(1)));
        assertEquals(List.of("B", "30"), values(rows.get(2)));
        assertEquals(List.of("C", "20"), values(rows.get(3)));
        assertEquals(List.of("A", "10"), values(rows.get(4)));
        assertEquals(List.of("Summe", "60"), values(rows.get(5)));
    }

    private static List<String> values(List<XlsxTestReader.Cell> cells) {
        List<String> out = new ArrayList<>();
        for (XlsxTestReader.Cell c : cells) {
            out.add(c.value());
        }
        return out;
    }
}
