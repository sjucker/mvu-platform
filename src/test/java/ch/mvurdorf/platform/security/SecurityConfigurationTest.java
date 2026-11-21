package ch.mvurdorf.platform.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigurationTest {

    @Test
    void toUri() {
        assertThat(SecurityConfiguration.toUri("localhost:8080")).hasScheme("http").hasHost("localhost").hasPort(8080);
        assertThat(SecurityConfiguration.toUri("http://localhost:8080")).hasScheme("http").hasHost("localhost").hasPort(8080);
        assertThat(SecurityConfiguration.toUri("platform.mvurdorf.ch")).hasScheme("https").hasHost("platform.mvurdorf.ch");
        assertThat(SecurityConfiguration.toUri("https://platform.mvurdorf.ch/")).hasScheme("https").hasHost("platform.mvurdorf.ch");
    }
}
