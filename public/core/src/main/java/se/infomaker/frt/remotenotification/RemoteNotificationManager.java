package se.infomaker.frt.remotenotification;

import java.util.ArrayList;

public class RemoteNotificationManager {
    private static final RemoteNotificationManager INSTANCE = new RemoteNotificationManager();

    private final ArrayList<RemoteNotificationInterceptor> interceptors = new ArrayList<>();

    public static RemoteNotificationManager instance() {
        return INSTANCE;
    }

    private RemoteNotificationManager() {

    }

    public static void registerInterceptor(RemoteNotificationInterceptor interceptor) {
        INSTANCE.interceptors.add(interceptor);
    }

    public static RemoteNotification intercept(RemoteNotification notification) throws AbortNotificationException {
        for (RemoteNotificationInterceptor interceptor : INSTANCE.interceptors) {
            notification = interceptor.intercept(notification);
        }
        return notification;
    }
}
