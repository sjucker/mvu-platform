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
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationFilter;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.security.web.webauthn.management.PublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.UserCredentialRepository;

import java.net.URI;
import java.time.Duration;

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
    SecurityFilterChain securityFilterChain(HttpSecurity http, LoginService loginService) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/images/**").permitAll()
                                                         .requestMatchers("/line-awesome/**").permitAll()
                                                         .requestMatchers(PathRequest.toStaticResources()
                                                                                     .atCommonLocations()).permitAll()
                                                         .requestMatchers("/actuator/health/**", "/actuator/info").permitAll());

        var rememberMeServices = new PasskeyAwareRememberMeServices(rememberMeKey, loginService);
        rememberMeServices.setTokenValiditySeconds((int) Duration.ofDays(365).toSeconds());
        rememberMeServices.setAlwaysRemember(true);
        http.rememberMe(configurer -> configurer.key(rememberMeKey)
                                                .rememberMeServices(rememberMeServices));

        http.webAuthn(configurer -> configurer.rpName("MVU Platform")
                                              .rpId(platformUrl.getHost())
                                              .allowedOrigins(getOrigin(platformUrl))
                                              .disableDefaultRegistrationPage(true)
                                              .withObjectPostProcessor(new ObjectPostProcessor<WebAuthnAuthenticationFilter>() {
                                                  @Override
                                                  public <O extends WebAuthnAuthenticationFilter> O postProcess(O filter) {
                                                      // the filter is not wired by Spring Security like the form login filter is
                                                      filter.setRememberMeServices(rememberMeServices);
                                                      filter.setSessionAuthenticationStrategy(new ChangeSessionIdAuthenticationStrategy());
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
