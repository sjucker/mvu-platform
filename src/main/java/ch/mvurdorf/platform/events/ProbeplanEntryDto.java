package ch.mvurdorf.platform.events;

import java.time.LocalDateTime;

public record ProbeplanEntryDto(int year,
                                String month,
                                String type,
                                String dayOfMonth,
                                String dayOfWeek,
                                String time,
                                String title,
                                String location,
                                String literature,
                                LocalDateTime deleted,
                                boolean dateChanged,
                                boolean timeChanged,
                                boolean titleChanged,
                                boolean locationChanged,
                                boolean literatureChanged) {
}
