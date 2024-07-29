package se.infomaker.frt.remotenotification.notification;

import android.app.Activity;
import android.content.Context;

import java.util.Map;

/**
 * Handle notifications when the user taps a notification in the notification tray
 */
public interface OnNotificationInteractionHandler {
    /**
     * @param activity
     * @param notification
     */
    void handleOpenNotification(Activity activity, Map<String, String> notification);

    /**
     * @param context
     * @param notification
     */
    void handleDeleteNotification(Context context, Map<String, String> notification);
}
