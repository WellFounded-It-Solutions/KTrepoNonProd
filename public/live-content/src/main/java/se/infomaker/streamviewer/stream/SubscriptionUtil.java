package se.infomaker.streamviewer.stream;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import io.realm.RealmResults;
import se.infomaker.livecontentmanager.query.LocationFilter;
import se.infomaker.livecontentmanager.query.MatchFilter;
import se.infomaker.livecontentmanager.query.QueryFilter;
import se.infomaker.livecontentui.config.LiveContentUIConfig;
import se.infomaker.storagemodule.Storage;
import se.infomaker.storagemodule.model.Subscription;
import com.navigaglobal.mobile.livecontent.R;
import se.infomaker.streamviewer.StatsHelper;
import se.infomaker.streamviewer.di.StreamNotificationSettingsHandlerFactory;

public class SubscriptionUtil {

    public static final String CONCEPT_TYPE = "concept";
    public static final String MATCH_TYPE = "match";

    public static QueryFilter createFilter(Subscription subscription, LiveContentUIConfig config) {
        switch (subscription.getType()) {
            case CONCEPT_TYPE: {
                return new MatchFilter(config.getLiveContent().getConceptField(), subscription.getValue("uuid"));
            }
            case MATCH_TYPE: {
                return new MatchFilter(subscription.getValue("field"), subscription.getValue("value"));
            }
            default: {
                throw new RuntimeException("Unsupported subscription type " + subscription.getType());
            }
        }
    }

    public static void putStatistics(@NotNull Subscription subscription, @NotNull final Bundle bundle) {
        switch (subscription.getType())
        {
            case "location": {
                bundle.putDouble(StatsHelper.LONGITUDE_ATTRIBUTE, subscription.getDouble("longitude"));
                bundle.putDouble(StatsHelper.LATITUDE_ATTRIBUTE, subscription.getDouble("latitude"));
                bundle.putFloat(StatsHelper.RADIUS_ATTRIBUTE, subscription.getFloat("radius"));
                break;
            }
            case "concept": {
                bundle.putString("moduleId", subscription.getValue("moduleId"));
                bundle.putString("name", subscription.getValue("name"));
                bundle.putString("uuid", subscription.getValue("uuid"));
                break;
            }
            default: {
                for (String key : subscription.allKeys()) {
                    bundle.putString(key, subscription.getValue(key));
                }
            }
        }
    }

    /**
     *
     * @param field to match on
     * @param value to match
     * @return whether a matching subscription exists.
     */
    public static boolean hasMatchSubscription(String field, String value) {
        for (Subscription subscription : Storage.getSubscriptions()) {
            String fieldValue = subscription.getValue("field");
            String valueValue = subscription.getValue("value");
            if ("match".equals(subscription.getType())
                    && field.equals(fieldValue)
                    && value.equals(valueValue)) {
                return true;
            }
        }
        return false;
    }

    public static Set<String> matchSubscriptions() {
        RealmResults<Subscription> subscriptions = Storage.getSubscriptions("match");
        HashSet<String> matchSubscriptions = new HashSet<>();
        for (Subscription subscription : subscriptions) {
            String fieldValue = subscription.getValue("field");
            String valueValue = subscription.getValue("value");
            if (!TextUtils.isEmpty(fieldValue) && !TextUtils.isEmpty(valueValue)) {
                matchSubscriptions.add(fieldValue + ":" + valueValue);
            }
        }
        return matchSubscriptions;
    }

    public static void inflateStreamMenu(Context context, Subscription subscription, Menu menu) {
        switch (subscription.getType()) {
            case CONCEPT_TYPE:
            case MATCH_TYPE: {
                new MenuInflater(context).inflate(R.menu.concept_popup, menu);
                break;
            }
        }
    }

    public static SubscriptionMenuHandler menuHandlerWith(View view, Activity activity, String moduleIdentifier, Subscription subscription, MenuItem notificationItem, ImageView notificationIcon, String viewName, StreamNotificationSettingsHandlerFactory settingsHandlerFactory) {
        switch (subscription.getType()) {
            case MATCH_TYPE:
            case CONCEPT_TYPE: {
                return new SubscriptionMenuHandler(view, activity, moduleIdentifier, subscription, notificationItem, notificationIcon, viewName, settingsHandlerFactory);
            }
        }
        throw new RuntimeException("Unsupported type");
    }
}
