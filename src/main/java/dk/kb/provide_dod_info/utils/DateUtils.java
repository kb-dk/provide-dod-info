package dk.kb.provide_dod_info.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

    private static final LocalDate date = LocalDate.now(ZoneId.of("Europe/Copenhagen"));
    private static final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd", Locale.ROOT);
    private static final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM", Locale.ROOT);
    private static final DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy", Locale.ROOT);

    /**
     *
     * @return the current day as a string wit two digits, e.g. 01, 31
     */
    public static String getDay() {
        return date.format(dayFormatter);
    }

    /**
     *
     * @return the current month as a string with two digits, e.g. 01, 06, 12
     */
    public static String getMonth() {
        return date.format(monthFormatter);
    }

    /**
     *
     * @return The current year as a String e.g. 2021
     */
    public static String getYear() {
        return date.format(yearFormatter);
    }

    /**
     *
     * @return The current date as a string with the format yyyyMMdd
     */
    public static String getDate(){
      return getYear() + getMonth() + getDay();
    }
}
