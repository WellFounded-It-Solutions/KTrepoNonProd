package se.infomaker.frt.remotenotification;

/**
 * Handel remote notifications
 */
public interface OnRemoteNotificationListener {
    /**
     * Callback when a notification is received
     * @param notification received
     */
    void onNotification(RemoteNotification notification);
}
