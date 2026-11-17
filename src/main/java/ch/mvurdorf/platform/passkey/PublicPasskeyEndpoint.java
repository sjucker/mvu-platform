package ch.mvurdorf.platform.passkey;

import ch.mvurdorf.platform.service.BaseFirebaseService;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Passkey login of the app: on success, a Firebase custom token is returned, which the app uses to sign in.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/passkey")
public class PublicPasskeyEndpoint {

    private final PasskeyService passkeyService;
    private final BaseFirebaseService firebaseService;

    @PostMapping("/login/start")
    public ResponseEntity<PasskeyOptionsDto> startLogin() {
        log.info("POST /api/passkey/login/start");
        return ResponseEntity.ok(passkeyService.startAssertion());
    }

    /**
     * @param credentialJson the JSON serialized {@code PublicKeyCredential} returned by {@code navigator.credentials.get()}
     */
    @PostMapping("/login/finish/{requestId}")
    public ResponseEntity<PasskeyLoginDto> finishLogin(@PathVariable UUID requestId,
                                                       @RequestBody String credentialJson) throws FirebaseAuthException {
        log.info("POST /api/passkey/login/finish/{}", requestId);
        var email = passkeyService.finishAssertion(requestId, credentialJson);
        return ResponseEntity.ok(new PasskeyLoginDto(firebaseService.createCustomToken(email)));
    }

    @ExceptionHandler(PasskeyException.class)
    public ResponseEntity<Void> handlePasskeyException(PasskeyException e) {
        log.warn("passkey login failed", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
