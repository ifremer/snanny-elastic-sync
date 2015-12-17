package fr.ifremer.sensornanny.sync.util;

import org.junit.Assert;
import org.junit.Test;

public class UrlUtilsTest {

    @Test
    public void testUriParse() {
        String result = UrlUtils.parse("http://www.ifremer.fr/tematres/vocab/index.php?tema=107", "tema");
        Assert.assertEquals("107", result);

        result = UrlUtils.parse("http://www.ifremer.fr/tematres/vocab/index.php?tema=108&val1=23", "tema");
        Assert.assertEquals("108", result);

        result = UrlUtils.parse("http://www.ifremer.fr/tematres/vocab/index.php?testBefore=3°é&tema=110", "tema");
        Assert.assertEquals("110", result);

        result = UrlUtils.parse("http://www.ifremer.fr/tematres/vocab/index.php?tem=107", "tema");
        Assert.assertNull(result);

    }
}
