package fr.ifremer.sensornanny.sync.parse;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

import fr.ifremer.sensornanny.observation.parser.IObservationParser;
import fr.ifremer.sensornanny.observation.parser.ObservationData;
import fr.ifremer.sensornanny.sync.config.Config;
import fr.ifremer.sensornanny.sync.util.JarLoader;

/**
 * This class handle the concrete parsers for the elastic sync system
 * 
 * @author athorel
 *
 */
public class ParserManager {

    /**
     * List of parser loaded
     */
    private Set<IObservationParser> parsers = new HashSet<>();

    /**
     * Method that allow to retrieve parsers
     */
    @Inject
    public void initialiseParsers() {
        // Discover jars
        URL[] jars = JarLoader.discoverJars(Config.syncParserLib());
        // find parsers
        List<IObservationParser> foundParsers = JarLoader.scanForInterfaces(jars, IObservationParser.class,
                "fr.ifremer");
        for (IObservationParser parser : foundParsers) {
            if (Config.moduloForParser(parser.getClass()) > -1) {
                parsers.add(parser);
            }
        }
    }

    /**
     * Return the parser for an observation data
     * 
     * @param data data to parse
     * @return parser which accept this format, if none return <code>null</code>
     */
    public IObservationParser getParser(ObservationData data) {
        for (IObservationParser parser : parsers) {
            if (parser.accept(data)) {
                return parser;
            }
        }
        return null;
    }

}
