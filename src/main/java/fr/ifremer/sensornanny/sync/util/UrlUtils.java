package fr.ifremer.sensornanny.sync.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UrlUtils {

    private static final String DEFAULT_CONTENT_CHARSET = "UTF-8";
    private static final String PARAMETER_SEPARATOR = "&";
    private static final String NAME_VALUE_SEPARATOR = "=";

    /**
     * Returns a list of {@link NameValuePair NameValuePairs} as built from the
     * URI's query portion. For example, a URI of
     * http://example.org/path/to/file?a=1&b=2&c=3 would return a list of three
     * NameValuePairs, one for a=1, one for b=2, and one for c=3.
     * <p>
     * This is typically useful while parsing an HTTP PUT.
     * 
     * @param uri
     *            uri to parse
     * @param encoding
     *            encoding to use while parsing the query
     */
    public static String parse(String url, String field) {
        Map<String, String> result = new HashMap<>();
        URI uri = URI.create(url);
        String query = uri.getRawQuery();
        if (query != null && query.length() > 0) {
            parse(result, new Scanner(query));
        }
        return result.get(field);
    }

    /**
     * Returns a list of {@link NameValuePair NameValuePairs} as built from the
     * URI's query portion. For example, a URI of
     * http://example.org/path/to/file?a=1&b=2&c=3 would return a list of three
     * NameValuePairs, one for a=1, one for b=2, and one for c=3.
     * <p>
     * This is typically useful while parsing an HTTP PUT.
     * 
     * @param uri
     *            uri to parse
     * @param encoding
     *            encoding to use while parsing the query
     */
    public static Map<String, String> parse(String url) {
        Map<String, String> result = new HashMap<>();
        URI uri = URI.create(url);
        String query = uri.getRawQuery();
        if (query != null && query.length() > 0) {
            parse(result, new Scanner(query));
        }
        return result;
    }

    /**
     * Adds all parameters within the Scanner to the list of
     * <code>parameters</code>, as encoded by <code>encoding</code>. For
     * example, a scanner containing the string <code>a=1&b=2&c=3</code> would
     * add the {@link NameValuePair NameValuePairs} a=1, b=2, and c=3 to the
     * list of parameters.
     * 
     * @param parameters
     *            List to add parameters to.
     * @param scanner
     *            Input that contains the parameters to parse.
     * @param encoding
     *            Encoding to use when decoding the parameters.
     */
    private static void parse(Map<String, String> parameters, Scanner scanner) {
        scanner.useDelimiter(PARAMETER_SEPARATOR);
        while (scanner.hasNext()) {
            final String[] nameValue = scanner.next().split(NAME_VALUE_SEPARATOR);
            if (nameValue.length == 2) {
                String name = decode(nameValue[0], DEFAULT_CONTENT_CHARSET);
                String value = null;
                if (nameValue.length == 2) {
                    value = decode(nameValue[1], DEFAULT_CONTENT_CHARSET);
                }
                parameters.put(name, value);
            }
        }
    }

    private static String decode(String content, String encoding) {
        try {
            return URLDecoder.decode(content, encoding != null ? encoding : DEFAULT_CONTENT_CHARSET);
        } catch (UnsupportedEncodingException problem) {
            throw new IllegalArgumentException(problem);
        }
    }
}
