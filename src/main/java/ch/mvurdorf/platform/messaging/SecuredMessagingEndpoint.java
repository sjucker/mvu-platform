package ch.mvurdorf.platform.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/secured/messaging")
public class SecuredMessagingEndpoint {

    private final MessagingService messagingService;

    @PutMapping
    public ResponseEntity<Void> updateToken(@AuthenticationPrincipal Jwt jwt,
                                            @RequestBody TokenUpdateDto body) {
        log.info("PUT /api/secured/messaging");

        messagingService.updateToken(jwt.getClaimAsString("email"), body.token());
        return ResponseEntity.ok().build();
    }

}
