package ch.mvurdorf.platform.service;

import lombok.NoArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;

/**
 * Resolves links to the static images of the platform (served from {@code META-INF/resources/images}), so they can be
 * embedded into emails and PDFs instead of being loaded from the server.
 */
@NoArgsConstructor(access = PRIVATE)
public final class StaticImages {

    private static final String STATIC_RESOURCES = "META-INF/resources";

    /**
     * @param src     the src of an image, e.g. {@code https://admin.mvurdorf.ch/images/mailing/letterhead.png}
     * @param baseUrl the base url of the platform
     * @return the image, if src links to a static image of the platform
     */
    public static Optional<Image> find(String src, String baseUrl) {
        if (src == null || baseUrl == null || !src.startsWith(baseUrl + "/images/")) {
            return Optional.empty();
        }
        return load(src.substring(baseUrl.length()));
    }

    /**
     * @param path the path of the image, e.g. {@code /images/mailing/letterhead.png}
     */
    public static Optional<Image> load(String path) {
        var resource = new ClassPathResource(STATIC_RESOURCES + path);
        if (!resource.exists()) {
            return Optional.empty();
        }
        try (var in = resource.getInputStream()) {
            var contentType = MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
            return Optional.of(new Image(resource.getFilename(), contentType.toString(), in.readAllBytes()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public record Image(String fileName, String contentType, byte[] data) {
    }
}
