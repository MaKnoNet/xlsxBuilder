package com.xlsbuilder;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

/** Factory für gängige {@link DataProvider}-Adapter. */
public final class DataProviders {

    private DataProviders() {
    }

    public static <T> DataProvider<T> ofIterator(Iterator<? extends T> iterator) {
        return new DataProvider<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                if (!iterator.hasNext()) {
                    throw new NoSuchElementException();
                }
                return iterator.next();
            }
        };
    }

    public static <T> DataProvider<T> ofIterable(Iterable<? extends T> iterable) {
        return ofIterator(iterable.iterator());
    }

    /** Adaptiert einen {@link Stream}; der Stream wird bei {@link DataProvider#close()} geschlossen. */
    public static <T> DataProvider<T> ofStream(Stream<? extends T> stream) {
        Iterator<? extends T> iterator = stream.iterator();
        return new DataProvider<T>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                return iterator.next();
            }

            @Override
            public void close() {
                stream.close();
            }
        };
    }
}
