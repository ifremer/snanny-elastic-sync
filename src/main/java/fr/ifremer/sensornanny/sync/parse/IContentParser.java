package fr.ifremer.sensornanny.sync.parse;

import java.io.InputStream;

/**
 * Interface for a content parser
 * 
 * @author athorel
 *
 * @param <T> type of element returned by the parser
 */
public interface IContentParser<T> {

    T parse(InputStream stream) throws Exception;

    String getType();
}
