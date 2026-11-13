package ch.mvurdorf.platform.events;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static ch.mvurdorf.platform.events.EventDateValidator.isToDateValid;
import static org.assertj.core.api.Assertions.assertThat;

class EventDateValidatorTest {

    private static final LocalDate DAY = LocalDate.of(2025, 6, 15);
    private static final LocalDate NEXT_DAY = DAY.plusDays(1);
    private static final LocalDate PREVIOUS_DAY = DAY.minusDays(1);
    private static final LocalTime EVENING = LocalTime.of(19, 0);
    private static final LocalTime LATE = LocalTime.of(22, 0);
    private static final LocalTime AFTER_MIDNIGHT = LocalTime.of(2, 0);

    @Test
    void toTimeAfterFromTime_noToDateNeeded() {
        assertThat(isToDateValid(DAY, EVENING, null, LATE)).isTrue();
    }

    @Test
    void toTimeEqualToFromTime_noToDateNeeded() {
        assertThat(isToDateValid(DAY, EVENING, null, EVENING)).isTrue();
    }

    @Test
    void toTimeBeforeFromTime_withoutToDate_isInvalid() {
        assertThat(isToDateValid(DAY, EVENING, null, AFTER_MIDNIGHT)).isFalse();
    }

    @Test
    void toTimeBeforeFromTime_withSameToDate_isInvalid() {
        assertThat(isToDateValid(DAY, EVENING, DAY, AFTER_MIDNIGHT)).isFalse();
    }

    @Test
    void toTimeBeforeFromTime_withEarlierToDate_isInvalid() {
        assertThat(isToDateValid(DAY, EVENING, PREVIOUS_DAY, AFTER_MIDNIGHT)).isFalse();
    }

    @Test
    void toTimeBeforeFromTime_withLaterToDate_isValid() {
        assertThat(isToDateValid(DAY, EVENING, NEXT_DAY, AFTER_MIDNIGHT)).isTrue();
    }

    @Test
    void missingTimes_areNotJudged() {
        assertThat(isToDateValid(DAY, null, null, AFTER_MIDNIGHT)).isTrue();
        assertThat(isToDateValid(DAY, EVENING, null, null)).isTrue();
        assertThat(isToDateValid(DAY, null, null, null)).isTrue();
    }

    @Test
    void missingFromDate_isNotJudged() {
        assertThat(isToDateValid(null, EVENING, null, AFTER_MIDNIGHT)).isTrue();
    }
}
