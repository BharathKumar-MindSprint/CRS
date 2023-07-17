package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RecurrenceUtils {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

    public static String calculateEndDate(String startDate, int value, String unit) {
        Calendar calendar = Calendar.getInstance();
        try {
            DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
            Date parsedStartDate = DATE_FORMATTER.parse(startDate);
            calendar.setTime(parsedStartDate);

            if (unit.equalsIgnoreCase("days")) {
                calendar.add(Calendar.DAY_OF_MONTH, value);
            } else if (unit.equalsIgnoreCase("weeks")) {
                calendar.add(Calendar.WEEK_OF_MONTH, value);
            } else if (unit.equalsIgnoreCase("months")) {
                calendar.add(Calendar.MONTH, value);
            } else if (unit.equalsIgnoreCase("years")) {
                calendar.add(Calendar.YEAR, value);
            }

            Date endDate = calendar.getTime();
            return DATE_FORMATTER.format(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
