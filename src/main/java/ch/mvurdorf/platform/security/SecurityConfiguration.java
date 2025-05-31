package ch.mvurdorf.platform.security;

import ch.mvurdorf.platform.service.FirebaseService;
import ch.mvurdorf.platform.ui.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class SecurityConfiguration extends VaadinWebSecurity {

    private final String rememberMeKey;

    public SecurityConfiguration(@Value("${remember-me.key}") String rememberMeKey) {
        this.rememberMeKey = rememberMeKey;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/images/**").permitAll());
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/line-awesome/**").permitAll());

        http.rememberMe(configurer -> configurer.key(rememberMeKey)
                                                .tokenValiditySeconds((int) Duration.ofDays(365).toSeconds())
                                                .alwaysRemember(true));

        super.configure(http);
        setLoginView(http, LoginView.class);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(LoginService loginService, FirebaseService firebaseService) {
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
