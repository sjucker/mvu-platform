package ch.mvurdorf.platform.passkey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Lets a signed-in user (of the app) register and manage their passkeys.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/secured/passkey")
public class PasskeyEndpoint {

    private final PasskeyService passkeyService;

    @GetMapping
    public ResponseEntity<List<PasskeyDto>> getPasskeys(@AuthenticationPrincipal Jwt jwt) {
        log.info("GET /api/secured/passkey");
        return ResponseEntity.ok(passkeyService.findPasskeys(jwt.getClaimAsString("email")));
    }

    @PostMapping("/register/start")
    public ResponseEntity<PasskeyOptionsDto> startRegistration(@AuthenticationPrincipal Jwt jwt) {
        log.info("POST /api/secured/passkey/register/start");
        return ResponseEntity.ok(passkeyService.startRegistration(jwt.getClaimAsString("email")));
    }

    /**
     * @param credentialJson the JSON serialized {@code PublicKeyCredential} returned by {@code navigator.credentials.create()}
     * @param name           label shown in the list of passkeys (e.g. the device name)
     */
    @PostMapping("/register/finish/{requestId}")
    public ResponseEntity<Void> finishRegistration(@AuthenticationPrincipal Jwt jwt,
                                                   @PathVariable UUID requestId,
                                                   @RequestParam(required = false) String name,
                                                   @RequestBody String credentialJson) {
        log.info("POST /api/secured/passkey/register/finish/{}", requestId);
        passkeyService.finishRegistration(jwt.getClaimAsString("email"), requestId, credentialJson, name);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePasskey(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        log.info("DELETE /api/secured/passkey/{}", id);
        return passkeyService.deletePasskey(jwt.getClaimAsString("email"), id)
               ? ResponseEntity.ok().build()
               : ResponseEntity.notFound().build();
    }

    @ExceptionHandler(PasskeyException.class)
    public ResponseEntity<Void> handlePasskeyException(PasskeyException e) {
        log.warn("passkey registration failed", e);
        return ResponseEntity.badRequest().build();
    }
}
