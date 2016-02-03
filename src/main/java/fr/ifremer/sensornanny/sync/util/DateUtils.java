package fr.ifremer.sensornanny.sync.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class DateUtils {
    private static final double START_TIME_NETCDF = 2209165199011d;
    private static final int HOURS_IN_MILLIS = 24 * 60 * 60 * 1000;
    private static final String ISO_DATETIME_8601 = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String ISO_DATE_8601 = "yyyy-MM-dd";
    private static final String ISO_DATETIME = "yyyy-MM-dd HH:mm:ss";

    private static ThreadLocal<Map<String, SimpleDateFormat>> formatter = new ThreadLocal<Map<String, SimpleDateFormat>>() {

        protected Map<String, SimpleDateFormat> initialValue() {
            Map<String, SimpleDateFormat> map = new HashMap<>();
            map.put(ISO_DATETIME_8601, new SimpleDateFormat(ISO_DATETIME_8601));
            map.put(ISO_DATE_8601, new SimpleDateFormat(ISO_DATE_8601));
            map.put(ISO_DATETIME, new SimpleDateFormat(ISO_DATETIME));
            return map;
        }

    };

    public static Date parse(String date) {
        Date ret = null;
        Collection<SimpleDateFormat> list = formatter.get().values();
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

    public static String formatDateTime(Date date) {
        if (date == null) {
            return StringUtils.EMPTY;
        }
        return formatter.get().get(ISO_DATETIME).format(date);
    }

}
