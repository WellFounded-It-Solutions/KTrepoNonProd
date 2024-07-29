package se.infomaker.streamviewer.notification;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;
import androidx.core.text.HtmlCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dagger.hilt.android.qualifiers.ApplicationContext;
import se.infomaker.frt.remotenotification.OnRemoteNotificationListener;
import se.infomaker.frt.remotenotification.RemoteNotification;
import se.infomaker.frt.remotenotification.notification.NotificationIntentFactory;
import se.infomaker.frt.remotenotification.notification.NotificationUtil;
import se.infomaker.frt.statistics.StatisticsEvent;
import se.infomaker.frt.statistics.StatisticsManager;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.DateUtil;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.frtutilities.NotificationAudioHelper;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.ktx.ThemeUtils;
import se.infomaker.livecontentui.livecontentrecyclerview.utils.DefaultUtils;
import se.infomaker.storagemodule.Storage;
import se.infomaker.storagemodule.model.Subscription;
import se.infomaker.streamviewer.config.FollowConfig;
import se.infomaker.streamviewer.config.RemoteNotificationConfig;
import timber.log.Timber;

public class StreamNotificationListener implements OnRemoteNotificationListener {
    private static final AtomicInteger nextId = new AtomicInteger(5000);

    static final String SHARED_PREFERENCES_KEY = "streamNotification";
    static final String GROUP_ID_SHARED_PREFERENCES_KEY = "notificationChannelGroupIds";
    private static final String SHARED_PREFERENCES_KEY_NOTIFICATION_ID = "notificationId";
    private static final String SHARED_PREFERENCES_KEY_TIMESTAMP = "timestamp";
    static final String SHARED_PREFERENCES_KEY_INTERACTED = "interacted";
    private static final long[] VIBRATION_PATTERN = new long[]{100, 50, 100};

    private final RemoteNotificationConfig config;
    private final Context context;
    private final NotificationIntentFactory intentFactory;
    private final String identifier;

    @AssistedInject
    public StreamNotificationListener(@ApplicationContext Context context, NotificationIntentFactory intentFactory, @Assisted String moduleIdentifier) {
        this.context = context;
        this.intentFactory = intentFactory;
        identifier = moduleIdentifier;
        config = ConfigManager.getInstance(context.getApplicationContext()).getConfig(moduleIdentifier, FollowConfig.class).getRemoteNotification();
    }

    @Override
    public void onNotification(RemoteNotification notification) {
        Timber.d("Got notified: %s", notification);
        try {
            String type = notification.getData().get("type");
            if (StreamNotificationFilter.STREAM_CHECK.equals(type)) {
                Timber.d("Stream alive");
                return;
            }
            JSONObject properties = new JSONObject(notification.getData().get("properties"));

            // Make sure we reuse the same notification ID to update notification for article
            SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

            String contentId = properties.getJSONArray(config.getContentIdKey()).getString(0);

            String eventType = getEventType(properties);
            if ("DELETE".equals(eventType)) {
                JSONObject persistedNotification = new JSONObject(sharedPref.getString(contentId, "{}"));
                int notificationId = persistedNotification.getInt(SHARED_PREFERENCES_KEY_NOTIFICATION_ID);
                deleteSentNotification(notificationId);
                return;
            }

            CharSequence title = null;
            if (config.getTitleKey() != null && properties.has(config.getTitleKey())) {
                title = HtmlCompat.fromHtml(properties.getJSONArray(config.getTitleKey()).getString(0), HtmlCompat.FROM_HTML_MODE_LEGACY);
            }
            Date pubDate = getPubdate(properties);
            CharSequence subtitle = null;
            if (config.getSubtitleKey() != null && properties.has(config.getSubtitleKey())) {
                subtitle = HtmlCompat.fromHtml(properties.getJSONArray(config.getSubtitleKey()).getString(0), HtmlCompat.FROM_HTML_MODE_LEGACY);
            }

            if (sharedPref.contains(contentId)) {
                JSONObject persistedNotification = new JSONObject(sharedPref.getString(contentId, "{}"));

                int notificationId = persistedNotification.getInt(SHARED_PREFERENCES_KEY_NOTIFICATION_ID);
                long timestamp = persistedNotification.getLong(SHARED_PREFERENCES_KEY_TIMESTAMP);
                boolean interacted = persistedNotification.getBoolean(SHARED_PREFERENCES_KEY_INTERACTED);

                Date addedDate = new Date(timestamp);
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.HOUR, -24);
                if (addedDate.after(calendar.getTime())) {
                    // If added less than 24 hours ago, check if user has canceled or read notification
                    if (!interacted) {
                        // Update notification if user hasn't interacted with it
                        sendNotification(title, subtitle, notification.getData(), notificationId, pubDate, contentId);
                    }
                } else {
                    // If added more than 24 hours ago, treat it as a new notification
                    persistedNotification.put(SHARED_PREFERENCES_KEY_TIMESTAMP, new Date().getTime());
                    persistNotification(contentId, persistedNotification);
                    sendNotification(title, subtitle, notification.getData(), notificationId, pubDate, contentId);
                }
            } else {
                int notificationId = nextId.getAndIncrement();
                JSONObject newNotification = new JSONObject();
                newNotification.put(SHARED_PREFERENCES_KEY_NOTIFICATION_ID, notificationId);
                newNotification.put(SHARED_PREFERENCES_KEY_TIMESTAMP, new Date().getTime());
                newNotification.put(SHARED_PREFERENCES_KEY_INTERACTED, false);
                persistNotification(contentId, newNotification);
                sendNotification(title, subtitle, notification.getData(), notificationId, pubDate, contentId);
            }
        } catch (JSONException e) {
            Timber.d(e, "Failed to handle notification");
        }
    }

    private static String getEventType(JSONObject properties) {
        JSONArray eventType = properties.optJSONArray("eventtype");
        if (eventType != null) {
            return eventType.optString(0);
        }
        return null;
    }

    private Date getPubdate(JSONObject properties) {
        String dateString = null;
        try {
            String pubdateKey = config.getPubdateKey();
            if (pubdateKey != null) {
                dateString = properties.getJSONArray(pubdateKey).getString(0);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Date date = null;
        if (dateString != null) {

            date = DateUtil.getDateFromString(dateString);

        }
        return date != null ? date : new Date();
    }

    private void persistNotification(String contentId, JSONObject notification) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(contentId, notification.toString());
        editor.apply();
    }

    private void sendNotification(CharSequence title, CharSequence notificationText, Map<String, String> data, int id, Date pubDate, String contentId) {
        if (System.currentTimeMillis() - pubDate.getTime() > 24 * 60 * 60 * 1000) {
            Timber.d("Ignoring notification (to old): " + title + " - " + notificationText);
            return;
        }

        Timber.d("Notify: " + title + " " + notificationText);
        ResourceManager manager = new ResourceManager(context, identifier);
        String streamId = data.get("streamId");

        if (streamId == null) {
            streamId = "default";
        }

        Subscription subscription = getStream(streamId);
        String streamName = "";
        StatisticsEvent.Builder builder = new StatisticsEvent.Builder();
        if (subscription != null) {
            streamName = subscription.getName();
            for (String key : subscription.allKeys()) {
                builder.attribute(StatisticsNotificationHelper.keyToNew(key), subscription.getValue(key));
            }
        }

        builder.event("showNotification")
                .attribute("title", title)
                .attribute("text", notificationText)
                .attribute("notificationTitle", title)
                .attribute("notificationText", notificationText)
                .attribute("contentId", contentId)
                .moduleId(identifier)
                .moduleName(ModuleInformationManager.getInstance().getModuleName(identifier))
                .moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(identifier))
                .attribute("streamName", streamName);

        StatisticsManager.getInstance().logEvent(builder.build());

        String channelId = streamName != null ? streamId : manager.getModuleIdentifier();
        String channelName = streamName != null ? streamName : ModuleInformationManager.getInstance().getModuleName(identifier);
        String groupId = manager.getModuleIdentifier();
        String groupName = ModuleInformationManager.getInstance().getModuleTitle(identifier);

        NotificationAudioHelper audioHelper = new NotificationAudioHelper(manager);
        Uri soundUri = audioHelper.audioResourceUriOrNull(context.getPackageName(), manager.getModuleIdentifier(), streamName != null ? streamName : "Default");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.enableLights(true);
                notificationChannel.setVibrationPattern(VIBRATION_PATTERN);
                if (soundUri != null) {
                    notificationChannel.setSound(soundUri, NotificationAudioHelper.Companion.getAudioAttributes());
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    NotificationChannelGroup channelGroup = notificationManager.getNotificationChannelGroup(groupId);
                    if (channelGroup == null) {
                        notificationManager.createNotificationChannelGroup(new NotificationChannelGroup(groupId, groupName));
                    }
                    notificationChannel.setGroup(groupId);
                }
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        Theme appTheme = ThemeManager.getInstance(context).getAppTheme();
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, streamId)
                        .setAutoCancel(true)
                        .setWhen(pubDate.getTime())
                        .setContentTitle(title)
                        .setColor(ThemeUtils.getBrandColor(appTheme).get())
                        .setContentText(notificationText)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setOnlyAlertOnce(true);

        if (NotificationUtil.shouldVibrateOnNotification(context, identifier)) {
            notificationBuilder.setVibrate(VIBRATION_PATTERN);
        }
        if (NotificationUtil.shouldPlayNotificaitonSound(context, identifier)) {
            if (soundUri != null) {
                notificationBuilder.setSound(soundUri);
                notificationBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
            } else {
                notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS);
            }
        } else {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        }

        //Small icon
        int ic_stat_notify = manager.getDrawableIdentifier("ic_stat_notify");
        if (ic_stat_notify > 0) {
            Bitmap bitmap = DefaultUtils.drawableToBitmap(AppCompatResources.getDrawable(context, ic_stat_notify));
            if (bitmap != null && bitmap.getWidth() > 0 && bitmap.getHeight() > 0) {
                notificationBuilder.setSmallIcon(ic_stat_notify);
            } else {
                Timber.e("Something went wrong when trying to set the small icon");
            }
        }

        //Large icon
        int notify_icon = manager.getDrawableIdentifier("notify_icon");
        Bitmap largeIcon = null;
        if (notify_icon > 0) {
            Drawable drawable = AppCompatResources.getDrawable(context, notify_icon);
            largeIcon = DefaultUtils.drawableToBitmap(drawable);
        }
        Bitmap fallbackLargeIcon = BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier("ic_launcher", "mipmap", context.getPackageName()));
        if (largeIcon != null && largeIcon.getWidth() > 0 && largeIcon.getHeight() > 0) {
            notificationBuilder.setLargeIcon(largeIcon);
        } else if (fallbackLargeIcon != null && fallbackLargeIcon.getWidth() > 0 && fallbackLargeIcon.getHeight() > 0) {
            notificationBuilder.setLargeIcon(fallbackLargeIcon);
        }

        notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(notificationText).setBigContentTitle(title).setSummaryText(streamName));

        PendingIntent deepLinkIntent = intentFactory.createDeepLinkIntent(context, identifier, data);
        notificationBuilder.setContentIntent(deepLinkIntent);
        PendingIntent deleteIntent = intentFactory.createDeleteIntent(context, identifier, data);
        notificationBuilder.setDeleteIntent(deleteIntent);
        notificationManager.notify(id, notificationBuilder.build());
    }

    private void deleteSentNotification(int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    private Subscription getStream(String streamId) {
        if (!TextUtils.isEmpty(streamId)) {
            for (Subscription subscription : Storage.getSubscriptions()) {
                if (streamId.equals(subscription.getRemoteStreamId())) {
                    return subscription;
                }
            }
        }
        return null;
    }
}
