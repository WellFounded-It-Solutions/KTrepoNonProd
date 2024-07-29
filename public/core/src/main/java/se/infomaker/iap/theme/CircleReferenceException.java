package se.infomaker.iap.theme;

/**
 * Indicates that a circle reference has been detected
 */
class CircleReferenceException extends ThemeException {
    public CircleReferenceException() {
    }

    public CircleReferenceException(String message) {
        super(message);
    }

    public CircleReferenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CircleReferenceException(Throwable cause) {
        super(cause);
    }
}
