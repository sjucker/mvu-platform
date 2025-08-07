package ch.mvurdorf.platform.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDataDto {
    private Long id;
    private boolean trackChanges;
    private LocalDate fromDate;
    private LocalTime fromTime;
    private LocalDate toDate;
    private LocalTime toTime;
    private boolean approximately;
    private String title;
    private String description;
    private String location;
    private String interna;
    private String literature;
    private EventType type;
    private boolean relevantForAbsenz;
    private boolean relevantForWebsite;

    public static EventDataDto newEvent() {
        return new EventDataDto(null, false, null, null, null, null, false, null, null, null, null, null, null, true, false);
    }

    public static EventDataDto of(EventDto event) {
        return new EventDataDto(event.id(),
                                true,
                                event.fromDate(),
                                event.fromTime(),
                                event.toDate(),
                                event.toTime(),
                                event.approximately(),
                                event.title(),
                                event.description(),
                                event.location(),
                                event.interna(),
                                event.literature(),
                                event.type(),
                                event.relevantForAbsenz(),
                                event.relevantForWebsite());
    }

    public static EventDataDto copy(EventDto event) {
        return new EventDataDto(null, false,
                                event.fromDate(),
                                event.fromTime(),
                                event.toDate(),
                                event.toTime(),
                                event.approximately(),
                                event.title(),
                                event.description(),
                                event.location(),
                                event.interna(),
                                event.literature(),
                                event.type(),
                                event.relevantForAbsenz(),
                                event.relevantForWebsite());
    }
}
