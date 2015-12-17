package fr.ifremer.sensornanny.sync.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

public class DateUtils {
    private static final double START_TIME_NETCDF = 2209165199011d;
    private static final int HOURS_IN_MILLIS = 24 * 60 * 60 * 1000;
    private static final String ISO_DATETIME_8601 = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String ISO_DATE_8601 = "yyyy-MM-dd";

    private static ThreadLocal<List<SimpleDateFormat>> formatter = new ThreadLocal<List<SimpleDateFormat>>() {

        protected List<SimpleDateFormat> initialValue() {
            return Lists.newArrayList(
                    //
                    new SimpleDateFormat(ISO_DATETIME_8601),
                    //
                    new SimpleDateFormat(ISO_DATE_8601));
        }

    };

    public static Date parse(String date) {
        Date ret = null;
        List<SimpleDateFormat> list = formatter.get();
        for (SimpleDateFormat simpleDateFormat : list) {
            ret = safeParse(date, simpleDateFormat);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    /**
     * Safe parse date with a simple format
     * 
     * @param date date to parse
     * @param format de parsing de la date
     * @return date si le pattern correspond sinon <code>null</code>
     */
    private static Date safeParse(String date, SimpleDateFormat format) {
        try {
            return format.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date randomDateTransform(Double daysSinceStart) {
        return new Date((long) (daysSinceStart * HOURS_IN_MILLIS - START_TIME_NETCDF));
    }

}
