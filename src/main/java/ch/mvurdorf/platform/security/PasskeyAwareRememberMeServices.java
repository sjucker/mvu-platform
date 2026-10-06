package ch.mvurdorf.platform.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

/**
 * The principal of a passkey login is a {@code PublicKeyCredentialUserEntity} and not a {@code UserDetails},
 * so the default implementation would use its {@code toString()} as username for the remember-me cookie.
 */
public class PasskeyAwareRememberMeServices extends TokenBasedRememberMeServices {

    public PasskeyAwareRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
    }

    @Override
    protected String retrieveUserName(Authentication authentication) {
        return authentication.getName();
    }
}
