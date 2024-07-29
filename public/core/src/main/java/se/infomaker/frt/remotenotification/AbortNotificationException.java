package se.infomaker.frt.remotenotification;

public class AbortNotificationException extends Exception {

    public AbortNotificationException(String message) {
        super((message));
    }

    public AbortNotificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbortNotificationException(Throwable cause) {
        super(cause);
    }
}
