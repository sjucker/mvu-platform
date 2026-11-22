package ch.mvurdorf.platform.passkey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WellKnownEndpointTest {

    private static final String FINGERPRINT = "FA:C6:17:45:DC:09:03:78:6F:B9:ED:E6:2A:96:2B:39:9F:73:48:F0:BB:6F:89:9B:83:32:66:75:91:03:3B:9C";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var endpoint = new WellKnownEndpoint(List.of("S45K487Z5C.ch.mvurdorf.platform"), "ch.mvurdorf.platform", List.of(FINGERPRINT, ""));
        mockMvc = MockMvcBuilders.standaloneSetup(endpoint).build();
    }

    @Test
    void appleAppSiteAssociation() throws Exception {
        mockMvc.perform(get("/.well-known/apple-app-site-association"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andExpect(jsonPath("$.webcredentials.apps", contains("S45K487Z5C.ch.mvurdorf.platform")));
    }

    @Test
    void assetLinks() throws Exception {
        mockMvc.perform(get("/.well-known/assetlinks.json"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(APPLICATION_JSON))
               .andExpect(jsonPath("$[0].relation", contains("delegate_permission/common.handle_all_urls", "delegate_permission/common.get_login_creds")))
               .andExpect(jsonPath("$[0].target.package_name").value("ch.mvurdorf.platform"))
               .andExpect(jsonPath("$[0].target.sha256_cert_fingerprints", contains(FINGERPRINT)));
    }
}
