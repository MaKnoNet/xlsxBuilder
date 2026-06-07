package de.makno.xlsxbuilder.builder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Fasst ein oder mehrere Worksheets in einer {@code .xlsx}-Datei zusammen. Jedes Blatt wird durch
 * einen eigenständigen {@link XlsxBuilder} beschrieben (mit eigenem Datentyp, eigenen Spalten,
 * Sortierung, Summenzeile usw.) und liefert seine Daten über {@link XlsxBuilder#data(DataProvider)}.
 *
 * <p>Die Blätter werden nacheinander gestreamt geschrieben (inkl. External Merge Sort je Blatt),
 * sodass der Speicherbedarf out-of-core/beschränkt bleibt.
 *
 * <pre>{@code
 * WorkbookBuilder.create()
 *     .sheet(XlsxBuilder.<Employee>create().sheetName("Mitarbeiter")
 *         .column("Name", Employee::name)
 *         .data(employeeProvider))
 *     .sheet(XlsxBuilder.<Order>create().sheetName("Aufträge")
 *         .column("Nr", Order::id).ofType(ColumnType.LONG)
 *         .data(orderProvider))
 *     .write(Path.of("report.xlsx"));
 * }</pre>
 *
 * <p><b>Thread-Sicherheit:</b> nicht thread-safe und auf Einmal-Nutzung ausgelegt – pro Auftrag eine
 * eigene Instanz erzeugen, nicht zwischen Threads teilen und nicht gleichzeitig in dieselbe Datei
 * schreiben. Da die Bibliothek keinen geteilten/statischen Zustand hat, laufen nebenläufige Aufträge
 * mit jeweils eigenen Instanzen isoliert; pro {@link #write} entsteht ein eigenes POI-Workbook.
 *
 * <p><b>Einmal-Nutzung:</b> Eine Instanz darf nur einmal geschrieben werden; ein erneuter
 * {@link #write}-Aufruf wirft eine {@link IllegalStateException} (die Datenquellen der Blätter sind
 * forward-only und nach dem ersten Schreiben erschöpft).
 */
public final class WorkbookBuilder {

    private static final Logger LOG = LogManager.getLogger(WorkbookBuilder.class);

    /**
     * Standard-Anzahl Zeilen, die SXSSF je Blatt gleichzeitig im Speicher hält (Rest wird
     * ausgelagert). Dies ist ein guter Kompromiss für die meisten Anwendungsfälle.
     */
    private static final int DEFAULT_ROW_WINDOW = 100;

    private final List<XlsxBuilder<?>> sheets = new ArrayList<>();
    private int sxssfRowWindow = DEFAULT_ROW_WINDOW;
    private boolean written; // Einmal-Nutzung: nach write(...) nicht erneut verwendbar

    private WorkbookBuilder() {}

    public static WorkbookBuilder create() {
        return new WorkbookBuilder();
    }

    /**
     * Setzt die Anzahl Zeilen, die SXSSF je Blatt gleichzeitig im Speicher hält (der Rest wird auf
     * Temp-Dateien ausgelagert). Der Speicherbedarf steigt mit dieser Größe, aber auch die
     * Schreib-Performance. Default ist {@value #DEFAULT_ROW_WINDOW}.
     *
     * <p><b>Typische Werte:</b>
     * <ul>
     *   <li>50–100: sehr sparsam mit RAM, gut für sehr große Dateien mit limitiertem Heap;</li>
     *   <li>100–500: Standard, guter Balance zwischen Speicher und Performance;</li>
     *   <li>1000+: mehr RAM, aber auch mehr Performance (wenn Heap groß genug).</li>
     * </ul>
     *
     * @param window Anzahl Zeilen im Speicher (>= 1)
     * @return this für Fluent API
     * @throws IllegalArgumentException wenn window < 1
     */
    public WorkbookBuilder sxssfRowWindow(int window) {
        if (window < 1) {
            throw new IllegalArgumentException("sxssfRowWindow muss >= 1 sein");
        }
        this.sxssfRowWindow = window;
        return this;
    }

    /** Fügt ein Blatt hinzu. Der {@link XlsxBuilder} muss eine Datenquelle ({@code .data(...)}) haben. */
    public WorkbookBuilder sheet(XlsxBuilder<?> sheet) {
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
        if (written) {
            throw new IllegalStateException(
                    "WorkbookBuilder ist Einmal-Nutzung: bereits geschrieben – pro Auftrag eine neue Instanz"
                            + " erstellen");
        }
        written = true;
        long startNanos = System.nanoTime();
        try (SXSSFWorkbook wb = new SXSSFWorkbook(sxssfRowWindow)) {
            for (XlsxBuilder<?> sheet : sheets) {
                sheet.renderInto(wb);
            }
            wb.write(out);
        }
        LOG.debug(
                "Workbook: {} Blätter in {} ms geschrieben (sxssfRowWindow={})",
                sheets.size(),
                (System.nanoTime() - startNanos) / 1_000_000,
                sxssfRowWindow);
    }
}
