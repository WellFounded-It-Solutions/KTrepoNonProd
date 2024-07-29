package se.infomaker.frt.remotenotification;

public interface RemoteNotificationInterceptor {

    /**
     * Intercept point where the notification can be modified
     * a notification can be aborted by throwing an AbortNotificationException
     */
    RemoteNotification intercept(RemoteNotification remoteNotification) throws AbortNotificationException;

}
