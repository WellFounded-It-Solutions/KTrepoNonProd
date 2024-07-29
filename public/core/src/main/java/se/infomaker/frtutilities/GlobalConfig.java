package se.infomaker.frtutilities;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import se.infomaker.frtutilities.settingsconfig.DefaultSettingsConfig;

/**
 * Created by magnusekstrom on 05/07/16.
 */

public class GlobalConfig implements Serializable {
    @SerializedName("defaultSettings")
    DefaultSettingsConfig defaultSettingsConfig = new DefaultSettingsConfig();

    public DefaultSettingsConfig getDefaultSettingsConfig() {
        return defaultSettingsConfig;
    }
}
