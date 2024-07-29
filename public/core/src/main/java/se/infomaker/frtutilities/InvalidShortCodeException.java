package se.infomaker.frtutilities;

/**
 * Indicates that a shortcode was malformed to a degree where it is not possible to continue
 */
public class InvalidShortCodeException extends Exception {

    public InvalidShortCodeException(String message) {
        super(message);
    }
}
