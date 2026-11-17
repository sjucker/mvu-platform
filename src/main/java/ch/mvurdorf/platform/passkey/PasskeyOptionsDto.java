package ch.mvurdorf.platform.passkey;

import com.fasterxml.jackson.annotation.JsonRawValue;

import java.util.UUID;

/**
 * @param requestId identifies the pending request, must be sent back when finishing the registration/assertion
 * @param options   JSON for {@code navigator.credentials.create()} or {@code navigator.credentials.get()}
 *                  (i.e. <code>{"publicKey": {...}}</code>)
 */
public record PasskeyOptionsDto(UUID requestId, @JsonRawValue String options) {
}
