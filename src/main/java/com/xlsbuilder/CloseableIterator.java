package com.xlsbuilder;

import java.io.Closeable;
import java.util.Iterator;

/** Iterator, der Ressourcen hält (z. B. offene Run-Dateien) und ohne checked Exception schließt. */
public interface CloseableIterator<T> extends Iterator<T>, Closeable {
    @Override
    void close();
}
