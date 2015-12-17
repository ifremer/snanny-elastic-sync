package fr.ifremer.sensornanny.sync.parse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public final class ParseUtil {

    private ParseUtil() {

    }

    public static <T> T parse(IContentParser<T> parser, String data) throws ParseException {
        if (StringUtils.isNotBlank(data)) {
            InputStream inputStream = new ByteArrayInputStream(data.getBytes());
            try {
                return parser.parse(inputStream);
            } catch (Exception e) {
                throw new ParseException(String.format("[%s]Unable to parse element", parser.getType()), e);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }

        }
        throw new ParseException(String.format("[%s]Nothing to parse", parser.getType()), null);
    }

}
