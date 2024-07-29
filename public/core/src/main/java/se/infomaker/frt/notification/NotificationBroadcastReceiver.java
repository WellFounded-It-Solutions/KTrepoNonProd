package se.infomaker.frt.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import se.infomaker.frt.remotenotification.notification.NotificationIntentFactory;
import se.infomaker.frt.remotenotification.notification.OnNotificationInteractionHandler;

@AndroidEntryPoint
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    @Inject Map<String, OnNotificationInteractionHandler> notificationHandlers;

    @Override
    public void onReceive(Context context, Intent intent) {
        String moduleId = intent.getStringExtra(NotificationIntentFactory.MODULE_ID);
        Map<String, String> notificationBundle = (HashMap<String, String>) intent.getSerializableExtra(NotificationIntentFactory.NOTIFICATION_DATA);
        if (notificationBundle != null) {
            OnNotificationInteractionHandler handler = notificationHandlers.get(moduleId);
            if (handler != null) {
                handler.handleDeleteNotification(context, notificationBundle);
            }
        }
    }
}
