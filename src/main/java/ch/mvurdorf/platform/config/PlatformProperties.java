package ch.mvurdorf.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform")
public record PlatformProperties(String url,
                                 String supportUrl,
                                 boolean overrideRecipients,
                                 String bccMail) {
}
