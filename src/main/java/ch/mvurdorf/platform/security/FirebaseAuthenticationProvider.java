package ch.mvurdorf.platform.security;

import ch.mvurdorf.platform.service.FirebaseService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class FirebaseAuthenticationProvider extends DaoAuthenticationProvider {

    private final FirebaseService firebaseService;

    public FirebaseAuthenticationProvider(UserDetailsService userDetailsService, FirebaseService firebaseService) {
        super(userDetailsService);
        this.firebaseService = firebaseService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        if (!firebaseService.verifyUsernamePassword(authentication.getName(), authentication.getCredentials().toString())) {
            throw new BadCredentialsException(this.messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }
    }
}
