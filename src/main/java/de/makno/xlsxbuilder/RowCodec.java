package de.makno.xlsxbuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Compact, type-tagged (de)serialization of a {@link Row} for the run files of the
 * {@link ExternalMergeSort}.
 *
 * <p>Instead of Java's {@code ObjectOutputStream} (which writes class descriptors per object and is
 * expensive with {@code reset()}), each cell value is written with a 1-byte type tag plus a compact
 * primitive encoding. This is much faster and produces considerably smaller files (less disk I/O,
 * especially during multi-pass merging). The common cell types are encoded directly; unknown (but
 * {@link java.io.Serializable}) types fall back to Java serialization.
 *
 * <p>Important: the concrete runtime type is preserved (e.g. {@code Integer} vs. {@code Long}), so
 * that comparison ({@link RowComparator}) and width estimation are identical to the in-memory case.
 */
final class RowCodec {

    private static final byte NULL = 0;
    private static final byte STRING = 1;
    private static final byte INT = 2;
    private static final byte LONG = 3;
    private static final byte DOUBLE = 4;
    private static final byte BOOL = 5;
    private static final byte BIGDEC = 6;
    private static final byte LDATE = 7;
    private static final byte LDATETIME = 8;
    private static final byte LTIME = 9;
    private static final byte JAVA = 99; // fallback: any Serializable

    /**
     * Defense-in-depth resource limits for the Java-serialization fallback ({@link #JAVA}). The run
     * files are written and read by the <em>same</em> process (its own sort temp directory), so this is
     * not a primary trust boundary; the filter caps depth, references, array size and total bytes to
     * blunt deserialization "bombs" should a temp file ever be tampered with. It deliberately does
     * <em>not</em> allow-list classes – the fallback exists precisely to carry arbitrary user-supplied
     * {@link java.io.Serializable} cell types.
     */
    private static final ObjectInputFilter DESERIALIZATION_LIMITS =
            ObjectInputFilter.Config.createFilter("maxbytes=16777216;maxdepth=64;maxrefs=100000;maxarray=1000000");

    private RowCodec() {}

    static void writeRow(DataOutputStream out, Row row) throws IOException {
        int n = row.size();
        out.writeInt(n);
        for (int i = 0; i < n; i++) {
            writeValue(out, row.get(i));
        }
    }

    static Row readRow(DataInputStream in) throws IOException {
        int n = in.readInt();
        Object[] values = new Object[n];
        for (int i = 0; i < n; i++) {
            values[i] = readValue(in);
        }
        return new Row(values);
    }

    private static void writeValue(DataOutputStream out, Object v) throws IOException {
        if (v == null) {
            out.writeByte(NULL);
        } else if (v instanceof String s) {
            out.writeByte(STRING);
            writeString(out, s);
        } else if (v instanceof Integer i) {
            out.writeByte(INT);
            out.writeInt(i);
        } else if (v instanceof Long l) {
            out.writeByte(LONG);
            out.writeLong(l);
        } else if (v instanceof Double d) {
            out.writeByte(DOUBLE);
            out.writeDouble(d);
        } else if (v instanceof Boolean b) {
            out.writeByte(BOOL);
            out.writeBoolean(b);
        } else if (v instanceof BigDecimal bd) {
            out.writeByte(BIGDEC);
            byte[] unscaled = bd.unscaledValue().toByteArray();
            out.writeInt(bd.scale());
            out.writeInt(unscaled.length);
            out.write(unscaled);
        } else if (v instanceof LocalDate ld) {
            out.writeByte(LDATE);
            out.writeLong(ld.toEpochDay());
        } else if (v instanceof LocalDateTime ldt) {
            out.writeByte(LDATETIME);
            out.writeLong(ldt.toLocalDate().toEpochDay());
            out.writeLong(ldt.toLocalTime().toNanoOfDay());
        } else if (v instanceof LocalTime lt) {
            out.writeByte(LTIME);
            out.writeLong(lt.toNanoOfDay());
        } else {
            out.writeByte(JAVA);
            writeJavaSerialized(out, v);
        }
    }

    private static Object readValue(DataInputStream in) throws IOException {
        byte tag = in.readByte();
        return switch (tag) {
            case NULL -> null;
            case STRING -> readString(in);
            case INT -> in.readInt();
            case LONG -> in.readLong();
            case DOUBLE -> in.readDouble();
            case BOOL -> in.readBoolean();
            case BIGDEC -> {
                int scale = in.readInt();
                byte[] unscaled = readBytes(in, in.readInt());
                yield new BigDecimal(new BigInteger(unscaled), scale);
            }
            case LDATE -> LocalDate.ofEpochDay(in.readLong());
            case LDATETIME -> LocalDateTime.of(
                    LocalDate.ofEpochDay(in.readLong()), LocalTime.ofNanoOfDay(in.readLong()));
            case LTIME -> LocalTime.ofNanoOfDay(in.readLong());
            case JAVA -> readJavaSerialized(in);
            default -> throw new IOException("Unknown RowCodec type tag: " + tag);
        };
    }

    private static void writeString(DataOutputStream out, String s) throws IOException {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String readString(DataInputStream in) throws IOException {
        return new String(readBytes(in, in.readInt()), StandardCharsets.UTF_8);
    }

    private static byte[] readBytes(DataInputStream in, int length) throws IOException {
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return bytes;
    }

    private static void writeJavaSerialized(DataOutputStream out, Object v) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(buffer)) {
            oos.writeObject(v);
        } catch (NotSerializableException e) {
            throw new IOException(
                    "Cell value of type " + v.getClass().getName() + " is not Serializable - with sortBy(...)"
                            + " all cell values must be Serializable, because sorted runs are spilled to temp files",
                    e);
        }
        byte[] bytes = buffer.toByteArray();
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static Object readJavaSerialized(DataInputStream in) throws IOException {
        byte[] bytes = readBytes(in, in.readInt());
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            ois.setObjectInputFilter(DESERIALIZATION_LIMITS);
            return ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Deserialization failed", e);
        }
    }
}
