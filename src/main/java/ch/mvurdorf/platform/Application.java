package ch.mvurdorf.platform;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application.
 * <p>
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@PWA(name = "Musikverein Harmonie Urdorf Platform", shortName = "MVU Platform")
@Theme("my-theme")
public class Application implements AppShellConfigurator {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
