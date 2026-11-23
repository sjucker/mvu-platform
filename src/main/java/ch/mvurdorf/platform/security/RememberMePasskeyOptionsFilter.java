package ch.mvurdorf.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

/**
 * Rejects the passkey registration options request of a user that is only logged in through the remember-me cookie.
 * <p>
 * Spring's {@code PublicKeyCredentialCreationOptionsFilter} runs before the {@code AuthorizationFilter} and only
 * checks for an authenticated user (without a setter to change that). So a stolen remember-me cookie must not be
 * enough to enroll a passkey, which would keep working after the cookie expires or the password is changed.
 * {@code POST /webauthn/register} itself is protected with {@code fullyAuthenticated()}.
 */
class RememberMePasskeyOptionsFilter extends OncePerRequestFilter {

    private static final RequestMatcher REGISTER_OPTIONS = pathPattern(POST, "/webauthn/register/options");

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
    private final SecurityContextHolderStrategy securityContextHolderStrategy;

    RememberMePasskeyOptionsFilter(SecurityContextHolderStrategy securityContextHolderStrategy) {
        this.securityContextHolderStrategy = securityContextHolderStrategy;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (REGISTER_OPTIONS.matches(request)
                && trustResolver.isRememberMe(securityContextHolderStrategy.getContext().getAuthentication())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }
}
