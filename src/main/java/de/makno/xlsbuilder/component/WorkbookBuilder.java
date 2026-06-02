package de.makno.xlsbuilder.component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Fasst ein oder mehrere Worksheets in einer {@code .xlsx}-Datei zusammen. Jedes Blatt wird durch
 * einen eigenständigen {@link ExcelBuilder} beschrieben (mit eigenem Datentyp, eigenen Spalten,
 * Sortierung, Summenzeile usw.) und liefert seine Daten über {@link ExcelBuilder#data(DataProvider)}.
 *
 * <p>Die Blätter werden nacheinander gestreamt geschrieben (inkl. External Merge Sort je Blatt),
 * sodass der Speicherbedarf out-of-core/beschränkt bleibt.
 *
 * <pre>{@code
 * WorkbookBuilder.create()
 *     .sheet(ExcelBuilder.<Employee>create().sheetName("Mitarbeiter")
 *         .column("Name", Employee::name)
 *         .data(employeeProvider))
 *     .sheet(ExcelBuilder.<Order>create().sheetName("Aufträge")
 *         .column("Nr", Order::id).ofType(ColumnType.LONG)
 *         .data(orderProvider))
 *     .write(Path.of("report.xlsx"));
 * }</pre>
 */
public final class WorkbookBuilder {

    /** Anzahl Zeilen, die SXSSF je Blatt gleichzeitig im Speicher hält (Rest wird ausgelagert). */
    private static final int ROW_WINDOW = 100;

    private final List<ExcelBuilder<?>> sheets = new ArrayList<>();

    private WorkbookBuilder() {
    }

    public static WorkbookBuilder create() {
        return new WorkbookBuilder();
    }

    /** Fügt ein Blatt hinzu. Der {@link ExcelBuilder} muss eine Datenquelle ({@code .data(...)}) haben. */
    public WorkbookBuilder sheet(ExcelBuilder<?> sheet) {
        sheets.add(Objects.requireNonNull(sheet, "sheet"));
        return this;
    }

    public void write(Path out) throws IOException {
        Objects.requireNonNull(out, "out");
        try (OutputStream os = Files.newOutputStream(out)) {
            write(os);
        }
    }

    public void write(OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        if (sheets.isEmpty()) {
            throw new IllegalStateException("Mindestens ein Blatt erforderlich");
        }
        try (SXSSFWorkbook wb = new SXSSFWorkbook(ROW_WINDOW)) {
            for (ExcelBuilder<?> sheet : sheets) {
                sheet.renderInto(wb);
            }
            wb.write(out);
        }
    }
}
