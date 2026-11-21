package ch.mvurdorf.platform.security;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.page.PendingJavaScriptResult;
import com.vaadin.flow.server.VaadinServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.nullsLast;

/**
 * Manages the passkeys of a user and triggers the WebAuthn ceremonies in the browser (see {@code passkey.ts}).
 * Components using this need to load the client code with {@code @JsModule("./passkey.ts")}.
 */
@Service
@RequiredArgsConstructor
public class PasskeyService {

    private final PublicKeyCredentialUserEntityRepository userEntityRepository;
    private final UserCredentialRepository userCredentialRepository;

    public List<CredentialRecord> getPasskeys(String email) {
        var userEntity = userEntityRepository.findByUsername(email);
        if (userEntity == null) {
            return List.of();
        }
        return userCredentialRepository.findByUserId(userEntity.getId()).stream()
                                       .sorted(Comparator.comparing(CredentialRecord::getCreated, nullsLast(Comparator.naturalOrder())))
                                       .toList();
    }

    public void delete(String email, CredentialRecord passkey) {
        var userEntity = userEntityRepository.findByUsername(email);
        if (userEntity == null || !userEntity.getId().equals(passkey.getUserEntityUserId())) {
            throw new IllegalArgumentException("passkey does not belong to %s".formatted(email));
        }
        userCredentialRepository.delete(passkey.getCredentialId());
    }

    public static PendingJavaScriptResult isSupported(Component component) {
        return component.getElement().executeJs("return window.mvuPasskey.isSupported()");
    }

    /**
     * Redirects to the requested page on success.
     */
    public static PendingJavaScriptResult login(Component component) {
        var csrfToken = csrfToken();
        return component.getElement().executeJs("return window.mvuPasskey.login($0, $1)",
                                                csrfToken.getHeaderName(), csrfToken.getToken());
    }

    public static PendingJavaScriptResult register(Component component, String label) {
        var csrfToken = csrfToken();
        return component.getElement().executeJs("return window.mvuPasskey.register($0, $1, $2)",
                                                csrfToken.getHeaderName(), csrfToken.getToken(), label);
    }

    private static CsrfToken csrfToken() {
        // Vaadin's own requests are excluded from CSRF protection, but the CsrfFilter still exposes the token
        return (CsrfToken) VaadinServletRequest.getCurrent().getAttribute(CsrfToken.class.getName());
    }
}
