package ch.mvurdorf.platform.security;

import ch.mvurdorf.platform.service.BaseFirebaseService;
import ch.mvurdorf.platform.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationFilter;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;

import java.net.URI;
import java.time.Duration;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    private final String rememberMeKey;
    private final URI platformUrl;

    public SecurityConfiguration(@Value("${remember-me.key}") String rememberMeKey,
                                 @Value("${platform.url:http://localhost:8080}") String platformUrl) {
        this.rememberMeKey = rememberMeKey;
        this.platformUrl = toUri(platformUrl);
    }

    static URI toUri(String url) {
        if (url.contains("://")) {
            return URI.create(url);
        }
        // e.g. "localhost:8080" would otherwise be parsed as scheme "localhost"
        var scheme = url.startsWith("localhost") ? "http" : "https";
        return URI.create(scheme + "://" + url);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, LoginService loginService,
                                            SecurityContextHolderStrategy securityContextHolderStrategy) throws Exception {
        // Vaadin only installs its SecurityContextHolderStrategy globally after all singletons are created, but the
        // WebAuthn filters capture the global strategy on construction (and do not all offer a setter). Without this
        // they would not see the authentication of the logged-in user and the passkey registration fails with HTTP 400.
        SecurityContextHolder.setContextHolderStrategy(securityContextHolderStrategy);

        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/images/**").permitAll()
                                                         .requestMatchers("/line-awesome/**").permitAll()
                                                         .requestMatchers(PathRequest.toStaticResources()
                                                                                     .atCommonLocations()).permitAll()
                                                         .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                                                         // the WebAuthnRegistrationFilter runs after the AuthorizationFilter
                                                         // and Vaadin denies any non-Vaadin request by default
                                                         .requestMatchers(POST, "/webauthn/register").authenticated());

        var rememberMeServices = new PasskeyAwareRememberMeServices(rememberMeKey, loginService);
        rememberMeServices.setTokenValiditySeconds((int) Duration.ofDays(365).toSeconds());
        rememberMeServices.setAlwaysRemember(true);
        http.rememberMe(configurer -> configurer.key(rememberMeKey)
                                                .rememberMeServices(rememberMeServices));

        // WebAuthnAuthenticationProvider only copies the authorities and does not check the account status
        // (unlike DaoAuthenticationProvider), so make sure deactivated users cannot log in with a passkey
        var accountStatusChecker = new AccountStatusUserDetailsChecker();
        http.setSharedObject(UserDetailsService.class, username -> {
            var user = loginService.loadUserByUsername(username);
            accountStatusChecker.check(user);
            return user;
        });

        http.webAuthn(configurer -> configurer.rpName("MVU Platform")
                                              .rpId(platformUrl.getHost())
                                              .allowedOrigins(getOrigin(platformUrl))
                                              .disableDefaultRegistrationPage(true)
                                              .withObjectPostProcessor(new ObjectPostProcessor<WebAuthnAuthenticationFilter>() {
                                                  @Override
                                                  public <O extends WebAuthnAuthenticationFilter> O postProcess(O filter) {
                                                      // the filter is not wired by Spring Security like the form login filter is;
                                                      // use the same session strategy as form login (session fixation protection + CSRF token rotation)
                                                      filter.setRememberMeServices(rememberMeServices);
                                                      filter.setSessionAuthenticationStrategy(http.getSharedObject(SessionAuthenticationStrategy.class));
                                                      return filter;
                                                  }
                                              }));

        return http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer.loginView(LoginView.class)).build();
    }

    private static String getOrigin(URI uri) {
        return uri.getScheme() + "://" + uri.getAuthority();
    }

    @Bean
    public PublicKeyCredentialUserEntityRepository publicKeyCredentialUserEntityRepository(JdbcOperations jdbcOperations) {
        return new JdbcPublicKeyCredentialUserEntityRepository(jdbcOperations);
    }

    @Bean
    public UserCredentialRepository userCredentialRepository(JdbcOperations jdbcOperations) {
        return new JdbcUserCredentialRepository(jdbcOperations);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(LoginService loginService, BaseFirebaseService firebaseService) {
        return new FirebaseAuthenticationProvider(loginService, firebaseService);
    }

    @Bean
    @Order(10)
    public SecurityFilterChain configureSecuredApi(HttpSecurity http) throws Exception {
        return http.securityMatcher("/api/secured/**")
                   .csrf(AbstractHttpConfigurer::disable)
                   .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                   .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                   .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                   .build();
    }

    @Bean
    @Order(11)
    public SecurityFilterChain configurePublicApi(HttpSecurity http) throws Exception {
        return http.securityMatcher("/api/**")
                   .csrf(AbstractHttpConfigurer::disable)
                   .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                   .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                   .build();
    }

}
