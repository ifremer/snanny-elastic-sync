package fr.ifremer.sensornanny.sync;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class MainTest {

    @Test
    public void testMain() {
        Calendar instance = Calendar.getInstance();
        Date to = instance.getTime();
        instance.add(Calendar.MONTH, -1);
        Main.execute(instance.getTime(), to);
    }
}
