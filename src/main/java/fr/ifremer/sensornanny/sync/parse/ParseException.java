package fr.ifremer.sensornanny.sync.parse;

/**
 * Parse exception while parsing xml or json content
 * 
 * @author athorel
 *
 */
public class ParseException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ParseException(String message, Throwable e) {
        super(message, e);
    }

}
