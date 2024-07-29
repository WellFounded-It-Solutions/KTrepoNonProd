package se.infomaker.frt.remotenotification;

import android.content.Context;

import se.infomaker.frt.remotenotification.notification.OnNotificationInteractionHandler;

public interface OnRemoteNotificationListenerFactory {
    /**
     *
     * @param context
     * @param moduleIdentifier
     * @return aRemoteNotificationListener for the module instance or null if not valid
     */
    OnRemoteNotificationListener create(Context context, String moduleIdentifier);

    /**
     *
     * @param context
     * @param moduleIdentifier
     * @return aRemoteNotificationListener for the module instance or null if not valid
     */
    NotificationFilter[] createFilters(Context context, String moduleIdentifier);

    /**
     * Creates a notification handler that can handle opening notifications from notification tray
     * @param context
     * @param moduleIdentifier
     * @return
     */
    OnNotificationInteractionHandler createNotificationHandler(Context context, String moduleIdentifier);
}
