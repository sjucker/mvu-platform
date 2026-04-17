package ch.mvurdorf.platform.events;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ICalUtil {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter DTSTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private ICalUtil() {
    }

    public static String buildIcal(List<EventDto> events) {
        var dtstamp = DTSTAMP_FORMAT.format(Instant.now());
        var sb = new StringBuilder();
        append(sb, "BEGIN:VCALENDAR");
        append(sb, "VERSION:2.0");
        append(sb, "PRODID:-//MV Harmonie Urdorf//DE");
        append(sb, "METHOD:PUBLISH");
        append(sb, "NAME:MV Harmonie Urdorf");
        append(sb, "X-WR-CALNAME:MV Harmonie Urdorf");
        append(sb, "CALSCALE:GREGORIAN");
        append(sb, "X-PUBLISHED-TTL:PT2H");
        append(sb, "REFRESH-INTERVAL;VALUE=DURATION:PT2H");
        appendVTimezone(sb);

        for (var event : events) {
            appendEvent(sb, event, dtstamp);
        }

        append(sb, "END:VCALENDAR");
        return sb.toString();
    }

    private static void appendVTimezone(StringBuilder sb) {
        append(sb, "BEGIN:VTIMEZONE");
        append(sb, "TZID:Europe/Zurich");
        append(sb, "BEGIN:STANDARD");
        append(sb, "DTSTART:19701025T020000");
        append(sb, "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10");
        append(sb, "TZOFFSETFROM:+0200");
        append(sb, "TZOFFSETTO:+0100");
        append(sb, "TZNAME:CET");
        append(sb, "END:STANDARD");
        append(sb, "BEGIN:DAYLIGHT");
        append(sb, "DTSTART:19700329T030000");
        append(sb, "RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3");
        append(sb, "TZOFFSETFROM:+0100");
        append(sb, "TZOFFSETTO:+0200");
        append(sb, "TZNAME:CEST");
        append(sb, "END:DAYLIGHT");
        append(sb, "END:VTIMEZONE");
    }

    private static void appendEvent(StringBuilder sb, EventDto event, String dtstamp) {
        append(sb, "BEGIN:VEVENT");
        append(sb, "UID:mvu-event-%d@mvurdorf.ch".formatted(event.id()));
        append(sb, "DTSTAMP:" + dtstamp);

        if (event.fromTime() != null) {
            append(sb, "DTSTART;TZID=Europe/Zurich:" + formatDateTime(event.fromDate(), event.fromTime()));
            var endDate = event.toDate() != null ? event.toDate() : event.fromDate();
            var endTime = event.toTime() != null ? event.toTime() : event.fromTime().plusHours(2);
            append(sb, "DTEND;TZID=Europe/Zurich:" + formatDateTime(endDate, endTime));
        } else {
            append(sb, "DTSTART;VALUE=DATE:" + formatDate(event.fromDate()));
            var endDate = (event.toDate() != null ? event.toDate() : event.fromDate()).plusDays(1);
            append(sb, "DTEND;VALUE=DATE:" + formatDate(endDate));
        }

        appendFolded(sb, "SUMMARY", event.title());
        if (event.location() != null) {
            appendFolded(sb, "LOCATION", event.location());
        }
        if (event.description() != null) {
            appendFolded(sb, "DESCRIPTION", event.description());
        }

        append(sb, "END:VEVENT");
    }

    static void appendFolded(StringBuilder sb, String property, String value) {
        var line = property + ":" + escapeText(value);
        while (line.length() > 75) {
            sb.append(line, 0, 75).append("\r\n ");
            line = line.substring(75);
        }
        sb.append(line).append("\r\n");
    }

    static String escapeText(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\r\n", "\\n")
                   .replace("\n", "\\n")
                   .replace(",", "\\,")
                   .replace(";", "\\;");
    }

    static String formatDateTime(LocalDate date, LocalTime time) {
        return date.format(DATE_FORMAT) + "T" + time.format(TIME_FORMAT);
    }

    static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    private static void append(StringBuilder sb, String line) {
        sb.append(line).append("\r\n");
    }
}
