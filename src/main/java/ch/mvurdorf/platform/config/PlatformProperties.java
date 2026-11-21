package ch.mvurdorf.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "platform")
public record PlatformProperties(String url,
                                 String supportUrl,
                                 boolean overrideRecipients,
                                 String bccMail) {

    public PlatformProperties {
        // the passkey relying party id and allowed origin are derived from it, and it is used for links in mails
        var uri = url == null ? null : URI.create(url);
        if (uri == null || uri.getScheme() == null || uri.getHost() == null) {
            throw new IllegalStateException("platform.url must be an absolute URL like https://example.com, but was: " + url);
        }
    }

    public URI uri() {
        return URI.create(url);
    }

    public String supporterPortalLink(String uuid) {
        return "%s/portal/%s".formatted(supportUrl, uuid);
    }
}
