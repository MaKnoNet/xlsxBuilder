package de.makno.xlsxbuilder;

import java.io.Closeable;
import java.util.Iterator;

/** Iterator that holds resources (e.g. open run files) and closes without a checked exception. */
public interface CloseableIterator<T> extends Iterator<T>, Closeable {
    @Override
    void close();
}
