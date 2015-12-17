package fr.ifremer.sensornanny.sync.reader;

import java.util.Iterator;

public class FifoReader {

    Iterator<?> iterator = null;

    public FifoReader(Iterator<?> iterator) {
        this.iterator = iterator;
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T read() {
        if (iterator.hasNext()) {
            return (T) iterator.next();
        }
        return null;
    }

}
