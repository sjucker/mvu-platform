package ch.mvurdorf.platform.security;

import ch.mvurdorf.platform.service.BaseFirebaseService;
import ch.mvurdorf.platform.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Duration;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
public class SecurityConfiguration {

    private final String rememberMeKey;

    public SecurityConfiguration(@Value("${remember-me.key}") String rememberMeKey) {
        this.rememberMeKey = rememberMeKey;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize.requestMatchers("/images/**").permitAll()
                                                         .requestMatchers("/line-awesome/**").permitAll()
                                                         .requestMatchers("/actuator/health/**", "/actuator/info").permitAll());

        http.rememberMe(configurer -> configurer.key(rememberMeKey)
                                                .tokenValiditySeconds((int) Duration.ofDays(365).toSeconds())
                                                .alwaysRemember(true));

        return http.with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer.loginView(LoginView.class)).build();
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
