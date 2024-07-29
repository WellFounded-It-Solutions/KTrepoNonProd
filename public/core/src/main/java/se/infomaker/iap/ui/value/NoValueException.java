package se.infomaker.iap.ui.value;

/**
 * Indicates that no value was found
 */
public class NoValueException extends Exception {
    @SuppressWarnings("unused")
    public NoValueException() {
    }

    public NoValueException(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    public NoValueException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoValueException(Throwable cause) {
        super(cause);
    }
}
