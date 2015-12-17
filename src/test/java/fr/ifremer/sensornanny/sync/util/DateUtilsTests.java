package fr.ifremer.sensornanny.sync.util;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import fr.ifremer.sensornanny.sync.base.UnitTest;

public class DateUtilsTests extends UnitTest {

    @Test
    public void testParseSimpleDate() {
        Date result = DateUtils.parse("2013-09-05");
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        Assert.assertEquals(2013, cal.get(Calendar.YEAR));
        Assert.assertEquals(8, cal.get(Calendar.MONTH));
        Assert.assertEquals(5, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testParseCompleteDate() {
        Date result = DateUtils.parse("2014-04-04T08:30:29.021042");
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        Assert.assertEquals(2014, cal.get(Calendar.YEAR));
        Assert.assertEquals(3, cal.get(Calendar.MONTH));
        Assert.assertEquals(4, cal.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(8, cal.get(Calendar.HOUR_OF_DAY));
        Assert.assertEquals(30, cal.get(Calendar.MINUTE));
        Assert.assertEquals(29, cal.get(Calendar.SECOND));
        Assert.assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    public void testParseDateNetCdf() {
        Date randomDateTransform = DateUtils.randomDateTransform(41300.255444d);
        Calendar instance = Calendar.getInstance();
        instance.setTime(randomDateTransform);
        Assert.assertEquals(26, instance.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(0, instance.get(Calendar.MONTH));
        Assert.assertEquals(2013, instance.get(Calendar.YEAR));
    }

}
