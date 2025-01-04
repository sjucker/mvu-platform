package ch.mvurdorf.platform.utils;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static ch.mvurdorf.platform.utils.FormatUtil.ZONE_ID;
import static org.assertj.core.api.Assertions.assertThat;

class FormatUtilTest {

    @Test
    void formatDate() {
        assertThat(FormatUtil.formatDate(null)).isEqualTo("-");
        assertThat(FormatUtil.formatDate(LocalDate.of(2025, 1, 4))).isEqualTo("04.01.2025");
    }

    @Test
    void formatTime() {
        assertThat(FormatUtil.formatTime(null)).isEqualTo("-");
        assertThat(FormatUtil.formatTime(LocalTime.of(18, 19, 20))).isEqualTo("18:19");
    }

    @Test
    void formatDateTime() {
        assertThat(FormatUtil.formatDateTime(null)).isEqualTo("-");
        assertThat(FormatUtil.formatDateTime(LocalDateTime.of(2025, 1, 4, 20, 15))).isEqualTo("04.01.2025 20:15");
    }

    @Test
    void formatInstant() {
        assertThat(FormatUtil.formatInstant(null)).isEqualTo("-");
        assertThat(FormatUtil.formatInstant(Instant.now(Clock.fixed(Instant.ofEpochSecond(1735984546L), ZONE_ID)))).isEqualTo("04.01.2025 10:55");
    }
}
