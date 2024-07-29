package se.infomaker.iap.push.google;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

class FCMUtil {
    private FCMUtil() {
    }

    private static final String TOKEN = "token";
    private static final String DEVICE_ID = "deviceId";
    private static final String PUSH_TOPIC = "pushTopic";
    private static final String APPLICATION = "application";
    private static final String PUSH_REGISTER_URL = "pushRegisterUrl";
    private static final String PUSH_UNREGISTER_URL = "pushUnregisterUrl";

    private static SharedPreferences pushPreferences(Context context) {
        return context.getSharedPreferences("GCM_preferences", Context.MODE_PRIVATE);
    }

    static void updateRegistration(Context context, String deviceId, String token) {
        pushPreferences(context).edit()
                .putString(DEVICE_ID, deviceId)
                .putString(TOKEN, token)
                .apply();
    }

    static void removeRegistration(Context context) {
        pushPreferences(context).edit()
                .remove(DEVICE_ID)
                .apply();
    }

    static void addUnregisterUrl(Context context, String url) {
        pushPreferences(context).edit()
                .putString(PUSH_UNREGISTER_URL, url)
                .apply();
    }

    static void registerForDeviceIdChange(Context context,
                                       SharedPreferences.OnSharedPreferenceChangeListener listener) {
        pushPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    static void unregisterForDeviceIdChange(Context context,
                                                 SharedPreferences.OnSharedPreferenceChangeListener listener) {
        pushPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Store the pushConfiguration associated with this device
     * @param context
     * @param pushTopic
     * @param application
     * @param pushRegisterURL
     * @param pushUnregisterURL
     */
    static void updateRegistrationConfig(Context context, String pushTopic, String application, String pushRegisterURL, String pushUnregisterURL) {
        pushPreferences(context).edit()
                .putString(PUSH_TOPIC, pushTopic)
                .putString(APPLICATION, application)
                .putString(PUSH_REGISTER_URL, pushRegisterURL)
                .putString(PUSH_UNREGISTER_URL, pushUnregisterURL).apply();
    }

    /**
     * Gets the stored pushTopic or null if none exist
     *
     * @param context
     * @return pushTopic
     */
    static String getPushTopic(Context context) {
        return pushPreferences(context).getString(PUSH_TOPIC, null);
    }

    /**
     * Gets the stored application or null if none exist
     *
     * @param context
     * @return application
     */
    static String getApplication(Context context) {
        return pushPreferences(context).getString(APPLICATION, null);
    }

    /**
     * Gets the latest known device id or null if none exist
     *
     * @param context
     * @return device id
     */
    @Nullable
    static String getDeviceId(Context context) {
        return pushPreferences(context).getString(DEVICE_ID, null);
    }

    /**
     * Gets the latest known push registration URL or null if none exist
     *
     * @param context
     * @return push register url
     */
    static String getPushRegisterUrl(Context context) {
        return pushPreferences(context).getString(PUSH_REGISTER_URL, null);
    }

    /**
     * Gets the latest known push unregistration URL or null if none exist
     *
     * @param context
     * @return push register url
     */
    static String getPushUnregisterUrl(Context context) {
        return pushPreferences(context).getString(PUSH_UNREGISTER_URL, null);
    }

    /**
     * Gets latest known token or null if none exist
     *
     * @param context
     * @return token
     */
    static String getToken(Context context) {
        return pushPreferences(context).getString(TOKEN, null);
    }

    static void setToken(Context context, String token) {
        pushPreferences(context).edit()
                .putString(TOKEN, token)
                .apply();
    }

    static boolean isTokenUpdated(Context context, String token) {
        return !token.equals(getToken(context));
    }
}
