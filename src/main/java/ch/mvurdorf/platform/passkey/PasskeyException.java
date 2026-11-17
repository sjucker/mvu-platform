package ch.mvurdorf.platform.passkey;

/**
 * Thrown when a passkey registration or assertion can not be completed (unknown or expired request, invalid credential).
 */
public class PasskeyException extends RuntimeException {

    public PasskeyException(String message) {
        super(message);
    }

    public PasskeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
