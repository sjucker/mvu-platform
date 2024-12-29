package ch.mvurdorf.platform.service;

import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

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
        var response = webClient.post()
                                .headers(headers -> headers.setBasicAuth(appId, privateKey))
                                .contentType(APPLICATION_JSON)
                                .body(BodyInserters.fromValue(new Request(mjml)))
                                .retrieve()
                                .bodyToMono(Response.class)
                                .block();

        if (response != null) {
            return response.html();
        } else {
            throw new IllegalStateException("received empty result from MJML API");
        }
    }

    private record Request(String mjml) {
    }

    private record Response(List<?> errors, String html, String mjml, String mjml_version) {
    }
}
