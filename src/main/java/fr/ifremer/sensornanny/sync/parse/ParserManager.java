package fr.ifremer.sensornanny.sync.parse;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

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

    private static final Logger logger = Logger.getLogger(ParserManager.class.getName());
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
            }else{
		logger.warning("No modulo configuration for parser: " + parser.getClass());
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
        logger.info("Searching parser for type " + data.getMimeType());
        for (IObservationParser parser : parsers) {
            if (parser.accept(data)) {		
                logger.info("Parser found for " + data.getMimeType() + " : " + parser.getClass());
                return parser;
            }
        }
        logger.warning("No parser found for " + data.getMimeType());
        return null;
    }

}
