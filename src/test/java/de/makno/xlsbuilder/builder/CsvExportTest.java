package de.makno.xlsbuilder.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests für den CSV-Export ({@link ExcelBuilder#writeCsv}). */
class CsvExportTest {

    @TempDir
    Path tempDir;

    private record R(String name, int wert) {}

    private static String[] lines(Path file) throws Exception {
        return Files.readString(file, StandardCharsets.UTF_8).split("\\r\\n", -1);
    }

    @Test
    void writesHeaderDataAndRfc4180Quoting() throws Exception {
        List<R> data =
                List.of(new R("Plain", 1), new R("Hat,Komma", 2), new R("Mit\"Quote", 3), new R("Zeilen\numbruch", 4));
        Path out = tempDir.resolve("basic.csv");

        ExcelBuilder.<R>create()
                .column("Name", R::name)
                .column("Wert", R::wert)
                .ofType(ColumnType.INTEGER)
                .data(DataProviders.ofIterable(data))
                .writeCsv(out);

        String[] lines = lines(out);
        assertEquals("Name,Wert", lines[0]);
        assertEquals("Plain,1", lines[1]);
        assertEquals("\"Hat,Komma\",2", lines[2]);
        assertEquals("\"Mit\"\"Quote\",3", lines[3]);
        assertEquals("\"Zeilen\numbruch\",4", lines[4], "eingebetteter Zeilenumbruch bleibt im Feld");
    }

    @Test
    void semicolonAndBomOption() throws Exception {
        Path out = tempDir.resolve("german.csv");
        ExcelBuilder.<R>create()
                .column("Name", R::name)
                .column("Wert", R::wert)
                .ofType(ColumnType.INTEGER)
                .data(DataProviders.ofIterable(List.of(new R("x", 7))))
                .writeCsv(out, CsvOptions.excelGerman());

        byte[] bytes = Files.readAllBytes(out);
        assertTrue(
                bytes.length >= 3
                        && (bytes[0] & 0xFF) == 0xEF
                        && (bytes[1] & 0xFF) == 0xBB
                        && (bytes[2] & 0xFF) == 0xBF,
                "UTF-8-BOM vorangestellt");
        String[] lines = lines(out);
        assertEquals("﻿Name;Wert", lines[0]); // BOM (U+FEFF) steckt am Anfang der ersten Zeile
        assertEquals("x;7", lines[1]);
    }

    @Test
    void summaryFooterPlaceholdersAndNull() throws Exception {
        List<R> data = java.util.Arrays.asList(new R("A", 10), new R(null, 20));
        Path out = tempDir.resolve("full.csv");

        ExcelBuilder.<R>create()
                .column("Name", R::name)
                .nullText("-")
                .column("Wert", R::wert)
                .ofType(ColumnType.INTEGER)
                .sumColumn("Wert")
                .summaryLabel("Name", "Summe")
                .footer("Zeilen {rowCount}, Summe {sum:Wert}")
                .data(DataProviders.ofIterable(data))
                .writeCsv(out);

        String[] lines = lines(out);
        assertEquals("Name,Wert", lines[0]);
        assertEquals("A,10", lines[1]);
        assertEquals("-,20", lines[2], "null -> Spalten-Platzhalter");
        assertEquals("Summe,30", lines[3], "vorberechnete Summenzeile");
        // Footer enthält ein Komma (Trennzeichen) -> RFC-4180-konform gequotet.
        assertEquals("\"Zeilen 2, Summe 30\"", lines[4], "Footer mit dynamischen Platzhaltern");
    }

    @Test
    void resolvesLazyPlaceholderInCsvFooter() throws Exception {
        Path out = tempDir.resolve("lazyFooter.csv");
        ExcelBuilder.<R>create()
                .column("Name", R::name)
                .column("Wert", R::wert)
                .ofType(ColumnType.INTEGER)
                .footer("Version {version}")
                .placeholderResolver(key -> "version".equals(key) ? "1.2.3" : null)
                .data(DataProviders.ofIterable(List.of(new R("A", 1))))
                .writeCsv(out);

        String[] lines = lines(out);
        assertEquals("Version 1.2.3", lines[2], "Resolver-Wert erscheint im CSV-Footer");
    }
}
