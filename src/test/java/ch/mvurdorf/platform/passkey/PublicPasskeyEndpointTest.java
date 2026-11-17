package ch.mvurdorf.platform.passkey;

import ch.mvurdorf.platform.service.BaseFirebaseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicPasskeyEndpointTest {

    private static final UUID REQUEST_ID = UUID.fromString("7c0c5f8e-2f4a-4a8e-9a43-0d8b7f6b2d11");

    private PasskeyService passkeyService;
    private BaseFirebaseService firebaseService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        passkeyService = mock(PasskeyService.class);
        firebaseService = mock(BaseFirebaseService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PublicPasskeyEndpoint(passkeyService, firebaseService)).build();
    }

    @Test
    void startLoginReturnsOptionsAsJsonObject() throws Exception {
        when(passkeyService.startAssertion()).thenReturn(new PasskeyOptionsDto(REQUEST_ID, """
                {"publicKey":{"challenge":"abc","rpId":"platform.mvurdorf.ch","userVerification":"required"}}"""));

        mockMvc.perform(post("/api/passkey/login/start"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.requestId").value(REQUEST_ID.toString()))
               .andExpect(jsonPath("$.options.publicKey.rpId").value("platform.mvurdorf.ch"))
               .andExpect(jsonPath("$.options.publicKey.challenge").value("abc"));
    }

    @Test
    void finishLoginReturnsCustomToken() throws Exception {
        when(passkeyService.finishAssertion(REQUEST_ID, "{\"id\":\"cred\"}")).thenReturn("test@mvurdorf.ch");
        when(firebaseService.createCustomToken("test@mvurdorf.ch")).thenReturn("custom-token");

        mockMvc.perform(post("/api/passkey/login/finish/{requestId}", REQUEST_ID).contentType(APPLICATION_JSON).content("{\"id\":\"cred\"}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.customToken").value("custom-token"));
    }

    @Test
    void finishLoginWithInvalidPasskeyIsUnauthorized() throws Exception {
        when(passkeyService.finishAssertion(REQUEST_ID, "{}")).thenThrow(new PasskeyException("invalid"));

        mockMvc.perform(post("/api/passkey/login/finish/{requestId}", REQUEST_ID).contentType(APPLICATION_JSON).content("{}"))
               .andExpect(status().isUnauthorized());
    }
}
