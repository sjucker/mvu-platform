package ch.mvurdorf.platform.events;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static ch.mvurdorf.platform.events.ICalUtil.appendFolded;
import static ch.mvurdorf.platform.events.ICalUtil.escapeText;
import static ch.mvurdorf.platform.events.ICalUtil.formatDate;
import static ch.mvurdorf.platform.events.ICalUtil.formatDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class ICalUtilTest {

    private static final LocalDate DATE = LocalDate.of(2025, 6, 15);
    private static final LocalTime TIME = LocalTime.of(19, 30);

    @Test
    void emptyEventList() {
        var result = ICalUtil.buildIcal(List.of());

        assertThat(lines(result)).contains(
                "BEGIN:VCALENDAR",
                "VERSION:2.0",
                "END:VCALENDAR"
        );
        assertThat(result).doesNotContain("BEGIN:VEVENT");
    }

    @Test
    void allDayEvent() {
        var result = ICalUtil.buildIcal(List.of(event(1L, DATE, null, null, null, "Probe", null, null)));

        assertThat(lines(result)).contains(
                "DTSTART;VALUE=DATE:20250615",
                "DTEND;VALUE=DATE:20250616"
        );
    }

    @Test
    void allDayMultiDayEvent() {
        var toDate = DATE.plusDays(2);
        var result = ICalUtil.buildIcal(List.of(event(1L, DATE, null, toDate, null, "Lager", null, null)));

        assertThat(lines(result)).contains(
                "DTSTART;VALUE=DATE:20250615",
                "DTEND;VALUE=DATE:20250618"
        );
    }

    @Test
    void timedEventWithoutEndTime() {
        var result = ICalUtil.buildIcal(List.of(event(1L, DATE, TIME, null, null, "Konzert", null, null)));

        assertThat(lines(result)).contains(
                "DTSTART:20250615T193000",
                "DTEND:20250615T213000"
        );
    }

    @Test
    void timedEventWithEndTime() {
        var endTime = LocalTime.of(21, 45);
        var result = ICalUtil.buildIcal(List.of(event(1L, DATE, TIME, null, endTime, "Konzert", null, null)));

        assertThat(lines(result)).contains(
                "DTSTART:20250615T193000",
                "DTEND:20250615T214500"
        );
    }

    @Test
    void timedEventWithEndDate() {
        var toDate = DATE.plusDays(1);
        var endTime = LocalTime.of(0, 30);
        var result = ICalUtil.buildIcal(List.of(event(1L, DATE, TIME, toDate, endTime, "Event", null, null)));

        assertThat(lines(result)).contains(
                "DTSTART:20250615T193000",
                "DTEND:20250616T003000"
        );
    }

    @Test
    void eventWithLocationAndDescription() {
        var result = ICalUtil.buildIcal(List.of(event(1L, DATE, null, null, null, "Probe", "MZH Urdorf", "Bitte pünktlich")));

        assertThat(lines(result)).contains(
                "SUMMARY:Probe",
                "LOCATION:MZH Urdorf",
                "DESCRIPTION:Bitte pünktlich"
        );
    }

    @Test
    void eventWithoutLocationAndDescription() {
        var result = ICalUtil.buildIcal(List.of(event(1L, DATE, null, null, null, "Probe", null, null)));

        assertThat(result).doesNotContain("LOCATION:");
        assertThat(result).doesNotContain("DESCRIPTION:");
    }

    @Test
    void uid() {
        var result = ICalUtil.buildIcal(List.of(event(42L, DATE, null, null, null, "Probe", null, null)));

        assertThat(lines(result)).contains("UID:mvu-event-42@mvurdorf.ch");
    }

    @Test
    void escapeTextCommaAndSemicolon() {
        assertThat(escapeText("a,b;c")).isEqualTo("a\\,b\\;c");
    }

    @Test
    void escapeTextBackslash() {
        assertThat(escapeText("a\\b")).isEqualTo("a\\\\b");
    }

    @Test
    void escapeTextNewlines() {
        assertThat(escapeText("a\nb")).isEqualTo("a\\nb");
        assertThat(escapeText("a\r\nb")).isEqualTo("a\\nb");
    }

    @Test
    void lineFolding() {
        var sb = new StringBuilder();
        var longValue = "A".repeat(80);
        appendFolded(sb, "SUMMARY", longValue);

        var lines = lines(sb.toString());
        assertThat(lines[0]).hasSize(75);
        assertThat(lines[1]).startsWith(" ");
    }

    @Test
    void formatDateValue() {
        assertThat(formatDate(LocalDate.of(2025, 1, 5))).isEqualTo("20250105");
    }

    @Test
    void formatDateTimeValue() {
        assertThat(formatDateTime(LocalDate.of(2025, 1, 5), LocalTime.of(9, 5, 3))).isEqualTo("20250105T090503");
    }

    private static String[] lines(String ical) {
        return ical.split("\r\n");
    }

    private static EventDto event(Long id, LocalDate fromDate, LocalTime fromTime,
                                  LocalDate toDate, LocalTime toTime,
                                  String title, String location, String description) {
        return new EventDto(id, fromDate, fromTime, toDate, toTime, false,
                            title, description, location, null, null,
                            EventType.BLUE, false, true, false,
                            null, null, null, null);
    }
}
