package se.infomaker.frt.remotenotification.corenotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.NotificationCompat;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import se.infomaker.frt.remotenotification.OnRemoteNotificationListener;
import se.infomaker.frt.remotenotification.RemoteNotification;
import se.infomaker.frt.remotenotification.notification.NotificationIntentFactory;
import se.infomaker.frtutilities.NotificationAudioHelper;
import se.infomaker.frtutilities.ResourceManager;
import se.infomaker.frtutilities.TextUtils;
import se.infomaker.iap.theme.Theme;
import se.infomaker.iap.theme.ThemeManager;
import se.infomaker.iap.theme.color.ThemeColor;
import se.infomaker.iap.theme.ktx.ThemeUtils;

public class CoreNotificationListener implements OnRemoteNotificationListener {
    private static final long[] VIBRATION_PATTERN = new long[]{100, 50, 100};
    private final Context context;
    private final NotificationIntentFactory intentFactory;
    private final AtomicInteger currentId = new AtomicInteger(1000);

    @Inject
    public CoreNotificationListener(@ApplicationContext Context context, NotificationIntentFactory intentFactory) {
        this.context = context;
        this.intentFactory = intentFactory;
    }

    @Override
    public void onNotification(RemoteNotification notification) {
        String title = notification.getData().get("title");
        String subtitle = notification.getData().get("message");

        sendNotification(title, subtitle);
    }

    private void sendNotification(String title, String notificationText) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        ResourceManager manager = new ResourceManager(context, null);
        PendingIntent openAppIntent = intentFactory.createDeepLinkIntent(context, null, null);

        NotificationAudioHelper audioHelper = new NotificationAudioHelper(manager);
        Uri soundUri = audioHelper.audioResourceUriOrNull(context.getPackageName(), manager.getModuleIdentifier(), null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel("default") == null) {
            NotificationChannel notificationChannel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setVibrationPattern(VIBRATION_PATTERN);
            if (soundUri != null) {
                notificationChannel.setSound(soundUri, NotificationAudioHelper.Companion.getAudioAttributes());
            }
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Theme appTheme = ThemeManager.getInstance(context).getAppTheme();
        ThemeColor themeIconTint = appTheme.getColor("notificationIconTint", null);
        int notificationIconTint = themeIconTint != null ? themeIconTint.get() : ThemeUtils.getBrandColor(appTheme).get();
        NotificationCompat.Style style = new NotificationCompat.BigTextStyle().bigText(notificationText);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, "default")
                .setAutoCancel(true)
                .setSmallIcon(manager.getDrawableIdentifier("ic_stat_notify"))
                .setColor(notificationIconTint)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(VIBRATION_PATTERN)
                .setContentText(notificationText)
                .setStyle(style)
                .setContentIntent(openAppIntent);

        if (soundUri != null) {
            notificationBuilder.setSound(soundUri);
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        } else {
            notificationBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS);
        }

        if (!TextUtils.isEmpty(title)) {
            notificationBuilder.setContentTitle(title);
        }

        int notifyIcon = manager.getDrawableIdentifier("notify_icon");
        Bitmap largeIcon = null;
        if (notifyIcon > 0) {
            Drawable drawable = AppCompatResources.getDrawable(context, notifyIcon);
            largeIcon = drawableToBitmap(drawable);
        }

        if (isValidIcon(largeIcon)) {
            notificationBuilder.setLargeIcon(largeIcon);
        }
        else {
            Bitmap icLauncherIcon = BitmapFactory.decodeResource(context.getResources(), context.getResources().getIdentifier("ic_launcher", "mipmap", context.getPackageName()));
            if (isValidIcon(icLauncherIcon)) {
                notificationBuilder.setLargeIcon(icLauncherIcon);
            }
        }

        notificationManager.notify(UUID.randomUUID().toString(), currentId.getAndIncrement(), notificationBuilder.build());
    }

    private boolean isValidIcon(Bitmap largeIcon) {
        return largeIcon != null && largeIcon.getWidth() > 0 && largeIcon.getHeight() > 0;
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.setDensity((int) Resources.getSystem().getDisplayMetrics().density);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
