package se.infomaker.settings;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.preference.PreferenceManager;

import com.navigaglobal.mobile.R;

import java.security.InvalidParameterException;
import java.util.List;

import io.reactivex.Observable;
import se.infomaker.frt.ui.fragment.SettingsFragment;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.settingsconfig.DefaultSettingsConfig;

public class SettingsHelper {
    private static final String INITIALIZED_KEY = "isInitialized";

    public static void initializeSettings(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isInitialized = preferences.getBoolean(INITIALIZED_KEY, false);
        if (!isInitialized) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(INITIALIZED_KEY, true);

            DefaultSettingsConfig settingsConfig = ConfigManager.getInstance(context).getGlobalConfig().getDefaultSettingsConfig();
            int numberOfIssuesConfig = settingsConfig.getNumberOfIssues();

            String[] options = context.getResources().getStringArray(R.array.listValues);
            List<Integer> optionsInteger = Observable.fromArray(options).map(Integer::parseInt).filter(integer -> integer >= 0).toList().blockingGet();
            int closestValue = numberOfIssuesConfig < 0 ? -1 : getClosestValue(numberOfIssuesConfig, optionsInteger);

            editor.putBoolean(SettingsFragment.KEY_PREF_RECEIVE_PUSH, settingsConfig.getAllowPush())
                    .putBoolean(SettingsFragment.KEY_PREF_DOWNLOAD, settingsConfig.getAllowDownload())
                    .putBoolean(SettingsFragment.KEY_PREF_PUSH_SOUND, settingsConfig.getSoundOnNotification())
                    .putBoolean(SettingsFragment.KEY_PREF_PUSH_VIBRATE, settingsConfig.getVibrateOnNotification())
                    .putString(SettingsFragment.KEY_PREF_EPAPER_AMOUNT_SAVE, String.valueOf(closestValue))
                    .putBoolean(SettingsFragment.KEY_PREF_PREFETCH, settingsConfig.isPrefetchingEnabled())
                    .apply();
        }
    }

    /**
     * Returns the value in values list closest to the inputValue.
     * If inputValue is equally close to two values in values list, we return the lower of the two.
     *
     * @param inputValue
     * @param values
     * @return the value in values list closest to the inputValue.
     * @throws InvalidParameterException if values is empty or null
     */
    static int getClosestValue(int inputValue, List<Integer> values) {
        if (values == null || values.isEmpty()) {
            throw new InvalidParameterException("List of values must not be empty");
        }
        return Observable.combineLatest(
                Observable.just(inputValue),
                Observable.fromIterable(values).sorted(), IntegerPair::new)
                .reduce((pair1, pair2) -> pair1.compareTo(pair2) < 0 ? pair1 : pair2)
                .map(pair -> pair.second).blockingGet();
    }

    private static class IntegerPair extends Pair<Integer, Integer> implements Comparable<IntegerPair> {
        /**
         * Constructor for a Pair.
         *
         * @param first  the first integer in the Pair
         * @param second the second integer in the pair
         */
        public IntegerPair(Integer first, Integer second) {
            super(first, second);
        }

        /**
         * @return distance between the pair values
         */
        int getDistance() {
            return Math.abs(first - second);
        }

        /**
         * Returns 0 if pair values are same between the two pairs
         *
         * Returns 1 if other.getDistance() has larger distance than this.getDistance(),
         * or if distance is equal; other.second is larger than this.second
         *
         * Returns -1 if other.getDistance() has smaller distance than this.getDistance(),
         * or if distance is equal; other.second is smaller than this.second
         *
         * @param other
         * @return
         */
        @Override
        public int compareTo(@NonNull IntegerPair other) {
            if (this.equals(other)) {
                return 0;
            }

            int thisDistance = this.getDistance();
            int otherDistance = other.getDistance();

            if (thisDistance == otherDistance) {
                return this.second < other.second ? -1 : 1;
            }

            return thisDistance < otherDistance ? -1 : 1;
        }
    }
}
