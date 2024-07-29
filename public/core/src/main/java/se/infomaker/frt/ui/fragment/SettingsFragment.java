package se.infomaker.frt.ui.fragment;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.navigaglobal.mobile.R;

import se.infomaker.frt.moduleinterface.ModuleInterface;
import se.infomaker.frt.statistics.StatisticsEvent;
import se.infomaker.frt.statistics.StatisticsManager;
import se.infomaker.frtutilities.ConfigManager;
import se.infomaker.frtutilities.MainMenuItem;
import se.infomaker.frtutilities.ModuleInformation;
import se.infomaker.frtutilities.ModuleInformationManager;
import se.infomaker.settings.SettingsConfig;
import timber.log.Timber;

/**
 * Created by lohnn on 2015-12-16.
 * © Infomaker Scandinavia AB
 */
public class SettingsFragment extends PreferenceFragmentCompat implements ModuleInterface {
    private static final String ARG_MODULE_IDENTIFIER = "moduleId";

    private ModuleInformation mModuleInformation;

    public static final String KEY_PREF_EPAPER_AMOUNT_SAVE = "pref_epaper_amount_save";
    public static final String KEY_PREF_DOWNLOAD = "pref_download";
    public static final String KEY_PREF_DOWNLOAD_CELLULAR = "pref_download_cellular";
    public static final String KEY_PREF_RECEIVE_PUSH = "pref_receive_push";
    public static final String KEY_PREF_PUSH_SOUND = "pref_push_sound";
    public static final String KEY_PREF_PUSH_VIBRATE = "pref_push_vibrate";
    public static final String KEY_BUILD_VERSION = "pref_build_version";
    public static final String KEY_PRIVACY_POLICY = "pref_privacy_policy";
    public static final String KEY_PREF_PREFETCH = "pref_prefetch";
//    public static final String KEY_OPEN_SOURCE_LICENSES = "pref_open_source_licenses";

    private CheckBoxPreference prefReceivePush;
    private CheckBoxPreference prefDownload;
    private CheckBoxPreference prefDownloadCellular;
    private SettingsConfig config;
    private CheckBoxPreference prefPushSound;
    private CheckBoxPreference prefPushVibrate;
    private CheckBoxPreference prefPrefetch;

    boolean hasEpaper = false;
    boolean isPrefetchEnabled = false;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        if (getArguments() != null) {
            String moduleIdentifier = getArguments().getString(ARG_MODULE_IDENTIFIER);
            config = ConfigManager.getInstance(getActivity()).getConfig(moduleIdentifier, SettingsConfig.class);
            mModuleInformation = ModuleInformationManager.getInstance().getModuleInformation(moduleIdentifier);
        }

        if(mModuleInformation != null) {
            StatisticsManager.getInstance().logEvent(new StatisticsEvent.Builder()
                    .viewShow()
                    .moduleId(String.valueOf(mModuleInformation.getIdentifier())).moduleName(mModuleInformation.getName()).moduleTitle(mModuleInformation.getTitle())
                    .viewName("settings")
                    .build());
        }

        //Load preferences from XML
        for (MainMenuItem mainMenuItem : ConfigManager.getInstance(getActivity()).getMainMenuConfig().getMainMenuItems()) {final String moduleName = mainMenuItem.getModuleName();
            if (moduleName != null && moduleName.startsWith("Epaper")) {
                hasEpaper = true;
                break;
            }
            if (mainMenuItem.getPrefetchEnabled() && !isPrefetchEnabled) {
                isPrefetchEnabled = true;
            }
        }
        if (hasEpaper) {
            addPreferencesFromResource(R.xml.settings_preferences);
        } else {
            addPreferencesFromResource(R.xml.settings_no_epaper_preferences);
        }

        Preference buildVersion = findPreference(KEY_BUILD_VERSION);

        buildVersion.setSummary(getVersion());

        prefReceivePush = (CheckBoxPreference) findPreference(KEY_PREF_RECEIVE_PUSH);
        prefPushSound = (CheckBoxPreference) findPreference(KEY_PREF_PUSH_SOUND);
        prefPushVibrate = (CheckBoxPreference) findPreference(KEY_PREF_PUSH_VIBRATE);
        prefDownload = (CheckBoxPreference) findPreference(KEY_PREF_DOWNLOAD);
        prefDownloadCellular = (CheckBoxPreference) findPreference(KEY_PREF_DOWNLOAD_CELLULAR);
        prefPrefetch = (CheckBoxPreference) findPreference(KEY_PREF_PREFETCH);
        if (prefPrefetch != null) {
            prefPrefetch.setDefaultValue(false);
        }

        setupCheckboxes();

        prefReceivePush.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean b = (boolean) newValue;
            if (hasEpaper) {
                prefDownload.setEnabled(b);
                prefDownloadCellular.setEnabled(b && prefDownload.isChecked());
            }
            prefPushSound.setEnabled(b);
            prefPushVibrate.setEnabled(b);
            return true;
        });

        if (hasEpaper) {
            prefDownload.setOnPreferenceChangeListener((preference, newValue) -> {
                prefDownloadCellular.setEnabled((Boolean) newValue);
                return true;
            });
        }

        if (isPrefetchEnabled) {
            prefPrefetch.setVisible(true);
            prefPrefetch.setOnPreferenceChangeListener((preference, newValue) -> true);
        } else {
            prefPrefetch.setVisible(false);
        }

        if (config != null && !TextUtils.isEmpty(config.getPolicyUrl())) {
            findPreference(KEY_PRIVACY_POLICY).setOnPreferenceClickListener(preference -> {
                android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(config.getPolicyUrl()));
                startActivity(intent);
                return true;
            });
        } else {
            findPreference(KEY_PRIVACY_POLICY).setVisible(false);
        }
    }

    private String getVersion() {
        Context context = getActivity().getApplicationContext();
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String versionName = info.versionName;
            int versionCode = (int) PackageInfoCompat.getLongVersionCode(info);
            return versionName + " (" + versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e("Failed to get version");
            return "Unknown";
        }
    }

    private void setupCheckboxes() {
        if (hasEpaper) {
            prefDownload.setEnabled(prefReceivePush.isEnabled() && prefReceivePush.isChecked());
            prefDownloadCellular.setEnabled(prefDownload.isChecked() && prefDownload.isEnabled());
        }

        if (isPrefetchEnabled) {
           prefPrefetch.setEnabled(true);
        }
        prefPushSound.setEnabled(prefReceivePush.isChecked());
        prefPushVibrate.setEnabled(prefReceivePush.isChecked());
    }

    @Override
    public boolean shouldDisplayToolbar() {
        return true;
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onAppBarPressed() {

    }
}
