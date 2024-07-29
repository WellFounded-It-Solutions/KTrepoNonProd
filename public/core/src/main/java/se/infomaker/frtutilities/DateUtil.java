package se.infomaker.frtutilities;

import android.content.Context;
import android.text.format.DateUtils;

import android.util.Log;
import com.navigaglobal.mobile.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class DateUtil {

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        SIMPLE_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm");

    private static final Map<String, DateFormat> DATE_FORMATS = new HashMap<>();

    private static DateFormat getDateFormat(String format) {
        String key = Locale.getDefault().toString() + format;
        if (DATE_FORMATS.containsKey(key)) {
            return DATE_FORMATS.get(key);
        }
        synchronized (DATE_FORMATS) {
            try {
                if (!DATE_FORMATS.containsKey(key)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
                    DATE_FORMATS.put(key, dateFormat);
                }
            } catch (Exception e) {
                Timber.e(e, "Failed to create format");
            }
        }
        return DATE_FORMATS.get(key);
    }

    public static Date getDateFromString(String dateString) {
        try {
            return SIMPLE_DATE_FORMAT.parse(dateString);
        } catch (ParseException e) {
            Timber.e(e, "Failed to parse date form string: " + dateString);
        }
        return null;
    }

    public static String formatDateString(String dateString, String outFormat) {
        return formatDateString(null, dateString, outFormat);
    }

    public static String formatDateStringWithoutTimeAgo(Context context,String dateString, String outFormat) {
        Log.d(DateUtil.class.getSimpleName(),"inside formatDateStringWithoutTimeAgo");
        if (dateString != null && !dateString.isEmpty()) {
            Date date = getDateFromString(dateString);
            if (date == null) {
                return dateString;
            }

            if (outFormat != null) {
                DateFormat dateFormat = getDateFormat(outFormat);
                if (dateFormat != null) {

                    String finalDateFormat=dateFormat.format(date);
                    if(outFormat.startsWith("EEE")){
                       if(finalDateFormat==null||finalDateFormat.length()==0)
                           return finalDateFormat;
                       return finalDateFormat.substring(0,1).toUpperCase()+finalDateFormat.substring(1);
                    }
                    return finalDateFormat;
                }
            }
            return DATE_FORMAT.format(date);
        }
        return null;

    }

    public static String formatDateString(Context context, String dateString, String outFormat) {

        if (dateString != null && !dateString.isEmpty()) {
            Date date = getDateFromString(dateString);
            if (date == null) {
                return dateString;
            }
            return context != null ? timeAgoSince(context, System.currentTimeMillis(), date, outFormat) : timeAgoSince(date);
        }
        return null;
    }

    public static String formatDateString(String dateString) {
        if (dateString != null && !dateString.isEmpty()) {
            Date date = getDateFromString(dateString);
            if (date != null) {
                return timeAgoSince(date);
            }
        }
        return null;
    }

    public static String timeAgoSince(Context context, Date date) {
        return timeAgoSince(context, System.currentTimeMillis(), date);
    }

    private static boolean isLeapYear(int year) {
        boolean isLeapYear = false;
        if(year % 4 == 0) {
            if(year % 100 == 0) {
                isLeapYear = year % 400 == 0;
            } else {
                isLeapYear = true;
            }
        }
        return isLeapYear;
    }

    public static String timeAgoSince(Context context, long now, Date date) {
        return timeAgoSince(context, now, date, null);
    }

    /**
     * Allow testing
     *
     * @param context
     * @param now
     * @param date
     * @return
     */
    public static String timeAgoSince(Context context, long now, Date date, String outFormat) {
        if (isSameDay(now, date.getTime())) {
            long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(now - date.getTime());
            if (diffMinutes < 1) {
                return context.getString(R.string.now);
            } else if (diffMinutes == 1) {
                return context.getString(R.string.oneMinuteAgo);
            } else if (diffMinutes < 60) {
                return context.getString(R.string.minutes_ago, diffMinutes);
            } else {
                return context.getString(R.string.today_format, TIME_FORMAT.format(date));
            }
        } else {

            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTimeInMillis(now);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            Calendar yesterdayCalendar = Calendar.getInstance();
            yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1);

            Calendar weekCalendar = Calendar.getInstance();
            weekCalendar.setTimeInMillis(now);
            weekCalendar.add(Calendar.WEEK_OF_YEAR, -1);

            long diffDay = getNumOfDaysBetweenDates(currentCalendar, calendar);
            if (weekCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && diffDay <= 6) {
                return getWeekday(context, date) + " " + TIME_FORMAT.format(date);
            }
            if (outFormat != null) {
                DateFormat dateFormat = getDateFormat(outFormat);
                if (dateFormat != null) {
                    return dateFormat.format(date);
                }
            }
            return DATE_FORMAT.format(date);
        }
    }

    @Deprecated
    public static String timeAgoSince(Date date) {
        if (DateUtils.isToday(date.getTime())) {
            long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - date.getTime());
            if (diffMinutes < 1) {
                return "Nu";
            } else if (diffMinutes == 1) {
                return "En minut sedan";
            } else if (diffMinutes < 60) {
                return diffMinutes + " minuter sedan";
            } else {
                return "Idag " + TIME_FORMAT.format(date);
            }
        } else {

            Calendar currentCalendar = Calendar.getInstance();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            Calendar yesterdayCalendar = Calendar.getInstance();
            yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1);

            Calendar weekCalendar = Calendar.getInstance();
            weekCalendar.add(Calendar.WEEK_OF_YEAR, -1);

            long diffDay = getNumOfDaysBetweenDates(currentCalendar, calendar);
            if (weekCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) && diffDay <= 6) {
                String day = getWeekday(calendar.get(Calendar.DAY_OF_WEEK));
                return day + " " + TIME_FORMAT.format(date);
            } else {
                return DATE_FORMAT.format(date);
            }
        }
    }

    private static long getNumOfDaysBetweenDates(Calendar currentCalendar, Calendar calendar) {
        int daysInYear = isLeapYear(calendar.get(Calendar.DAY_OF_YEAR)) ? 366 : 365;
        long diffDay = currentCalendar.get(Calendar.DAY_OF_YEAR) - calendar.get(Calendar.DAY_OF_YEAR);
        if (diffDay < 0) {
            diffDay = daysInYear - (calendar.get(Calendar.DAY_OF_YEAR) - currentCalendar.get(Calendar.DAY_OF_YEAR));
        }
        return diffDay;
    }

    private static String getWeekday(Context context, Date date ) {
        Locale locale;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        }
        else {
            locale = context.getResources().getConfiguration().locale;
        }
        return capitalizeFirstLetter(new SimpleDateFormat("EEEE", locale).format(date));
    }

    private static String getWeekday(int dayOfWeek) {

        switch (dayOfWeek) {
            case 1:
                return "Söndag";
            case 2:
                return "Måndag";
            case 3:
                return "Tisdag";
            case 4:
                return "Onsdag";
            case 5:
                return "Torsdag";
            case 6:
                return "Fredag";
            case 7:
                return "Lördag";
            default:
                return "";
        }
    }

    /**
     * @param first day
     * @param second day
     * @return true if first and second is the same day
     */
    private static boolean isSameDay(long first, long second) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(first);
        cal2.setTimeInMillis(second);
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    private static String capitalizeFirstLetter(String original) {
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
}
