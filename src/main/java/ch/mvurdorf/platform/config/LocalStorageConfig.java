package ch.mvurdorf.platform.config;

import ch.mvurdorf.platform.service.LocalStorageService;
import ch.mvurdorf.platform.service.StorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class LocalStorageConfig {

    @Bean
    public StorageService localStorageService() {
        return new LocalStorageService("target");
    }

}
