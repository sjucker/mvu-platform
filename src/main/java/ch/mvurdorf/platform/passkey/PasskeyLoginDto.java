package ch.mvurdorf.platform.passkey;

/**
 * @param customToken Firebase custom token, the app signs in with it using {@code signInWithCustomToken}
 */
public record PasskeyLoginDto(String customToken) {
}
