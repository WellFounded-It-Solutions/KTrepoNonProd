package se.infomaker.iap.theme;

public class UndefinedException extends ThemeException{
    public UndefinedException() {
    }

    public UndefinedException(String message) {
        super(message);
    }

    public UndefinedException(String message, Throwable cause) {
        super(message, cause);
    }
}
