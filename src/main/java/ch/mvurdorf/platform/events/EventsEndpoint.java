package ch.mvurdorf.platform.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/secured/event")
public class EventsEndpoint {

    private final EventsService eventsService;

    @GetMapping
    public ResponseEntity<List<EventAbsenzStatusDto>> ping(@AuthenticationPrincipal Jwt jwt) {
        log.info("GET /api/secured/event");

        return ResponseEntity.ok(eventsService.findEventAbsenzenForUser(jwt.getClaim("email")));
    }

}
