package se.infomaker.iap.push.google;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import se.infomaker.frt.remotenotification.PushRegistrationManager;
import se.infomaker.frt.remotenotification.RemoteNotification;
import se.infomaker.frt.remotenotification.RemoteNotificationRouter;
import timber.log.Timber;

/**
 * Passes received messages to router
 */
@AndroidEntryPoint
public class MessageListenerService extends FirebaseMessagingService {

    @Inject PushRegistrationManager pushRegistrationManager;
    @Inject RemoteNotificationRouter notificationRouter;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Map<String, String> data = remoteMessage.getData();
        notificationRouter.route(new RemoteNotification(from, data));
    }

    @Override
    public void onNewToken(@NonNull String newToken) {
        Timber.d("New FCM token: %s", newToken);
        if (FCMUtil.isTokenUpdated(getApplicationContext(), newToken)) {
            FCMUtil.setToken(getApplicationContext(), newToken);
            FCMUtil.removeRegistration(getApplicationContext());
        }
        pushRegistrationManager.ensureRegistered();
    }
}
