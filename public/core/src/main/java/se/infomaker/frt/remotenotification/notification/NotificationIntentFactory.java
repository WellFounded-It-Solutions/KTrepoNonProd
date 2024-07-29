package se.infomaker.frt.remotenotification.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.TaskStackBuilder;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import se.infomaker.frt.notification.NotificationBroadcastReceiver;
import se.infomaker.frt.ui.activity.MainActivity;
import se.infomaker.iap.SpringBoardActivity;

public class NotificationIntentFactory {

    public static final String MODULE_ID = "module_id";
    public static final String NOTIFICATION_DATA = "notification";

    @Inject
    public NotificationIntentFactory() {
    }

    /**
     * Creates a deep link intent,
     * The data will be delivered to the module fragment when the user taps the notification
     *
     * @param context
     * @param moduleId
     * @return
     */
    public PendingIntent createDeepLinkIntent(Context context, String moduleId, Map<String, String> data) {
        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(context, SpringBoardActivity.class);

        HashMap notificationData;
        if(data != null) {
            notificationData = new HashMap(data);
        } else {
            notificationData = new HashMap();
        }

        intent.putExtra(MODULE_ID, moduleId);
        intent.putExtra(NOTIFICATION_DATA, notificationData);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);

        return stackBuilder.getPendingIntent((int) System.currentTimeMillis(), applyImmutabilityFlagIfPossible(PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private int applyImmutabilityFlagIfPossible(int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return flags;
    }

    /**
     * Creates a delete intent,
     * The data will be delivered to the module fragment when the notification is cleared by the user
     *
     * @param context
     * @param moduleId
     * @return
     */
    public PendingIntent createDeleteIntent(Context context, String moduleId, Map<String, String> data) {
        // Creates an explicit intent for an Activity in your app
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);

        intent.putExtra(MODULE_ID, moduleId);
        intent.putExtra(NOTIFICATION_DATA, new HashMap(data));
        intent.setAction("notification_cancelled");

        return PendingIntent.getBroadcast(context, 0, intent, applyImmutabilityFlagIfPossible(PendingIntent.FLAG_CANCEL_CURRENT));
    }
}
