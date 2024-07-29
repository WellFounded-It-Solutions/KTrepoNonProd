package se.infomaker.livecontentui.livecontentrecyclerview.notification

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import se.infomaker.frt.remotenotification.OnRemoteNotificationListener
import se.infomaker.frt.remotenotification.RemoteNotification
import se.infomaker.frt.remotenotification.notification.NotificationIntentFactory
import se.infomaker.frt.remotenotification.notification.NotificationUtil
import se.infomaker.frt.statistics.StatisticsEvent
import se.infomaker.frt.statistics.StatisticsManager
import se.infomaker.frtutilities.ModuleInformationManager
import se.infomaker.frtutilities.NotificationAudioHelper
import se.infomaker.frtutilities.ResourceManager
import se.infomaker.frtutilities.TextUtils
import se.infomaker.iap.theme.ThemeManager
import se.infomaker.iap.theme.ktx.brandColor
import se.infomaker.livecontentui.livecontentrecyclerview.utils.DefaultUtils
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

class ContentListNotificationListener @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    private val intentFactory: NotificationIntentFactory,
    @Assisted private val moduleIdentifier: String
) : OnRemoteNotificationListener {

    override fun onNotification(notification: RemoteNotification) {
        Timber.d("Got notified: %s", notification)

        val title = notification.data["title"]
        val subtitle = notification.data["message"] ?: ""

        sendNotification(title, subtitle, notification.data)
    }

    private fun sendNotification(title: String?, notificationText: String, data: Map<String, String>) {
        val uuid = data["uuid"] ?: ""
        synchronized(shownNotifications) {
            val notificationKey = "$title:$notificationText:$uuid"
            if (shownNotifications.contains(notificationKey)) {
                return
            }
            shownNotifications.add(notificationKey)
        }

        Timber.d("Notify: $title $notificationText")
        val manager = ResourceManager(context, moduleIdentifier)

        val deepLinkIntent = intentFactory.createDeepLinkIntent(context, moduleIdentifier, data)

        val audioHelper = NotificationAudioHelper(manager)
        val notificationAudioUri = audioHelper.audioResourceUriOrNull(context.packageName, manager.moduleIdentifier, data["context"])

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(moduleIdentifier) == null) {

                val notificationChannel = NotificationChannel(
                    moduleIdentifier,
                    ModuleInformationManager.getInstance().getModuleTitle(moduleIdentifier) ?: moduleIdentifier.uppercase(),
                    NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.enableLights(true)
                notificationChannel.vibrationPattern = VIBRATION_PATTERN
                notificationAudioUri?.let {
                    notificationChannel.setSound(it, NotificationAudioHelper.audioAttributes)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val channelGroup = notificationManager.getNotificationChannelGroup(CHANNEL_GROUP_ID)
                    if (channelGroup == null) {
                        notificationManager.createNotificationChannelGroup(
                            NotificationChannelGroup(
                                CHANNEL_GROUP_ID,
                                CHANNEL_GROUP_NAME
                            )
                        )
                    }
                    notificationChannel.group = CHANNEL_GROUP_ID
                }
                notificationManager.createNotificationChannel(notificationChannel)
            }
        }

        val appTheme = ThemeManager.getInstance(context).appTheme
        val notificationBuilder = NotificationCompat.Builder(context, moduleIdentifier)
                .setAutoCancel(true)
                .setColor(appTheme.brandColor.get())
                .setPriority(NotificationCompat.PRIORITY_HIGH)

        if (!TextUtils.isEmpty(title)) {
            notificationBuilder.setContentTitle(title)
        }

        if (NotificationUtil.shouldVibrateOnNotification(context, moduleIdentifier)) {
            notificationBuilder.setVibrate(VIBRATION_PATTERN)
        }

        notificationBuilder.setContentText(notificationText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationText))
                .setContentIntent(deepLinkIntent)

        if (NotificationUtil.shouldPlayNotificaitonSound(context, moduleIdentifier)) {
            notificationAudioUri?.let {
                notificationBuilder.setSound(it)
                notificationBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS)
            } ?: run {
                notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_LIGHTS)
            }
        } else {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS)
        }

        val smallIconResId = manager.getDrawableIdentifier("ic_stat_notify")
        if (smallIconResId > 0) {
            val bitmap = DefaultUtils.drawableToBitmap(AppCompatResources.getDrawable(context, smallIconResId))
            if(bitmap != null && bitmap.width > 0 && bitmap.height > 0) {
                notificationBuilder.setSmallIcon(manager.getDrawableIdentifier("ic_stat_notify"))
            } else {
                Timber.e("Something went wrong when trying to set the small icon")
            }
        }

        var largeIcon: Bitmap? = null
        val largeIconResId = manager.getDrawableIdentifier("notify_icon")
        if (largeIconResId > 0) {
            val drawable = AppCompatResources.getDrawable(context, largeIconResId)
            largeIcon = DefaultUtils.drawableToBitmap(drawable)
        }

        if (largeIcon != null && largeIcon.width > 0 && largeIcon.height > 0) {
            notificationBuilder.setLargeIcon(largeIcon)
        } else {
            val fallbackLargeIcon = BitmapFactory.decodeResource(context.resources, context.resources.getIdentifier("ic_launcher", "mipmap", context.packageName))
            if (fallbackLargeIcon != null && fallbackLargeIcon.width > 0 && fallbackLargeIcon.height > 0) {
                notificationBuilder.setLargeIcon(fallbackLargeIcon)
            }
        }

        StatisticsEvent.Builder().event("showNotification")
            .attribute("title", title)
            .attribute("text", notificationText)
            .attribute("notificationTitle", title)
            .attribute("notificationText", notificationText)
            .attribute("contentId", uuid)
            .moduleId(moduleIdentifier)
            .moduleName(ModuleInformationManager.getInstance().getModuleName(moduleIdentifier))
            .moduleTitle(ModuleInformationManager.getInstance().getModuleTitle(moduleIdentifier))
            .build()
            .also {
                StatisticsManager.getInstance().logEvent(it)
            }

        val deleteIntent = intentFactory.createDeleteIntent(context, moduleIdentifier, data)
        notificationBuilder.setDeleteIntent(deleteIntent)

        notificationManager.notify(UUID.randomUUID().toString(), currentId.andDecrement, notificationBuilder.build())
    }

    companion object {
        val shownNotifications = mutableListOf<String>()
        private val currentId = AtomicInteger()
        private val VIBRATION_PATTERN = longArrayOf(100, 50, 100)
        private const val GROUP_ID_SHARED_PREFERENCES_KEY = "notificationChannelGroupIds"
        private const val CHANNEL_GROUP_ID = "newsfeed"
        private const val CHANNEL_GROUP_NAME = "News Feed"
    }
}