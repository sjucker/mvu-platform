package ch.mvurdorf.platform.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static java.util.Locale.GERMAN;

public class FormatUtil {

    public static final Locale LOCALE = Locale.of("de", "CH");
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Zurich");

    public static final String DATE_FORMAT = "dd.MM.yyyy";
    public static final String DATE_FORMAT_SHORT = "dd.MM.yy";
    public static final String TIME_FORMAT = "HH:mm";
    private static final String DATETIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;

    private FormatUtil() {
    }

    public static String formatDate(LocalDate date) {
        return formatDate(date, DATE_FORMAT);
    }

    public static String formatDate(LocalDate date, String format) {
        if (date == null) {
            return "-";
        }
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    public static String formatTime(LocalTime time) {
        if (time == null) {
            return "-";
        }
        return time.format(DateTimeFormatter.ofPattern(TIME_FORMAT));
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
    }

    public static String formatInstant(Instant instant) {
        if (instant == null) {
            return "-";
        }
        return DateTimeFormatter.ofPattern(DATETIME_FORMAT).withZone(ZONE_ID).format(instant);
    }

    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "-";
        }
        return NumberFormat.getCurrencyInstance(LOCALE).format(amount);
    }

    public static String dayOfWeek(LocalDate date) {
        if (date == null) {
            return "";
        }
        return DateTimeFormatter.ofPattern("EE", GERMAN).format(date);
    }

}
