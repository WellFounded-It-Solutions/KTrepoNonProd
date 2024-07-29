package se.infomaker.streamviewer.notification;

import android.text.TextUtils;

import java.util.Map;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import se.infomaker.frt.remotenotification.NotificationFilter;
import se.infomaker.frt.remotenotification.RemoteNotification;
import se.infomaker.livecontentmanager.query.QueryManager;
import se.infomaker.storagemodule.Storage;
import se.infomaker.storagemodule.model.Subscription;
import se.infomaker.streamviewer.config.FollowConfig;
import se.infomaker.streamviewer.stream.StreamNotificationSettingsHandler;
import timber.log.Timber;

public class StreamNotificationFilter implements NotificationFilter {
    public static final String STREAM_CHECK = "streamCheck";
    private final QueryManager queryManager;
    private final FollowConfig followConfig;

    @AssistedInject
    public StreamNotificationFilter(QueryManager queryManager, @Assisted FollowConfig followConfig) {
        this.queryManager = queryManager;
        this.followConfig = followConfig;
    }

    @Override
    public boolean matches(RemoteNotification notification) {
        // TODO Only match stream created in this module instance
        Map<String, String> data = notification.getData();
        if (data == null) {
            Timber.d("Ignoring notification without data");
            return false;
        }
        String type = data.get("type");
        if (STREAM_CHECK.equals(type)) {
            String streamId = data.get("streamId");
            for (Subscription subscription : Storage.getSubscriptions()) {
                if (streamId.equals(subscription.getRemoteStreamId()) && subscription.getPushActivated() != null && subscription.getPushActivated()) {
                    return true;
                }
            }
            Timber.d("Removing dead stream %s", streamId);
            StreamNotificationSettingsHandler.deleteSubscriptionRemote(followConfig, queryManager, streamId);
            return true;
        }
        if (!"streamNotify".equals(type)) {
            Timber.d("Ignoring notification wrong type");
            return false;
        }
        String streamId = data.get("streamId");
        if (TextUtils.isEmpty(streamId)) {
            Timber.d("Ignoring notification no stream id");
            return false;
        }
        for (Subscription subscription : Storage.getSubscriptions()) {
            if (streamId.equals(subscription.getRemoteStreamId())) {
                Timber.d("Should handle notification");
                return true;
            }
        }
        StreamNotificationSettingsHandler.deleteSubscriptionRemote(followConfig, queryManager, streamId);
        Timber.d("Ignoring notification should unsubscribe");
        return false;
    }
}
