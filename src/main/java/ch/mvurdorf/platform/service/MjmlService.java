package ch.mvurdorf.platform.service;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class MjmlService {

    private final String appId;
    private final String privateKey;
    private final WebClient webClient;

    public MjmlService(String appId,
                       String privateKey,
                       String baseUrl) {
        this.appId = appId;
        this.privateKey = privateKey;
        this.webClient = WebClient.create(baseUrl);
    }

    public String render(String mjml) {
        return renderWithErrors(mjml).html();
    }

    /**
     * Renders the given MJML and also returns the validation errors reported by the MJML API (if any).
     */
    public MjmlResult renderWithErrors(String mjml) {
        var response = webClient.post()
                                .headers(headers -> headers.setBasicAuth(appId, privateKey))
                                .contentType(APPLICATION_JSON)
                                .body(BodyInserters.fromValue(new Request(mjml)))
                                .retrieve()
                                .bodyToMono(Response.class)
                                .block();

        if (response != null) {
            var errors = response.errors() == null ? List.<String>of() : response.errors().stream()
                                                                                .map(MjmlService::errorMessage)
                                                                                .toList();
            return new MjmlResult(response.html(), errors);
        } else {
            throw new IllegalStateException("received empty result from MJML API");
        }
    }

    private static String errorMessage(Map<String, Object> error) {
        return Objects.toString(error.getOrDefault("formattedMessage", error.get("message")));
    }

    public record MjmlResult(String html, List<String> errors) {
    }

    private record Request(String mjml) {
    }

    private record Response(List<Map<String, Object>> errors, String html, String mjml, String mjml_version) {
    }
}
