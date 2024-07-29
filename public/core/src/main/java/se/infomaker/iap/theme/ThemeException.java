package se.infomaker.iap.theme;

public class ThemeException extends Exception {
    public ThemeException() {
    }

    public ThemeException(String message) {
        super(message);
    }

    public ThemeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ThemeException(Throwable cause) {
        super(cause);
    }
}
