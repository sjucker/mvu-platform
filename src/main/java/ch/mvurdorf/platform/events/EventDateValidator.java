package ch.mvurdorf.platform.events;

import jakarta.annotation.Nullable;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
final class EventDateValidator {

    static final String TO_DATE_REQUIRED_MESSAGE = "Uhrzeit bis liegt vor Uhrzeit von – bitte ein späteres Datum bis erfassen";

    /**
     * An end time before the start time implies a multi-day event, so an end date after the start date is mandatory.
     * Incomplete input (missing start date or either time) is not judged here.
     */
    static boolean isToDateValid(@Nullable LocalDate fromDate,
                                 @Nullable LocalTime fromTime,
                                 @Nullable LocalDate toDate,
                                 @Nullable LocalTime toTime) {
        if (fromDate == null || fromTime == null || toTime == null || !toTime.isBefore(fromTime)) {
            return true;
        }
        return toDate != null && toDate.isAfter(fromDate);
    }
}
