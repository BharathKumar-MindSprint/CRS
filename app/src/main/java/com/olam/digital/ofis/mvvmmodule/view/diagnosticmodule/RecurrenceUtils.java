package com.olam.digital.ofis.mvvmmodule.view.diagnosticmodule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RecurrenceUtils {
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int calculateRecurrenceCount(String startDateStr, String recurrenceType, int recurrenceFrequency)
            throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(startDateStr);
        Date currentDate = new Date();

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);

        int recurrenceCount = 0;

        switch (recurrenceType.toLowerCase()) {
            case "days":
                long daysDiff = (currentDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000);
                recurrenceCount = (int) (daysDiff / recurrenceFrequency);
                break;
            case "weeks":
                long weeksDiff = (currentCalendar.getTimeInMillis() - startCalendar.getTimeInMillis())
                        / (7 * 24 * 60 * 60 * 1000);
                recurrenceCount = (int) (weeksDiff / recurrenceFrequency);
                break;
            case "months":
                int monthsDiff = (currentCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)) * 12
                        + currentCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);
                recurrenceCount = monthsDiff / recurrenceFrequency;
                break;
            case "years":
                int yearsDiff = currentCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);
                recurrenceCount = yearsDiff / recurrenceFrequency;
                break;
            default:
                System.out.println("Unsupported recurrence frequency");
        }

//        recurrenceCount++;
        return recurrenceCount;
    }

    public static Date getNewStartDate(String startDate, String recurrenceType, int recurrenceFrequency,
                                        int recurrenceCount) throws ParseException {
//        recurrenceCount--;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(sdf.parse(startDate));

        switch (recurrenceType.toLowerCase()) {
            case "days":
                startCalendar.add(Calendar.DAY_OF_MONTH, recurrenceFrequency * recurrenceCount);
                break;
            case "weeks":
                startCalendar.add(Calendar.WEEK_OF_MONTH, recurrenceFrequency * recurrenceCount);
                break;
            case "months":
                startCalendar.add(Calendar.MONTH, recurrenceFrequency * recurrenceCount);
                break;
            case "years":
                startCalendar.add(Calendar.YEAR, recurrenceFrequency * recurrenceCount);
                break;
            default:
                System.out.println("Unsupported recurrence frequency");
        }

        return startCalendar.getTime();
    }

    public static Date getNewEndDate(Date newStartDate, String recurrenceType, int recurrenceFrequency) {
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(newStartDate);

        switch (recurrenceType.toLowerCase()) {
            case "days":
                endCalendar.add(Calendar.DAY_OF_MONTH, recurrenceFrequency);
                break;
            case "weeks":
                endCalendar.add(Calendar.WEEK_OF_MONTH, recurrenceFrequency);
                break;
            case "months":
                endCalendar.add(Calendar.MONTH, recurrenceFrequency);
                break;
            case "years":
                endCalendar.add(Calendar.YEAR, recurrenceFrequency);
                break;
            default:
                System.out.println("Unsupported recurrence frequency");
        }

        return endCalendar.getTime();
    }

    public static Date findLastAttendedDate(Date startDate, Date endDate, String recurrenceType,
                                            int recurrenceFrequency, int recurrenceCount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        // Increment based on the recurrence type and frequency
        for (int i = 0; i < recurrenceCount; i++) {
            if ("days".equals(recurrenceType)) {
                calendar.add(Calendar.DAY_OF_MONTH, recurrenceFrequency);
            } else if ("months".equals(recurrenceType)) {
                calendar.add(Calendar.MONTH, recurrenceFrequency);
            } else if ("weeks".equals(recurrenceType)) {
                calendar.add(Calendar.WEEK_OF_MONTH, recurrenceFrequency);
            } else if ("years".equals(recurrenceType)) {
                calendar.add(Calendar.YEAR, recurrenceFrequency);
            }

            // Check if the current date is after the old recurrence end date
            if (calendar.getTime().after(endDate)) {
                // Subtract one recurrence interval to get the last attended date
                if ("days".equals(recurrenceType)) {
                    calendar.add(Calendar.DAY_OF_MONTH, -recurrenceFrequency);
                } else if ("months".equals(recurrenceType)) {
                    calendar.add(Calendar.MONTH, -recurrenceFrequency);
                } else if ("weeks".equals(recurrenceType)) {
                    calendar.add(Calendar.WEEK_OF_MONTH, -recurrenceFrequency);
                } else if ("years".equals(recurrenceType)) {
                    calendar.add(Calendar.YEAR, -recurrenceFrequency);
                }

                return calendar.getTime();
            }
        }

        // Return the start date if no recurrence has been attended
        return startDate;
    }

    public static boolean areDatesEqual(Date date1, Date date2) {
        // Check if the dates are equal
        return date1 != null && date2 != null && !date1.equals(date2);
    }
}
