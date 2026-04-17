package ch.mvurdorf.platform.events;

import ch.mvurdorf.platform.utils.DateUtil;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
        append(sb, "PRODID:-//MV Harmonie Urdorf//MVU Platform//DE");
        append(sb, "CALSCALE:GREGORIAN");
        append(sb, "METHOD:PUBLISH");
        append(sb, "X-WR-CALNAME:MV Harmonie Urdorf");

        for (var event : events) {
            appendEvent(sb, event, dtstamp);
        }

        append(sb, "END:VCALENDAR");
        return sb.toString();
    }

    private static void appendEvent(StringBuilder sb, EventDto event, String dtstamp) {
        append(sb, "BEGIN:VEVENT");
        append(sb, "UID:mvu-event-%d@mvurdorf.ch".formatted(event.id()));
        append(sb, "DTSTAMP:" + dtstamp);

        if (event.fromTime() != null) {
            append(sb, "DTSTART:" + formatDateTime(event.fromDate(), event.fromTime()));
            var endDate = event.toDate() != null ? event.toDate() : event.fromDate();
            var endTime = event.toTime() != null ? event.toTime() : event.fromTime().plusHours(2);
            append(sb, "DTEND:" + formatDateTime(endDate, endTime));
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
        var utc = ZonedDateTime.of(date, time, DateUtil.ZONE_ID)
                               .withZoneSameInstant(ZoneOffset.UTC);
        return utc.format(DATE_FORMAT) + "T" + utc.format(TIME_FORMAT) + "Z";
    }

    static String formatDate(LocalDate date) {
        return date.format(DATE_FORMAT);
    }

    private static void append(StringBuilder sb, String line) {
        sb.append(line).append("\r\n");
    }
}
