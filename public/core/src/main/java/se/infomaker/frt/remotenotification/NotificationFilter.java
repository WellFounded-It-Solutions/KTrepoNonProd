package se.infomaker.frt.remotenotification;

/**
 * Determine if a notification matches given criteria
 */
public interface NotificationFilter {

    /**
     * Check if the filter matches a notification sender and data
     * @param sender origin of the notification
     * @param data notification data
     * @return true if the filter matches the notification
     */

    /**
     * Check if the filter matches a notification
     * @param notification to filter
     * @return true if the notification matches the filter
     */
    boolean matches(RemoteNotification notification);
}
