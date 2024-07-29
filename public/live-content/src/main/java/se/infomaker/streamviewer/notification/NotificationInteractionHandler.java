package se.infomaker.streamviewer.notification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import se.infomaker.frt.remotenotification.notification.OnNotificationInteractionHandler;
import se.infomaker.frt.statistics.StatisticsEvent;
import se.infomaker.frt.statistics.StatisticsManager;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.livecontentmanager.query.FilterHelper;
import se.infomaker.livecontentmanager.query.MatchFilter;
import se.infomaker.livecontentmanager.query.QueryFilter;
import se.infomaker.livecontentui.livecontentdetailview.activity.ArticlePagerActivity;
import se.infomaker.livecontentui.livecontentrecyclerview.activity.LiveContentRecyclerviewActivity;
import se.infomaker.storagemodule.Storage;
import se.infomaker.storagemodule.model.Subscription;
import se.infomaker.streamviewer.StatsHelper;
import se.infomaker.streamviewer.config.FollowConfig;
import se.infomaker.streamviewer.stream.SubscriptionUtil;
import timber.log.Timber;

public class NotificationInteractionHandler implements OnNotificationInteractionHandler {
    private final String moduleIdentifier;

    public NotificationInteractionHandler(String identifier) {
        this.moduleIdentifier = identifier;
    }

    @Override
    public void handleOpenNotification(Activity activity, Map<String, String> notification) {
        String streamId = notification.get("streamId");
        FollowConfig config = ConfigManager.getInstance(activity).getConfig(moduleIdentifier, FollowConfig.class);

        saveNotificationInteraction(activity, notification, config);

        QueryFilter filter = null;
        Subscription sub = null;
        for (Subscription subscription : Storage.getSubscriptions()) {
            if (streamId.equals(subscription.getRemoteStreamId())) {
                sub = subscription;
                filter = SubscriptionUtil.createFilter(subscription, config);
                break;
            }
        }
        if (sub == null || filter == null) {
            Timber.w("Could not find stream");
            return;
        }
        List<QueryFilter> filters = new ArrayList<>();
        filters.add(filter);
        try {

            JSONObject properties = new JSONObject(notification.get("properties"));
            String contentIdKey = config.getRemoteNotification().getContentIdKey();
            String id = properties.getJSONArray(contentIdKey).getString(0);
            filters.add(new MatchFilter(contentIdKey, id));

            Intent articleIntent = new Intent(activity, ArticlePagerActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("hitsListListId", moduleIdentifier);
            bundle.putString("title", sub.getName());

            FilterHelper.put(articleIntent, filters);
            articleIntent.putExtras(bundle);

            logNotificationEventStatistic(properties, config, id, sub);
            activity.startActivity(articleIntent);

        } catch (JSONException e) {
            Timber.e(e, "Could not handle notification");
        }
    }

    private void logNotificationEventStatistic(JSONObject properties, FollowConfig config, String id, Subscription sub) {
        String title = getString(properties, config.getRemoteNotification().getTitleKey());
        String text = getString(properties, config.getRemoteNotification().getSubtitleKey());

        StatisticsEvent.Builder builder = new StatisticsEvent.Builder().event(StatsHelper.OPEN_NOTIFICATION_EVENT)
                .attribute("title", title)
                .attribute("text", text)
                .attribute("notificationTitle", title)
                .attribute("notificationText", text)
                .attribute("contentId", id)
                .moduleId(moduleIdentifier)
                .moduleName(ModuleInformationManager.getInstance().getModuleName(moduleIdentifier))
                .moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleIdentifier))
                .attribute("streamName", sub.getName());
        for (String key : sub.allKeys()) {
            builder.attribute(StatisticsNotificationHelper.keyToNew(key), sub.getValue(key));
        }
        StatisticsManager.getInstance().logEvent(builder.build());
    }

    private String getString(JSONObject properties, String key) {
        try {
            return properties.getJSONArray(key).getString(0);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void handleDeleteNotification(Context context, Map<String, String> notification) {
        FollowConfig config = ConfigManager.getInstance(context).getConfig(moduleIdentifier, FollowConfig.class);
        saveNotificationInteraction(context, notification, config);
    }

    private void saveNotificationInteraction(Context context, Map<String, String> notification, FollowConfig config) {
        try {
            JSONObject properties = new JSONObject(notification.get("properties"));
            String contentIdKey = config.getRemoteNotification().getContentIdKey();
            String contendId = properties.getJSONArray(contentIdKey).getString(0);

            SharedPreferences sharedPref = context.getSharedPreferences(StreamNotificationListener.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
            JSONObject notificationJSON;
            if (sharedPref.contains(contendId)) {
                notificationJSON = new JSONObject(sharedPref.getString(contendId, "{}"));

            } else {
                notificationJSON = new JSONObject();
            }
            notificationJSON.put(StreamNotificationListener.SHARED_PREFERENCES_KEY_INTERACTED, true);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(contendId, notificationJSON.toString());
            editor.commit();
        } catch (JSONException e) {
            Timber.e(e, "Could not handle notification");
        }
    }
}