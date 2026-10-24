package com.example.smartpantry;


// this class parses expiry dates and calculates how soon they expire
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExpiryUtil {

    // Date format used for all expiry dates
    public static final String FORMAT = "yyyy-MM-dd";
    //Items expiring within this many days count as "expiring soon".
    public static final int SOON_DAYS = 3;

    // returns the parsed date, or null if the text is not a real yyyy-MM-dd date
    public static Date parse(String text) {
        // No text means no date
        if (text == null) return null;
        // Create a date parser for yyyy-MM-dd
        SimpleDateFormat f = new SimpleDateFormat(FORMAT, Locale.US);
        f.setLenient(false);   // rejects things like 2026-02-31
        // Try to turn the text into a date
        try {
            return f.parse(text.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    // midnight at the start of today, in milliseconds
    public static long startOfToday() {
        // Get the current date and time
        Calendar c = Calendar.getInstance();
        // Reset the time to midnight
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        // Return today's midnight in milliseconds
        return c.getTimeInMillis();
    }

    // whole days from today until the date (negative = already past)
    public static long daysUntil(String text) {
        // Convert the text to a date
        Date d = parse(text);
        // An invalid date gives a very large number
        if (d == null) return Long.MAX_VALUE;
        // Convert the time difference from milliseconds into days
        return Math.round((d.getTime() - startOfToday()) / 86400000.0);
    }
}
