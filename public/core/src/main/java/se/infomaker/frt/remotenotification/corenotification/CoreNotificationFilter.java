package se.infomaker.frt.remotenotification.corenotification;

import android.text.TextUtils;

import java.util.Map;

import javax.inject.Inject;

import se.infomaker.frt.remotenotification.NotificationFilter;
import se.infomaker.frt.remotenotification.RemoteNotification;
import timber.log.Timber;

public class CoreNotificationFilter implements NotificationFilter {

    @Inject
    public CoreNotificationFilter() {}

    @Override
    public boolean matches(RemoteNotification notification) {
        Map<String, String> data = notification.getData();
        if (data == null) {
            Timber.d("Ignoring notification without data");
            return false;
        }

        if (TextUtils.isEmpty(data.get("message")) && TextUtils.isEmpty(data.get("title"))) {
            Timber.d("Ignoring notification no message field");
            return false;
        }
        return true;
    }
}
