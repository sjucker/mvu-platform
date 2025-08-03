package ch.mvurdorf.platform.events;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/api/secured/event")
public class EventsEndpoint {

    @GetMapping
    public ResponseEntity<String> ping(@AuthenticationPrincipal Jwt jwt) {
        log.info("GET /api/secured/event");

        log.info("JWT subject: {}", jwt.getSubject());
        log.info("JWT claims: {}", jwt.getClaims());

        return ResponseEntity.ok("pong");
    }

}
