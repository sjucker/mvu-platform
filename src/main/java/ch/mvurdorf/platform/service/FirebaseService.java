package ch.mvurdorf.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class FirebaseService {

    private final String firebaseApiKey;

    public FirebaseService(@Value("${firebase.api-key}") String firebaseApiKey) {
        this.firebaseApiKey = firebaseApiKey;
    }

    // https://cloud.google.com/identity-platform/docs/reference/rest/v1/accounts/signUp

    private record FirebaseSignUpRequest(String email, String displayName, String password, boolean emailVerified) {}

    private record FirebaseSignUpResponse(String idToken, String refreshToken) {}

    /**
     * @return whether the user could be created
     */
    public boolean createUser(String email, String name, String password) {
        var signUpRequest = new FirebaseSignUpRequest(email, name, password, true);
        try {
            var response = RestClient.create("https://identitytoolkit.googleapis.com/v1/accounts:signUp")
                                     .post()
                                     .uri(uriBuilder -> uriBuilder.queryParam("key", firebaseApiKey).build())
                                     .body(signUpRequest)
                                     .contentType(APPLICATION_JSON)
                                     .retrieve()
                                     .body(FirebaseSignUpResponse.class);
            return response != null;
        } catch (RestClientResponseException e) {
            return false;
        }
    }

    // see https://cloud.google.com/identity-platform/docs/reference/rest/v1/accounts/signInWithPassword

    private record FirebaseSignInRequest(String email, String password, boolean returnSecureToken) {}

    private record FirebaseSignInResponse(String idToken, String refreshToken) {}

    public boolean verifyUsernamePassword(String email, String password) {
        var signInRequest = new FirebaseSignInRequest(email, password, true);
        try {
            var response = RestClient.create("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword")
                                     .post()
                                     .uri(uriBuilder -> uriBuilder.queryParam("key", firebaseApiKey).build())
                                     .body(signInRequest)
                                     .contentType(APPLICATION_JSON)
                                     .retrieve()
                                     .body(FirebaseSignInResponse.class);
            return response != null;
        } catch (RestClientResponseException e) {
            return false;
        }
    }

}
