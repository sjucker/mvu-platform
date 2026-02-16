package ch.mvurdorf.platform.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/event")
public class PublicEventsEndpoint {

    private final EventsService eventsService;

    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(@RequestParam(required = false) Integer limit) {
        log.info("GET /api/event?limit={}", limit);

        return ResponseEntity.ok(eventsService.findAllFutureEventsForWebsite(limit));
    }

    @GetMapping("/probeplan")
    public ResponseEntity<List<ProbeplanEntryDto>> probeplan(@RequestParam(required = false) LocalDate cutoffDate,
                                                             @RequestParam(required = false) LocalDate from,
                                                             @RequestParam(required = false) LocalDate to) {
        log.info("GET /api/event/probeplan?cutoffDate={}&from={}&to={}", cutoffDate, from, to);

        return ResponseEntity.ok(eventsService.getProbeplan(from, to, cutoffDate));
    }

}
