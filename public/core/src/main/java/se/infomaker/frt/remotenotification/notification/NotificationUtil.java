package se.infomaker.frt.remotenotification.notification;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.settingsconfig.DefaultSettingsConfig;

/**
 * Common helper methods used for notifications
 */
public class NotificationUtil {

    /**
     * Determines if sound should be played when presenting notification
     * @param context
     * @param moduleIdentifier
     * @return true if a sound should be played when presenting notification
     */
    public static boolean shouldPlayNotificaitonSound(Context context, String moduleIdentifier) {
        DefaultSettingsConfig settingsConfig = ConfigManager.getInstance(context).getGlobalConfig(moduleIdentifier).getDefaultSettingsConfig();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_push_sound", settingsConfig.getSoundOnNotification());
    }

    /**
     * Determines if the device should vibrate when presenting notification
     * @param context
     * @param moduleIdentifier
     * @return
     */
    public static boolean shouldVibrateOnNotification(Context context, String moduleIdentifier) {
        DefaultSettingsConfig settingsConfig = ConfigManager.getInstance(context).getGlobalConfig(moduleIdentifier).getDefaultSettingsConfig();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("pref_push_vibrate", settingsConfig.getVibrateOnNotification());
    }
}
