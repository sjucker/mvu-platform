package ch.mvurdorf.platform.config;

import ch.mvurdorf.platform.service.MjmlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MjmlConfiguration.MjmlProperties.class})
public class MjmlConfiguration {

    private static final String BASE_URL = "https://api.mjml.io/v1/render";

    @Bean
    public MjmlService mjmlService(@Value("${mjml.app-id}") String appId,
                                   @Value("${mjml.private-key}") String privateKey) {
        return new MjmlService(appId, privateKey, BASE_URL);
    }

    @ConfigurationProperties(prefix = "mjml")
    public record MjmlProperties(String appId,
                                 String privateKey) {
    }

}
