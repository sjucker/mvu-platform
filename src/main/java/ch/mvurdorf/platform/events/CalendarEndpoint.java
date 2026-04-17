package ch.mvurdorf.platform.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar")
public class CalendarEndpoint {

    private static final MediaType TEXT_CALENDAR = MediaType.parseMediaType("text/calendar;charset=UTF-8");

    private final EventsService eventsService;

    @GetMapping("/{token}/events.ics")
    public ResponseEntity<String> getCalendar(@PathVariable String token) {
        log.info("GET /api/calendar/{}/events.ics", token);

        return ResponseEntity.ok()
                             .contentType(TEXT_CALENDAR)
                             .body(ICalUtil.buildIcal(eventsService.findPositiveEventsForCalendar(token)));
    }
}
