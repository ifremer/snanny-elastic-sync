package fr.ifremer.sensornanny.sync.parse.observations;

import java.io.InputStream;
import java.util.function.Consumer;

public interface IObservationParser<T> {

    /**
     * This method allow to read element
     * 
     * @param fileName name of the file
     * @param stream of the file
     * @param consumer Consumer of parser
     * 
     */
    void read(String fileName, InputStream stream, Consumer<T> consumer);

    /**
     * This method verify if a parser accept the file
     * 
     * @param role of the file
     * @return <code>true</code> if the file is accepted by the parser otherwise <code>false</code>
     */
    boolean accept(String fileName);
}
