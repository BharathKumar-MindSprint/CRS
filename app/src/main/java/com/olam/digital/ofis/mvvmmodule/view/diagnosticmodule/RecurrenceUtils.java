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
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date startDate = inputDateFormat.parse(startDateStr);
        Date currentDate = new Date();

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);

        int recurrenceCount = 0;

        switch (recurrenceType.toLowerCase()) {
            case "days":
                long daysDiff = (currentDate.getTime() - startDate.getTime()) / (24L * 60 * 60 * 1000);
                recurrenceCount = (int) (daysDiff / recurrenceFrequency);
                break;
            case "weeks":
                long weeksDiff = (currentCalendar.getTimeInMillis() - startCalendar.getTimeInMillis())
                        / (7L * 24 * 60 * 60 * 1000);
                recurrenceCount = (int) (weeksDiff / recurrenceFrequency);
                break;
            case "months":
                int monthsDiff = (currentCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)) * 12
                        + currentCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH);

                if (currentCalendar.get(Calendar.DAY_OF_MONTH) < startCalendar.get(Calendar.DAY_OF_MONTH)) {
                    monthsDiff--;
                }

                recurrenceCount = monthsDiff / recurrenceFrequency;
                break;
            case "years":
                int yearsDiff = currentCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR);

                if (currentCalendar.get(Calendar.MONTH) < startCalendar.get(Calendar.MONTH)
                        || (currentCalendar.get(Calendar.MONTH) == startCalendar.get(Calendar.MONTH)
                        && currentCalendar.get(Calendar.DAY_OF_MONTH) < startCalendar.get(Calendar.DAY_OF_MONTH))) {
                    yearsDiff--;
                }

                recurrenceCount = yearsDiff / recurrenceFrequency;
                break;
            default:
                System.out.println("Unsupported recurrence frequency");
        }

        return recurrenceCount;
    }

    public static Date getNewStartDate(String startDate, String recurrenceType, int recurrenceFrequency,
                                        int recurrenceCount) throws ParseException {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        inputDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(inputDateFormat.parse(startDate));

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
            if ("days".equalsIgnoreCase(recurrenceType)) {
                calendar.add(Calendar.DAY_OF_MONTH, recurrenceFrequency);
            } else if ("months".equalsIgnoreCase(recurrenceType)) {
                calendar.add(Calendar.MONTH, recurrenceFrequency);
            } else if ("weeks".equalsIgnoreCase(recurrenceType)) {
                calendar.add(Calendar.WEEK_OF_MONTH, recurrenceFrequency);
            } else if ("years".equalsIgnoreCase(recurrenceType)) {
                calendar.add(Calendar.YEAR, recurrenceFrequency);
            }

            if (calendar.getTime().after(endDate)) {
                if ("days".equalsIgnoreCase(recurrenceType)) {
                    calendar.add(Calendar.DAY_OF_MONTH, -recurrenceFrequency);
                } else if ("months".equalsIgnoreCase(recurrenceType)) {
                    calendar.add(Calendar.MONTH, -recurrenceFrequency);
                } else if ("weeks".equalsIgnoreCase(recurrenceType)) {
                    calendar.add(Calendar.WEEK_OF_MONTH, -recurrenceFrequency);
                } else if ("years".equalsIgnoreCase(recurrenceType)) {
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
