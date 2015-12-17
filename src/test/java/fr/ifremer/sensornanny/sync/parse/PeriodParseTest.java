package fr.ifremer.sensornanny.sync.parse;

import java.time.Duration;
import java.time.Instant;

import org.junit.Assert;
import org.junit.Test;

import fr.ifremer.sensornanny.sync.base.UnitTest;

public class PeriodParseTest extends UnitTest {

    @Test
    public void testParseDefaultPeriod() {

        Duration fiveDays = Duration.parse("P5dT3H4M5S");
        Instant today = Instant.now();
        long realPeriod =
        // 5 Days
        (5 * 24 * 60 * 60) +
                // 3 Hours
                (3 * 60 * 60) +
                // 4 Minutes
                (4 * 60)
                // 5 secondes
                + 5;

        Assert.assertEquals(realPeriod, fiveDays.getSeconds());
        Instant result = (Instant) fiveDays.subtractFrom(today);

        Instant resultReal = today.minusSeconds(realPeriod);
        System.out.println(result);
        System.out.println(resultReal);
    }
}
