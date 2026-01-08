package ch.mvurdorf.platform;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@PWA(name = "Musikverein Harmonie Urdorf Platform", shortName = "MVU Platform")
@Theme("my-theme")
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(Application.PlatformProperties.class)
public class Application implements AppShellConfigurator {

    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @ConfigurationProperties(prefix = "platform")
    public record PlatformProperties(String url,
                                     String supportUrl,
                                     boolean overrideRecipients,
                                     String bccMail) {
    }

}
