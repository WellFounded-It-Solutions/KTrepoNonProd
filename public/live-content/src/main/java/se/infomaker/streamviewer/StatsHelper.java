package se.infomaker.streamviewer;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import se.infomaker.frt.statistics.StatisticsEvent;
import se.infomaker.frt.statistics.StatisticsManager;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.storagemodule.model.Subscription;
import se.infomaker.streamviewer.notification.StatisticsNotificationHelper;
import timber.log.Timber;

public class StatsHelper {
    public static final String DELETE_STREAM_EVENT = "deleteStream";
    public static final String CREATE_STREAM_EVENT = "createStream";
    public static final String EDIT_STREAM_EVENT = "editStream";
    public static final String START_LOCATION_PICKER = "startLocationPicker";
    public static final String NOTIFICATIONS_ACTIVATED_EVENT = "notificationsActivated";
    public static final String NOTIFICATIONS_DEACTIVATED_EVENT = "notificationsDeactivated";
    public static final String OPEN_NOTIFICATION_EVENT = "openNotification";
    public static final String VIEW_SHOW_EVENT = "viewShow";
    public static final String VIEW_CANCEL_EVENT = "viewCancel";
    public static final String LOCATION_PICKER_VIEW = "locationPicker";
    public static final String TOPIC_PICKER_VIEW = "topicPicker";
    public static final String STREAMS_VIEW = "streams";
    public static final String ARTICLE_VIEW = "article";

    private static final String TAG = StatsHelper.class.getSimpleName();
    public static final String LATITUDE_ATTRIBUTE = "latitude";
    public static final String LONGITUDE_ATTRIBUTE = "longitude";
    public static final String RADIUS_ATTRIBUTE = "radius";

    public static void logSubscriptionEvent(String moduleId, String event, String viewName, Map<String, Object> attributes) {

        StatisticsEvent.Builder builder = new StatisticsEvent.Builder()
                .moduleId(moduleId)
                .moduleName(ModuleInformationManager.getInstance().getModuleName(moduleId))
                .moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleId))
                .event(event);

        if (!TextUtils.isEmpty(viewName)) {
            builder.viewName(viewName);
        }
        if (attributes != null) {
            for (String key : attributes.keySet()) {
                builder.attribute(StatisticsNotificationHelper.keyToNew(key), attributes.get(key));
            }
        }
        StatisticsManager.getInstance().logEvent(builder.build());
    }

    public static void logSubscriptionEvent (String moduleId, String event, String viewName, Subscription subscription) {
        HashMap<String, Object> attributes = new HashMap<>();
        for (String key : subscription.allKeys()) {
            attributes.put(key, subscription.getValue(key));
        }
        attributes.putAll(subscription.statisticsAttributes());
        Timber.e("StatsHelper: ModuleID: %s,, ViewName: %s", moduleId, viewName);
        logSubscriptionEvent(moduleId, event, viewName, attributes);
    }
}
