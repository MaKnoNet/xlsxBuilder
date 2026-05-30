package com.xlsbuilder;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/** Liest erzeugte {@code .xlsx}-Dateien mit reinem JDK zurück (zur Verifikation in den Tests). */
final class XlsxTestReader {

    record Cell(String value, int styleIndex, String type) {
    }

    private XlsxTestReader() {
    }

    /** Namen aller ZIP-Einträge (zur Strukturprüfung). */
    static Set<String> entryNames(Path xlsx) throws Exception {
        Set<String> names = new HashSet<>();
        try (ZipFile zf = new ZipFile(xlsx.toFile())) {
            zf.stream().map(ZipEntry::getName).forEach(names::add);
        }
        return names;
    }

    /** Liest die {@code mergeCell}-Referenzen (z. B. "A1:C1") der erzeugten Datei. */
    static List<String> mergeRefs(Path xlsx) throws Exception {
        try (ZipFile zf = new ZipFile(xlsx.toFile());
             InputStream in = zf.getInputStream(zf.getEntry("xl/worksheets/sheet1.xml"))) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document doc = factory.newDocumentBuilder().parse(in);
            List<String> refs = new ArrayList<>();
            NodeList nodes = doc.getElementsByTagName("mergeCell");
            for (int i = 0; i < nodes.getLength(); i++) {
                refs.add(((Element) nodes.item(i)).getAttribute("ref"));
            }
            return refs;
        }
    }

    /** DOM-Lesung (für kleine Dateien): alle Zeilen inkl. Kopfzeile als Liste von Zellen. */
    static List<List<Cell>> readAll(Path xlsx) throws Exception {
        try (ZipFile zf = new ZipFile(xlsx.toFile());
             InputStream in = zf.getInputStream(zf.getEntry("xl/worksheets/sheet1.xml"))) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document doc = factory.newDocumentBuilder().parse(in);

            List<List<Cell>> rows = new ArrayList<>();
            NodeList rowNodes = doc.getElementsByTagName("row");
            for (int i = 0; i < rowNodes.getLength(); i++) {
                Element rowEl = (Element) rowNodes.item(i);
                List<Cell> cells = new ArrayList<>();
                NodeList cNodes = rowEl.getElementsByTagName("c");
                for (int j = 0; j < cNodes.getLength(); j++) {
                    Element c = (Element) cNodes.item(j);
                    String type = c.getAttribute("t");
                    String s = c.getAttribute("s");
                    int style = s.isEmpty() ? -1 : Integer.parseInt(s);
                    String value;
                    if ("inlineStr".equals(type)) {
                        NodeList ts = c.getElementsByTagName("t");
                        value = ts.getLength() > 0 ? ts.item(0).getTextContent() : "";
                    } else {
                        NodeList vs = c.getElementsByTagName("v");
                        value = vs.getLength() > 0 ? vs.item(0).getTextContent() : null;
                    }
                    cells.add(new Cell(value, style, type));
                }
                rows.add(cells);
            }
            return rows;
        }
    }

    /** StAX-Streaming (für große Dateien): ruft den Consumer je Datenzeile (ohne Kopfzeile) auf. */
    static long forEachDataRow(Path xlsx, BiConsumer<Long, List<String>> consumer) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, true);
        try (ZipFile zf = new ZipFile(xlsx.toFile());
             InputStream in = zf.getInputStream(zf.getEntry("xl/worksheets/sheet1.xml"))) {
            XMLStreamReader r = factory.createXMLStreamReader(in);
            long dataRows = 0;
            boolean firstRow = true;
            List<String> current = null;
            String pending = null;
            StringBuilder text = new StringBuilder();
            boolean capturing = false;

            while (r.hasNext()) {
                int event = r.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT -> {
                        switch (r.getLocalName()) {
                            case "row" -> current = new ArrayList<>();
                            case "c" -> pending = null;
                            case "v", "t" -> {
                                capturing = true;
                                text.setLength(0);
                            }
                            default -> {
                            }
                        }
                    }
                    case XMLStreamConstants.CHARACTERS -> {
                        if (capturing) {
                            text.append(r.getText());
                        }
                    }
                    case XMLStreamConstants.END_ELEMENT -> {
                        switch (r.getLocalName()) {
                            case "v", "t" -> {
                                if (capturing) {
                                    pending = text.toString();
                                    capturing = false;
                                }
                            }
                            case "c" -> current.add(pending);
                            case "row" -> {
                                if (firstRow) {
                                    firstRow = false;
                                } else {
                                    dataRows++;
                                    consumer.accept(dataRows, current);
                                }
                            }
                            default -> {
                            }
                        }
                    }
                    default -> {
                    }
                }
            }
            r.close();
            return dataRows;
        }
    }
}
