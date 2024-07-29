package se.infomaker.frt.remotenotification;

import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import se.infomaker.frt.remotenotification.corenotification.CoreNotificationFilter;
import se.infomaker.frt.remotenotification.corenotification.CoreNotificationListener;
import timber.log.Timber;

/**
 * Keeps a register of remote notification listeners and routes notifications to
 * listeners with matching filters
 */
@Singleton
public class RemoteNotificationRouter {

    private final SharedPreferences preferences;
    private final CoreNotificationListener coreNotificationListener;
    private final CoreNotificationFilter coreNotificationFilter;
    private final LinkedHashMap<OnRemoteNotificationListener, Set<NotificationFilter>> register;

    @Inject
    public RemoteNotificationRouter(
        SharedPreferences preferences,
        LinkedHashMap<OnRemoteNotificationListener, Set<NotificationFilter>> defaultRegister,
        CoreNotificationListener coreNotificationListener,
        CoreNotificationFilter coreNotificationFilter
    ) {
        this.preferences = preferences;
        this.register = defaultRegister;
        this.coreNotificationListener = coreNotificationListener;
        this.coreNotificationFilter = coreNotificationFilter;
    }

    /**
     * Register listeners for any filters, if the listener is already registered the filters are
     * appended to already present filters
     *
     * @param listener to add
     * @param filters  to assign to the listener
     */
    public synchronized void register(OnRemoteNotificationListener listener, NotificationFilter... filters) {
        Set<NotificationFilter> notificationFilters = register.get(listener);
        if (notificationFilters == null) {
            notificationFilters = new HashSet<>();
            register.put(listener, notificationFilters);
        }
        Collections.addAll(notificationFilters, filters);
    }

    /**
     * Unregister listener so that it does not receive any more notifications
     *
     * @param listener to unregister
     */
    public synchronized void unregister(OnRemoteNotificationListener listener) {
        register.remove(listener);
    }

    /**
     * Route a notification to all listeners with matching filters
     *
     * @param notification received
     * @return true if the message sent to any listener
     */
    public synchronized boolean route(RemoteNotification notification) {
        // TODO refactor settings
        boolean receivePush = preferences.getBoolean("pref_receive_push", true);
        if (!receivePush) {
            Timber.d("Push disabled, ignoring notification: %s", notification);
            return false;
        }

        try {
            notification = RemoteNotificationManager.intercept(notification);
        } catch (AbortNotificationException e) {
            Timber.d("Remote notification aborted: %s", e.getLocalizedMessage());
            return true;
        }
        boolean routed = false;
        for (OnRemoteNotificationListener listener : register.keySet()) {
            for (NotificationFilter filter : register.get(listener)) {
                if (filter.matches(notification)) {
                    listener.onNotification(notification);
                    routed = true;
                    break;
                }
            }
        }
        if (!routed && coreNotificationFilter.matches(notification)) {
            coreNotificationListener.onNotification(notification);
            routed = true;
        }
        return routed;
    }
}
